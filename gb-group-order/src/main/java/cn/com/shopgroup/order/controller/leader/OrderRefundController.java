package cn.com.shopgroup.order.controller.leader;

import cn.com.shopgroup.common.exception.BusinessException;
import cn.com.shopgroup.common.utils.CustomIdGenerator;
import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.common.utils.MoneyUtil;
import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.yeepay.YeePayUtils;
import cn.com.shopgroup.order.exception.OrderErrorCodeEnum;
import cn.com.shopgroup.goods.service.GbGoodsInfoService;
import cn.com.shopgroup.goods.service.GbGoodsSkuInfoService;
import cn.com.shopgroup.order.constants.PaymentStatusEnum;
import cn.com.shopgroup.order.http.request.OrderApproveRequest;
import cn.com.shopgroup.order.http.request.OrderRefundGoodsRequest;
import cn.com.shopgroup.order.http.request.OrderRefundInfoRequest;
import cn.com.shopgroup.order.model.GbOrderGoodsInfo;
import cn.com.shopgroup.order.model.GbOrderGoodsRefundRecord;
import cn.com.shopgroup.order.model.GbOrderInfo;
import cn.com.shopgroup.order.model.OrderTransactionLog;
import cn.com.shopgroup.order.service.GbOrderBusinessInfoService;
import cn.com.shopgroup.order.service.GbOrderGoodsRefundRecordService;
import cn.com.shopgroup.order.service.GbOrderInfoService;
import cn.com.shopgroup.order.service.OrderTransactionLogService;
import cn.com.shopgroup.user.model.GbOrgStaffInfo;
import cn.com.shopgroup.user.service.GbOrgMessageInfoService;
import cn.com.shopgroup.user.service.GbOrgStaffInfoService;
import cn.com.shopgroup.user.utils.RequestParamsUtils;
import com.alibaba.fastjson2.JSON;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@Api(value = "团长端--退款管理")
@RequestMapping("/order")
public class OrderRefundController {

    @Resource
    private GbOrderInfoService orderInfoService;

    @Resource
    private GbOrderBusinessInfoService orderBusinessInfoService;

    @Resource
    private GbOrgStaffInfoService staffService;

    @Resource
    private GbOrgMessageInfoService messageService;

    @Resource
    private GbOrderBusinessInfoService businessService;
    @Resource
    private GbOrderGoodsRefundRecordService refundRecordService;
    @Resource
    private OrderTransactionLogService transactionLogService;

    @Resource
    private GbGoodsInfoService goodsService;

    @Resource
    private GbGoodsSkuInfoService skuService;

    // 退款订单数量: 订单中存在商品发生过退款(部分退/整单全退, 审核同意)即计入
    @GetMapping("/leader/refund/count")
    public JsonResult refundOrderCount(@RequestParam("gid") Long groupId, @RequestParam("pid") Long pointId) {
        log.info("退款订单数量 /leader/refund/count groupId:{},pointId:{}",groupId,pointId);
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            throw new BusinessException(OrderErrorCodeEnum.LEADER_NOT_EXIST);
        }

        // 查询总数
        Long total = orderInfoService.getMiniLeaderRefundOrderCount(leaderId, groupId, pointId);
        return JsonResult.success(total);
    }

    //售后订单审核（同意/不同意）。status: 1=同意, 2=不同意; 每单一行key=订单号, value.refundGoodsMap为本次申请的订单商品行(行内refundNum/refundAmount为本次申请值); 同意=按本次申请金额向易宝发起退款(支持部分退款)、保留申请时占坑的商品数量并把商品售后状态置同意(主表退款金额refund_fee不在此累加, 统一由退款回调成功后按实际退款金额累加; 商品行退款以退款数量体现, 不单独落库金额); 不同意=主表退款金额申请/审核阶段均未累加无需回退(天然回到申请前), 仅按行回退商品退款/退货退款数量并把商品售后状态置不同意; 团长端旧版本未回传refundFlag/金额时后端按该订单最近一笔售后记录兜底
    @PostMapping("/leader/refund/approve")
    public JsonResult approveRefundOrder(@Validated @RequestBody OrderApproveRequest approveRequest) {
        log.info("团长管理-审核退款订单处理.../order/refund/approve,参数:{}", JSON.toJSONString(approveRequest));
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            throw new BusinessException(OrderErrorCodeEnum.LEADER_NOT_EXIST);
        }
        // 从请求头中获取员工id
        Long staffId = RequestParamsUtils.getRequestHeaderStaffId();
        Long opId = leaderId;
        String opName = "团长本人";
        if (staffId != 0) {
            GbOrgStaffInfo staffInfo = staffService.getStaffInfo(staffId);
            if (ObjectUtils.isEmpty(staffInfo)) {
                throw new BusinessException("staffId=" + staffId + "未查询到相关员工数据");
            }
            opName = staffInfo.getStaffName();
            opId = staffInfo.getStaffId();
        }
        // 审核结果 1 同意 2 拒绝
        Integer approveStatus = approveRequest.getStatus();
        String reason = approveRequest.getReason();
        //参数设置校验，这里不用判断空map情况
        Map<String, OrderRefundInfoRequest> refundMap = approveRequest.getRefundOrderGoodsMap();
        if (refundMap.isEmpty()) {
            throw new BusinessException(OrderErrorCodeEnum.REFUND_PARAM_ERROR);
        }
        for (Map.Entry<String, OrderRefundInfoRequest> entry : refundMap.entrySet()) {
            String orderNo = entry.getKey();
            OrderRefundInfoRequest refundInfoRequest = entry.getValue();
            // 先查询订单
            GbOrderInfo orderInfo = orderInfoService.getOrderInfoByOrderNo(orderNo);
            if (ObjectUtils.isEmpty(orderInfo)) {
                log.warn("订单不存在，审核失败");
                continue;
            }
            //查询原订单中的商品
            List<GbOrderGoodsInfo> goodsList = orderInfoService.getOrderGoodsList(orderNo);
            if (CollectionUtils.isEmpty(goodsList)) {
                throw new BusinessException(OrderErrorCodeEnum.ORDER_GOODS_NOT_FOUND);
            }
            // 订单商品处理: 仅校验本次审核的申请商品, 数量不能超过对应退款类型的剩余可退数量
            Map<Long, OrderRefundGoodsRequest> refundGoodsMap = refundInfoRequest.getRefundGoodsMap();
            int isReturnGoods = refundInfoRequest.getRefundFlag() == null ? 0 : refundInfoRequest.getRefundFlag();
            for (GbOrderGoodsInfo goods : goodsList) {
                Long orderGoodsId = goods.getId();
                // 非本次审核的商品行不参与校验
                if (!refundGoodsMap.containsKey(orderGoodsId)) {
                    continue;
                }
                OrderRefundGoodsRequest temp = refundGoodsMap.get(orderGoodsId);
                if (temp.getRefundNum() == null || temp.getRefundNum() <= 0) {
                    throw new BusinessException(OrderErrorCodeEnum.REFUND_QTY_ILLEGAL);
                }
                int goodsNum = goods.getGoodsNum() == null ? 0 : goods.getGoodsNum(); // 订单商品数量
                int receiptNum = goods.getReceiptNum() == null ? 0 : goods.getReceiptNum(); // 收货数量
                // 退款数(退待收货部分, 申请累计)
                int refundNum = goods.getRefundNum() == null ? 0 : goods.getRefundNum();
                // 退货退款数(退已收货部分, 申请累计)
                int refundGoodsNum = goods.getRefundGoodsNum() == null ? 0 : goods.getRefundGoodsNum();
                int remainRefundNum;
                if (isReturnGoods == 1) {
                    // 退款(未收货部分)的剩余可退数量
                    remainRefundNum = goodsNum - receiptNum - refundNum;
                } else if (isReturnGoods == 2) {
                    // 退货退款(已收货部分)的剩余可退数量
                    remainRefundNum = receiptNum - refundGoodsNum;
                } else {
                    // 兼容旧版本客户端(未传退款类型): 按原口径 购买数量-退货退款数量 校验
                    remainRefundNum = goodsNum - refundGoodsNum;
                }
                if (remainRefundNum < 0) {
                    remainRefundNum = 0;
                }
                if (temp.getRefundNum() > remainRefundNum) {
                    throw new BusinessException(OrderErrorCodeEnum.REFUND_QTY_EXCEEDED);
                }
            }

            //同意处理
            if (approveStatus == 1) {
                int m = handleAgree(leaderId, opId, opName, orderInfo, refundInfoRequest, reason);
                if (m == 0) {
                    continue;
                }
            } else {
                //拒绝
                handleRefuse(leaderId, opId, opName, refundInfoRequest, reason);
            }
        }
        // 返回
        return JsonResult.success("审核成功");
    }

    //拒绝（不同意）处理: 主表退款金额申请阶段未累加, 拒绝后金额天然回到申请前, 只需回退商品行退款/退货退款数量
    public void handleRefuse(Long leaderId, Long opId, String opName, OrderRefundInfoRequest request, String reason) {
        String orderNo = request.getOrderNo();
        // 拒绝退款(记录操作人/拒绝原因)
        orderInfoService.editMiniLeaderRefundOrder(orderNo, opName, reason);
        //拒绝请求过来的订单商品
        List<Long> orderGoodsIds = new ArrayList<>();
        for (Map.Entry<Long, OrderRefundGoodsRequest> entry : request.getRefundGoodsMap().entrySet()) {
            Long orderGoodsId = entry.getKey();
            orderGoodsIds.add(orderGoodsId);
        }
        //标识订单商品售后状态不同意
        orderInfoService.updateOrderGoodsApplyStatus(orderNo, orderGoodsIds, 3);
        // ---------- 恢复商品行申请前状态(商品行退款/退货退款数量申请时已占坑; 主表refund_fee不在申请时累加, 拒绝后金额天然回到申请前, 无需扣回) ----------
        // 本次申请退款类型优先取审核请求回传值; 旧版本团长端可能未回传, 则按该订单最近一笔售后申请记录兜底
        int isReturnGoods = request.getRefundFlag() == null ? 0 : request.getRefundFlag();
        if (isReturnGoods != 1 && isReturnGoods != 2) {
            List<GbOrderGoodsRefundRecord> recordList = refundRecordService.getRefundRecordListByOrderNo(orderNo);
            for (int i = recordList.size() - 1; i >= 0; i--) {
                GbOrderGoodsRefundRecord record = recordList.get(i);
                if (record.getRefundFlag() != null
                        && (record.getRefundFlag() == 1 || record.getRefundFlag() == 2)) {
                    isReturnGoods = record.getRefundFlag();
                    break;
                }
            }
        }
        // 恢复商品数量: 回退商品行本次申请累计的退款/退货退款数量, 否则占坑导致无法再次申请/数量虚高
        if (isReturnGoods == 1 || isReturnGoods == 2) {
            Map<Long, Integer> refundNumMap = new HashMap<>();
            for (Map.Entry<Long, OrderRefundGoodsRequest> entry : request.getRefundGoodsMap().entrySet()) {
                OrderRefundGoodsRequest req = entry.getValue();
                if (req.getRefundNum() != null && req.getRefundNum() > 0) {
                    refundNumMap.put(entry.getKey(), req.getRefundNum());
                }
            }
            orderInfoService.deductOrderGoodsRefundByOrderNo(refundNumMap, isReturnGoods);
        } else {
            // 类型仍未知(历史脏数据): 商品数量未回退, 需人工核对或团长端升级后重试
            log.warn("审核拒绝恢复: 订单{}未获取到退款类型, 商品行退款数量未回退", orderNo);
        }
        //插入退货记录售后日志
        GbOrderGoodsRefundRecord refundRecord = new GbOrderGoodsRefundRecord();
        refundRecord.setOperateId(opId);
        refundRecord.setOperateName(opName);
        refundRecord.setIsAgree(2);//不同意
        refundRecord.setOrderNo(orderNo);
        if (isReturnGoods == 1 || isReturnGoods == 2) {
            refundRecord.setRefundFlag(isReturnGoods);
        }
        refundRecord.setActionReason(reason);
        refundRecord.setAddTime(TimeUtils.getTimeStamp());
        refundRecordService.addRefundRecord(refundRecord);
        // 同步分账订单表, 核销之后的订单才能分账, 这样只要不核销订单, 就可以随时退款
        //businessService.updateBusinessOrderCheckStatus(request.getOrderNo());
        // 3. 恢复订单主状态: 拒绝并不产生真实退款, 该订单无其它待审核售后时从售后(5)恢复为申请前状态, 否则订单一直卡在售后
        orderInfoService.restoreOrderStatusAfterRefundReview(request.getOrderNo());

        // 添加日志, 消息类型: 1=系统消息2=内部消息3=业务消息
        Byte type = 2;
        String oper = "拒绝了";
        String content = opName + " " + oper + " 订单号(" + orderNo + ") 的退款订单。";
        messageService.addMiniLeaderMessageInfo(leaderId, opId, type, content);
    }

    //同意退款处理: 按本次申请金额发起退款, 主表refund_fee留待退款回调成功后累加
    public int handleAgree(Long leaderId, Long opId, String opName, GbOrderInfo orderInfo, OrderRefundInfoRequest request, String reason) {
        // 申请退款账户
        String merchantNo = orderInfo.getMerchantNo();
        String orderNo = orderInfo.getOrderNo();
        // 本次审核同意的退款金额(单位:分): 优先取审核请求回传的本次申请金额, 团长端旧版本未回传时按最近一笔申请记录兜底
        int agreeRefundCent = getApplyRefundCent(orderNo, request);
        // 按本次申请金额发起退款(支持部分退款); 申请金额缺失时退化为整单实付金额, 避免向易宝发起0元退款
        int requestRefundCent = agreeRefundCent > 0 ? agreeRefundCent
                : (orderInfo.getPayFee() == null ? 0 : orderInfo.getPayFee());
        if (requestRefundCent <= 0) {
            log.warn("退款发起终止: 订单{}本次可退金额为0", orderNo);
            return 0;
        }
        // 退款金额, 分转元
        double amount = MoneyUtil.centToYuan(requestRefundCent);
        Map<String, String> res = YeePayUtils.refund(merchantNo, orderNo, String.valueOf(amount));
        //插入交易流水表
        OrderTransactionLog transactionLog = new OrderTransactionLog();
        transactionLog.setOrderNo(orderNo);
        transactionLog.setTransactionNo("tr" + CustomIdGenerator.generateUUID());
        transactionLog.setPayAmount(amount);
        transactionLog.setPayMethod("yeePay");
        transactionLog.setOperatorId(opId);
        transactionLog.setOperatorName(opName);
        transactionLog.setAddTime(TimeUtils.getTimeStamp());
        transactionLog.setPayStatus(PaymentStatusEnum.REFUNDED.getCode());
        // 查看是否成功
        if (Integer.parseInt(res.get("success")) == 0) {
            log.error("退款失败：" + res.get("data"));
            transactionLog.setRemark("退款失败");
            handleInsertTransaction(transactionLog);
            return 0;
        } else {
            // 同步原始订单表和商户订单表的退款状态
            orderBusinessInfoService.editMiniLeaderOrderBusinessRefundStatus(orderNo);
            // 注: 主表退款金额refund_fee不在此维护, 申请/审核阶段都不累加;
            //     统一由退款回调成功后按易宝回传的实际退款金额累加(见 OrderRefundNotifyController#handleRefundResult)
            // 退款成功, 按实际退款数量回补库存(商品总库存 + SKU库存), 支持部分退款
            // 注: 商品行的退款/退货退款数量已在用户申请时累计占坑, 审核同意后保留不再重复累加
            List<GbOrderGoodsInfo> goodsList = orderInfoService.getOrderGoodsList(orderNo);
            for (GbOrderGoodsInfo goods : goodsList) {
                OrderRefundGoodsRequest refundGoods = request.getRefundGoodsMap().get(goods.getId());
                int refundNum = refundGoods != null && refundGoods.getRefundNum() != null ? refundGoods.getRefundNum() : 0;
                if (refundNum > 0) {
                    int packNum = goods.getPackNum() == null || goods.getPackNum() == 0 ? 1 : goods.getPackNum();
                    int stockNum = refundNum * packNum;
                    goodsService.increaseGoodsStock(goods.getGoodsId(), stockNum);
                    if (goods.getSkuId() != null && goods.getSkuId() > 0) {
                        skuService.increaseGoodsStock(goods.getSkuId(), stockNum);
                    }
                }
            }
            //查询该订单下是否有没有退款商品。如果没有，就改订单状态：退货，否则不改
            List<Long> orderGoodsIds = new ArrayList<>();
            for (Map.Entry<Long, OrderRefundGoodsRequest> entry : request.getRefundGoodsMap().entrySet()) {
                Long orderGoodsId = entry.getKey();
                orderGoodsIds.add(orderGoodsId);
            }
            //标识订单商品售后状态同意
            orderInfoService.updateOrderGoodsApplyStatus(orderNo, orderGoodsIds, 2);
            //插入退货记录售后日志
            GbOrderGoodsRefundRecord refundRecord = new GbOrderGoodsRefundRecord();
            refundRecord.setOperateId(opId);
            refundRecord.setOperateName(opName);
            refundRecord.setIsAgree(1);//同意退款
            refundRecord.setOrderNo(orderNo);
            refundRecord.setActionReason(reason);
            refundRecord.setAddTime(TimeUtils.getTimeStamp());
            refundRecordService.addRefundRecord(refundRecord);
            int flag = orderInfoService.getOrderGoodsStatus(orderNo);
            if (flag == 0) {
                //改订单商品状态 全部商品都退了为退款
                orderInfoService.editMiniLeaderRefundOrder(orderNo);
            } else {
                // 仅部分商品退款成功: 剩余商品继续正常流转, 订单从售后(5)恢复为申请前状态(该订单无其它待审核售后时)
                orderInfoService.restoreOrderStatusAfterRefundReview(orderNo);
            }
            // 同步分账订单表, 核销之后的订单才能分账, 这样只要不核销订单, 就可以随时退款
            businessService.updateBusinessOrderCheckStatus(request.getOrderNo());

            // 添加日志, 消息类型: 1=系统消息2=内部消息3=业务消息
            byte type = 2;
            String opString = "通过了";
            String content = opName + " " + opString + " 订单号(" + orderNo + ") 的退款订单。";
            messageService.addMiniLeaderMessageInfo(leaderId, opId, type, content);
            // 返回 成功标识
            transactionLog.setRemark("退款成功");
            handleInsertTransaction(transactionLog);
            return 1;
        }

    }

    // 计算本次审核(同意)对应的申请退款金额(单位:分): 优先取审核请求回传的本次申请金额;
    // 团长端旧版本未回传金额时, 按该订单最近一笔带金额的售后申请记录兜底
    private int getApplyRefundCent(String orderNo, OrderRefundInfoRequest request) {
        int refundCent = 0;
        if (request != null && !CollectionUtils.isEmpty(request.getRefundGoodsMap())) {
            for (OrderRefundGoodsRequest req : request.getRefundGoodsMap().values()) {
                if (req.getRefundAmount() != null && req.getRefundAmount() > 0) {
                    refundCent += MoneyUtil.yuanToCent(req.getRefundAmount());
                }
            }
        }
        if (refundCent <= 0) {
            List<GbOrderGoodsRefundRecord> recordList = refundRecordService.getRefundRecordListByOrderNo(orderNo);
            for (int i = recordList.size() - 1; i >= 0; i--) {
                GbOrderGoodsRefundRecord record = recordList.get(i);
                if (record.getRefundAmount() != null && record.getRefundAmount() > 0) {
                    refundCent = record.getRefundAmount();
                    break;
                }
            }
        }
        return refundCent;
    }

    private void handleInsertTransaction(OrderTransactionLog transactionLog) {
        transactionLogService.addTransactionLog(transactionLog);
    }

}
