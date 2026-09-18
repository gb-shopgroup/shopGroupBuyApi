package cn.com.shopgroup.order.controller.leader;

import cn.com.shopgroup.common.cache.RedisConstant;
import cn.com.shopgroup.common.cache.RedisHelper;
import cn.com.shopgroup.common.utils.CustomIdGenerator;
import cn.com.shopgroup.common.utils.MoneyUtil;
import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.order.constants.OrderStatusEnum;
import cn.com.shopgroup.order.constants.PaymentStatusEnum;
import cn.com.shopgroup.order.model.GbOrderInfo;
import cn.com.shopgroup.order.model.OrderTransactionLog;
import cn.com.shopgroup.order.service.GbOrderBusinessInfoService;
import cn.com.shopgroup.order.service.GbOrderInfoService;
import cn.com.shopgroup.order.service.OrderTransactionLogService;
import cn.com.shopgroup.order.service.RefundConfirmService;
import cn.com.shopgroup.user.service.GbOrgMessageInfoService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.yeepay.yop.sdk.utils.DigitalEnvelopeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrderRefundNotifyController {

    @Resource
    private GbOrderInfoService orderInfoService;

    @Resource
    private GbOrderBusinessInfoService orderBusinessInfoService;

    @Resource
    private OrderTransactionLogService transactionLogService;

    @Resource
    private GbOrgMessageInfoService messageService;

    @Resource
    private RefundConfirmService refundConfirmService;

    @Resource
    private RedisHelper redisHelper;

    // 退款结果回调通知: 易宝退款最终结果异步通知(与支付回调一致, 为 RSA2048 数字信封加密的 form 报文)
    @PostMapping("/leader/refund/notify")
    public String notify(HttpServletRequest req) {

        log.info("易宝退款回调-order/leader/refund/notify ......");
        // 1. 易宝RSA回调仅支持form格式
        final String contentTypeStr = req.getContentType();
        log.info("易宝退款回调 - contentTypeStr: {}", contentTypeStr);
        if (!StringUtils.startsWith(contentTypeStr, "application/x-www-form-urlencoded")) {
            throw new IllegalArgumentException("RSA回调请求仅支持form格式");
        }
        // 2. 取出密文并解密为明文JSON
        String response = req.getParameter("response");
        if (StringUtils.isEmpty(response)) {
            log.error("易宝退款回调 - response 为空");
            return "fail";
        }
        String plaintText;
        JSONObject jsonResponse;
        try {
            plaintText = DigitalEnvelopeUtils.decrypt(response, "RSA2048");
            jsonResponse = JSONObject.parse(plaintText);
        } catch (Exception e) {
            log.error("易宝退款回调 - 报文解密/解析失败", e);
            return "fail";
        }
        log.info("易宝退款回调 - jsonResponse: {}", JSON.toJSONString(jsonResponse));

        // 3. 关键字段解析(均做存在性判断, 兼容易宝不同接口版本的字段命名)
        // 商户订单号
        String orderNo = getJsonString(jsonResponse, "orderId");
        // 易宝退款单号(唯一), 作为回调幂等键
        String refundNo = getJsonString(jsonResponse, "uniqueRefundNo");
        // 退款状态: SUCCESS=退款成功, FAILED=退款失败, PROCESSING=退款处理中(易宝后续再次通知)
        String status = getJsonString(jsonResponse, "status");
        if (StringUtils.isEmpty(status)) {
            status = getJsonString(jsonResponse, "refundStatus");
        }
        // 本次退款金额(元)
        String refundAmount = getJsonString(jsonResponse, "refundAmount");
        // 退款失败码/失败原因
        String failCode = getJsonString(jsonResponse, "failCode");
        String failReason = getJsonString(jsonResponse, "failReason");

        if (StringUtils.isEmpty(orderNo)) {
            log.error("易宝退款回调 - 未获取到订单号, 明文:{}", plaintText);
            return "fail";
        }
        // 退款处理中: 结果未确定, 先确认接收, 等易宝下一次通知;
        // 注意: 此分支不能加幂等锁, 否则会吞掉后续的退款成功/失败通知
        if ("PROCESSING".equalsIgnoreCase(status)) {
            log.info("易宝退款回调 - 退款处理中, 等待易宝下次通知, orderNo:{}", orderNo);
            return "success";
        }
        // 退款结果判定: status=SUCCESS 且无失败码/失败原因才算退款成功, 其余(FAILED/未知状态/带失败信息)按失败处理
        boolean refundSuccess = "SUCCESS".equalsIgnoreCase(status)
                && StringUtils.isEmpty(failCode) && StringUtils.isEmpty(failReason);
        // 兜底: 易宝未回传退款单号时用订单号作为幂等键
        if (StringUtils.isEmpty(refundNo)) {
            refundNo = orderNo;
        }

        // 4. 幂等/防并发: 同一笔退款只落地一次(易宝为至少一次投递, 会重复通知);
        // 处理异常时释放锁并返回 fail, 由易宝重试, 避免回调丢失导致订单长期停留在售后
        String lockKey = RedisConstant.RedisRefundNotifyKey + refundNo;
        if (!redisHelper.getLock(lockKey, RedisConstant.RedisRefundNotifyExpired)) {
            log.info("易宝退款回调 - 重复通知, 已处理过, orderNo:{}, refundNo:{}", orderNo, refundNo);
            return "success";
        }
        try {
            handleRefundResult(orderNo, refundNo, refundAmount, refundSuccess, failCode, failReason);
        } catch (Exception e) {
            redisHelper.releaseLock(lockKey);
            log.error("易宝退款回调 - 处理异常, orderNo:{}, refundNo:{}", orderNo, refundNo, e);
            return "fail";
        }
        return "success";
    }

    /**
     * 退款结果业务处理
     * 金额维护口径: 申请与审核阶段都不维护主表refund_fee, 只在退款资金最终成功后按回调实际退款金额累加一次;
     * 商品行退款以"退款数量"体现(申请时按行累加, 审核同意保留, 审核拒绝按行回退), 商品维度退款金额可由 退款数量×商品单价 推算, 不单独落库.
     * 回调不再重复累计商品数量/金额, 只做与资金最终结果相关的处理:
     * 1. 退款成功: 资金相关落库(退款成功记录+累加refund_fee+订单退款流水号+交易流水)由 RefundConfirmService 在同一事务内完成,
     *    幂等基准为"退款记录表中该易宝退款单号的系统退款成功记录"(支持一单多次部分退款; 原订单表refund_no单值比对在多笔部分退款时存在重复累加风险);
     *    事务外再做分账表置已退款(不再参与分账)、订单主状态兜底同步(全部退完=已退款4, 否则保持售后5)、团长消息
     * 2. 退款失败: 记录失败流水与失败原因(不落退款成功记录)、订单不再停留在售后(5)、通知团长人工处理
     * (回调报文只有订单维度的信息, 不含商品明细, 商品行的申请占坑数量无法按行回退, 需团长重新发起或人工核对)
     */
    private void handleRefundResult(String orderNo, String refundNo, String refundAmount,
                                    boolean refundSuccess, String failCode, String failReason) {

        GbOrderInfo orderInfo = orderInfoService.getOrderInfoByOrderNo(orderNo);
        if (ObjectUtils.isEmpty(orderInfo)) {
            log.error("易宝退款回调 - 订单不存在, orderNo:{}, refundNo:{}", orderNo, refundNo);
            return;
        }
        double amount = StringUtils.isEmpty(refundAmount) ? 0D : Double.parseDouble(refundAmount);

        if (refundSuccess) {
            // 资金核心落库(事务化, 幂等判重 + 退款成功记录 + 累加refund_fee + 订单退款流水号 + 交易流水):
            // 任一步异常整体回滚并由易宝重试完整重做, 修复原"记流水号与累加金额分离"在中断重试后金额漏记的问题
            int refundCent = MoneyUtil.yuanToCent(amount);
            boolean firstConfirmed = refundConfirmService.confirmRefundSuccess(orderInfo, refundNo, refundCent, amount);
            if (!firstConfirmed) {
                log.info("易宝退款回调 - 该退款单已确认过, 幂等跳过, orderNo:{}, refundNo:{}", orderNo, refundNo);
                return;
            }
            // 以下非资金操作放事务外(重复执行无副作用):
            // 分账订单表同步为已退款(comm_status=5), 已退款订单不再参与分账
            orderBusinessInfoService.editMiniLeaderOrderBusinessRefundStatus(orderNo);
            // 订单主状态兜底同步: 订单仍停留在售后(5)时, 商品全部退完置已退款(4), 否则只要有未退完的保持售后(5)
            if (orderInfo.getStatus() != null && orderInfo.getStatus().intValue() == OrderStatusEnum.APPLY_REFUND.getCode()) {
                orderInfoService.updateOrderStatusAfterRefundAgree(orderNo);
            }
            // 团长消息通知(消息类型: 1=系统消息 2=内部消息 3=业务消息)
            messageService.addMiniLeaderMessageInfo(orderInfo.getLeaderId(), orderInfo.getLeaderId(), (byte) 2,
                    "订单号(" + orderNo + ") 退款成功, 退款金额 " + amount + " 元。");
            log.info("易宝退款回调 - 退款成功处理完成, orderNo:{}, refundNo:{}, refundAmount:{}", orderNo, refundNo, refundAmount);
            return;
        }

        // 退款失败: 资金未退出, 记录失败流水并通知团长, 同时让订单不再停留在售后(5), 避免订单一直卡死
        String reason = StringUtils.isEmpty(failReason) ? failCode : failReason;
        // 仅记失败交易流水, 不落退款记录(修复原失败分支复用落库逻辑会误记一条"系统退款成功/已退款x元"记录的问题)
        insertTransactionLog(orderNo, amount, PaymentStatusEnum.FAILED, "退款回调失败:" + reason + ",退款单号:" + refundNo);
        if (orderInfo.getStatus() != null && orderInfo.getStatus().intValue() == OrderStatusEnum.APPLY_REFUND.getCode()) {
            orderInfoService.restoreOrderStatusAfterRefundReview(orderNo);
        }
        messageService.addMiniLeaderMessageInfo(orderInfo.getLeaderId(), orderInfo.getLeaderId(), (byte) 2,
                "订单号(" + orderNo + ") 退款失败(" + reason + "), 请核对后重新发起退款。");
        log.error("易宝退款回调 - 退款失败, orderNo:{}, refundNo:{}, failCode:{}, failReason:{}",
                orderNo, refundNo, failCode, failReason);
    }

    // 记录退款回调交易流水(仅流水; 退款成功记录由 RefundConfirmService 事务内落库, 失败分支不落退款记录)
    private void insertTransactionLog(String orderNo, double amount, PaymentStatusEnum payStatus, String remark) {

        OrderTransactionLog transactionLog = new OrderTransactionLog();
        transactionLog.setOrderNo(orderNo);
        transactionLog.setTransactionNo("tr" + CustomIdGenerator.generateUUID());
        transactionLog.setRefundAmount(amount);
        transactionLog.setPayAmount(amount);
        transactionLog.setRemark(remark);
        transactionLog.setPayMethod("yeePay");
        transactionLog.setOperatorName("退款回调");
        transactionLog.setAddTime(TimeUtils.getTimeStamp());
        transactionLog.setPayStatus(payStatus.getCode());
        transactionLogService.addTransactionLog(transactionLog);
    }

    // 读取回调明文字段(字段不存在或为空时返回空串)
    private String getJsonString(JSONObject json, String key) {

        if (json == null || json.get(key) == null) {
            return "";
        }
        String value = json.getString(key);
        return value == null ? "" : value.trim();
    }

}
