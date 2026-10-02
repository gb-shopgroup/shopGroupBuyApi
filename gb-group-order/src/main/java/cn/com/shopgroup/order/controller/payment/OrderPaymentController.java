package cn.com.shopgroup.order.controller.payment;

import cn.com.shopgroup.common.cache.RedisConstant;
import cn.com.shopgroup.common.cache.RedisHelper;
import cn.com.shopgroup.common.exception.BusinessException;
import cn.com.shopgroup.common.utils.CustomIdGenerator;
import cn.com.shopgroup.common.utils.IpUtils;
import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.common.utils.MoneyUtil;
import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.common.wxmini.WxMiniAccessTokenHelper;
import cn.com.shopgroup.common.wxmini.WxMiniProgramHelper;
import cn.com.shopgroup.goods.service.GbGroupActivityInfoService;
import cn.com.shopgroup.order.constants.OrderStatusEnum;
import cn.com.shopgroup.order.constants.PaymentStatusEnum;
import cn.com.shopgroup.order.exception.OrderErrorCodeEnum;
import cn.com.shopgroup.order.model.GbLeaderMerchantMonthlyAmount;
import cn.com.shopgroup.order.model.GbOrderBusinessInfo;
import cn.com.shopgroup.order.model.GbOrderInfo;
import cn.com.shopgroup.order.model.OrderTransactionLog;
import cn.com.shopgroup.order.service.GbLeaderMerchantMonthlyAmountService;
import cn.com.shopgroup.order.service.GbOrderBusinessInfoService;
import cn.com.shopgroup.order.service.GbOrderInfoService;
import cn.com.shopgroup.order.service.OrderTransactionLogService;
import cn.com.shopgroup.user.model.GbOrgBusinessInfo;
import cn.com.shopgroup.user.model.GbOrgLeaderInfo;
import cn.com.shopgroup.user.service.GbOrgBusinessInfoService;
import cn.com.shopgroup.user.service.GbOrgLeaderInfoService;
import cn.com.shopgroup.yeepay.YeePayUtils;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.yeepay.yop.sdk.utils.DigitalEnvelopeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("order/payment")
@Slf4j
public class OrderPaymentController {

    @Resource
    private GbOrderInfoService orderInfoService;

    @Resource
    private GbOrderBusinessInfoService orderBusinessService;

    @Resource
    private RedisHelper redisHelper;
    @Resource
    private GbOrgBusinessInfoService businessService;
    @Resource
    private GbOrgLeaderInfoService leaderService;
    @Resource
    private GbGroupActivityInfoService groupService;
    @Resource
    private OrderTransactionLogService transactionLogService;
    @Resource
    private GbLeaderMerchantMonthlyAmountService merchantMonthlyAmountService;
    @Resource
    private WxMiniAccessTokenHelper tokenHelper;
    //订单支付时间30分钟，900秒；
    private static final int limitPayOrderTime = 1800;
    // 延迟调用微信发货的执行线程池, 避免阻塞支付回调
    private static final ScheduledExecutorService wxShipmentExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "wx-shipment-delay");
        thread.setDaemon(true);
        return thread;
    });

    // 发起支付
    @GetMapping("/order/pay")
    public JsonResult pay(@RequestParam("orderNo") String orderNo, @RequestParam("openid") String openid) {
        log.info("[订单支付/order/pay] params->orderNo:{},openid:{}", orderNo, openid);
        // 防刷
        String key = RedisConstant.RedisOrderPayKey + orderNo;
        if (redisHelper.hasKey(key)) {
            throw new BusinessException(OrderErrorCodeEnum.REPEAT_PAY_REQUEST);
        } else {
            redisHelper.setCacheObject(key, orderNo, RedisConstant.RedisOrderPayExpired, TimeUnit.SECONDS);
        }

        // 用户openid
        if (StringUtils.isEmpty(openid)) {
            throw new BusinessException(OrderErrorCodeEnum.OPENID_REQUIRED);
        }
        // 查询订单信息
        GbOrderInfo orderInfo = orderInfoService.getOrderInfoByOrderNo(orderNo);
        log.info("[order/pay] params->orderNo:{},getOrderInfo:{}", orderNo, JSON.toJSONString(orderInfo));
        if (ObjectUtils.isEmpty(orderInfo)) {
            throw new BusinessException(OrderErrorCodeEnum.ORDER_NOT_EXIST);
        }
        int nowTime = TimeUtils.getTimeStamp();
        int orderAddTime = orderInfo.getAddTime().intValue();
        if (nowTime - orderAddTime > limitPayOrderTime) {
            throw new BusinessException(OrderErrorCodeEnum.ORDER_TIMEOUT);
        }
        int orderStatus = orderInfo.getStatus().intValue();
        if (orderStatus != OrderStatusEnum.UNPAID.getCode()) {
            throw new BusinessException(OrderErrorCodeEnum.ONLY_UNPAID_PAYABLE);
        }
        // 订单价格和商品名称(团购名称)
        Double orderAmount = orderInfo.getOrderPrice();
        String goodsName = orderInfo.getGroupName();
        // 查询团长的收款账户
        Long leaderId = orderInfo.getLeaderId();
        GbOrgLeaderInfo leaderInfo = leaderService.getLeaderInfo(leaderId);
        if (ObjectUtils.isEmpty(leaderInfo)) {
            throw new BusinessException(OrderErrorCodeEnum.LEADER_INFO_ERROR);
        }
        List<GbOrgBusinessInfo> businessList = businessService.getMiniBusinessList(leaderId);
        log.info("支付时-查询团长下的账户信息,leaderId:{},businessList:{}", leaderId, JSON.toJSONString(businessList));
        if (CollectionUtils.isEmpty(businessList)) {
            throw new BusinessException(OrderErrorCodeEnum.LEADER_NO_ACCOUNT);
        }
        // 新算法
        GbLeaderMerchantMonthlyAmount merchantInfo = this.getLeaderMinMoneyMerchantInfo(leaderId, businessList, orderAmount);
        Long busId = merchantInfo.getBusId();
        String merchantNo = merchantInfo.getMerchantNo();

        // 把 收款账户busId 同步到订单表里面
        orderInfoService.miniBusinessOrder(orderNo, busId, merchantNo);

        // 客户IP地址
        String userIp = IpUtils.getClientIp();

        // 调用易宝支付
        Map<String, String> result = YeePayUtils.pay(merchantNo, openid, userIp, orderNo, goodsName, orderAmount);
        String success = result.get("success");
        String data = result.get("data");
        //插入交易流水表
        OrderTransactionLog transactionLog = new OrderTransactionLog();
        transactionLog.setTransactionNo("tr" + CustomIdGenerator.generateUUID());
        transactionLog.setOrderNo(orderNo);
        transactionLog.setPayAmount(orderAmount);
        transactionLog.setRemark("支付操作");
        transactionLog.setPayMethod("yeePay");
        transactionLog.setOperatorId(orderInfo.getMemberId());
        transactionLog.setOperatorName(openid);
        transactionLog.setAddTime(TimeUtils.getTimeStamp());
        if (Integer.parseInt(success) == 1) {
            transactionLog.setPayStatus(PaymentStatusEnum.PENDING.getCode());
            handleInsertTransaction(transactionLog);
            return JsonResult.success(JSONObject.parse(data));
        } else {
            transactionLog.setPayStatus(PaymentStatusEnum.FAILED.getCode());
            handleInsertTransaction(transactionLog);
            throw new BusinessException(OrderErrorCodeEnum.PAY_FAILED);
        }
    }

    private void handleInsertTransaction(OrderTransactionLog transactionLog) {
        transactionLogService.addTransactionLog(transactionLog);
    }

    /**
     * 排除受限账户
     *
     * <p>账户"当月累计收款金额 + 本次订单金额"超过设置的最高收款金额(limitAmount)时, 剔除该账户;
     * 当月无收款记录的账户(新商户/跨月首笔)视为累计收款 0, 参与额度判断。
     *
     * @param businessList 团长的收款账户列表
     * @param orderAmount  本次订单金额(单位: 元)
     * @return 未受额度限制的可用账户列表(每项含 leaderId / busId / merchantNo / totalAmount)
     */
    private List<GbLeaderMerchantMonthlyAmount> handleLimitAmount(List<GbOrgBusinessInfo> businessList, Double orderAmount) {
        log.info("支付时判断团长收款账户受限制情况,团长账户businessList:{}", JSON.toJSONString(businessList));
        List<GbLeaderMerchantMonthlyAmount> list = new ArrayList<>(businessList.size());
        // 订单金额(单位: 分)
        int orderFee = MoneyUtil.yuanToCent(orderAmount);
        String yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        for (GbOrgBusinessInfo business : businessList) {
            GbLeaderMerchantMonthlyAmount merchantInfo = merchantMonthlyAmountService.getByMerchantAndYearMonth(business.getLeaderId(), business.getCheckCustId(), yearMonth);
            if (ObjectUtils.isEmpty(merchantInfo)) {
                // 当月无收款记录: 构造累计收款 0 的内存对象(不落库), 保证后续限额判断与最小值比较口径统一
                merchantInfo = new GbLeaderMerchantMonthlyAmount();
                merchantInfo.setLeaderId(business.getLeaderId());
                merchantInfo.setBusId(business.getBusId());
                merchantInfo.setMerchantNo(business.getCheckCustId());
                merchantInfo.setYearMonth(yearMonth);
                merchantInfo.setTotalAmount(BigDecimal.ZERO);
            }
            if (isOverLimitAmount(merchantInfo, business, orderFee)) {
                // 超过最高收款金额, 剔除该账户
                log.info("支付时判断团长收款账户受限制账户信息:{}", JSON.toJSONString(business));
                continue;
            }
            list.add(merchantInfo);
        }
        log.info("支付时判断团长收款账户受限制情况,可用账户list:{}", JSON.toJSONString(list));
        return list;
    }

    /**
     * 判断账户加上本次订单金额后是否超过最高收款额度
     *
     * <p>比较式(与原有逻辑保持一致): (当月累计收款[分] + 订单金额[分]) * 100 > limitAmount * 10000;
     * 使用 long 运算, 避免商户累计收款金额较大时 int 溢出为负数, 导致受限账户被误放行。
     *
     * @param merchantInfo 商户月收入记录(totalAmount 为当月累计收款金额, 单位: 元)
     * @param business     收款账户(limitAmount 为最高收款金额, 单位: 万)
     * @param orderFee     本次订单金额(单位: 分)
     */
    private boolean isOverLimitAmount(GbLeaderMerchantMonthlyAmount merchantInfo, GbOrgBusinessInfo business, int orderFee) {
        // 商户当月累计收款金额: 数据库单位为元, 换算为分再参与比较; null 时按 0 处理
        BigDecimal totalYuan = merchantInfo.getTotalAmount() == null ? BigDecimal.ZERO : merchantInfo.getTotalAmount();
        int merchantMoney = totalYuan.movePointRight(2).setScale(0, RoundingMode.HALF_UP).intValue();
        // 最高收款金额, 未设置(null)时视为不限额, 避免拆箱 NPE 导致支付接口异常
        Integer limitAmount = business.getLimitAmount();
        if (limitAmount == null || limitAmount < 0) {
            return false;
        }
        return (merchantMoney + orderFee) * 100L > limitAmount * 10000L;
    }

    // 支付回调
    @PostMapping("/order/notify")
    public String notifyPay(HttpServletRequest req) {
        log.info("易宝支付回调-order/payment/order/notify ......");
        // 获取响应数据(密文)
        final String contentTypeStr = req.getContentType();
        log.info("易宝支付回调 - contentTypeStr: {}", contentTypeStr);
        if (!StringUtils.startsWith(contentTypeStr, "application/x-www-form-urlencoded")) {
            throw new IllegalArgumentException("RSA回调请求仅支持form格式");
        }
        // 加密签名后的业务数据
        String response = req.getParameter("response");
        log.info("易宝支付回调 - response: {}", response);
        // 解密成明文
        String plaintText = DigitalEnvelopeUtils.decrypt(response, "RSA2048");
        // 将明文转化为Json对象
        JSONObject jsonResponse = JSONObject.parse(plaintText);
        log.info("易宝支付回调 - jsonResponse: {}", JSON.toJSONString(jsonResponse));

        // 支付结果：SUCCESS（订单支付成功），FAIL（支付失败），TIME_OUT（订单过期）
        String status = "";
        if (jsonResponse.containsKey("status")) {
            status = jsonResponse.getString("status");
        }
        // 易宝支付回调明文 status = ：SUCCESS
        //log.error("易宝支付回调明文 status = ：" + status);

        // 支付失败的code码
        String failCode = "";
        if (jsonResponse.containsKey("failCode")) {
            failCode = jsonResponse.getString("failCode");
        }
        // 易宝支付回调明文 failCode = ：
        //log.error("易宝支付回调明文 failCode = ：" + failCode);

        // 支付失败的失败原因
        String failReason = "";
        if (jsonResponse.containsKey("failReason")) {
            failReason = jsonResponse.getString("failReason");
        }
        // 易宝支付回调明文 failReason = ：
        //log.error("易宝支付回调明文 failReason = ：" + failReason);

        // 支付失败
        if (failCode.trim().length() > 0 || failReason.trim().length() > 0) {
            log.error("易宝支付回调明文 failCode = ：" + failCode);
            log.error("易宝支付回调明文 failReason = ：" + failReason);
            log.error("易宝支付回调明文：" + plaintText);
            return "fail";
        }
        if (status.equalsIgnoreCase("SUCCESS") == false) {
            log.error("易宝支付回调明文 status = ：" + status);
            log.error("易宝支付回调明文：" + plaintText);
            return "fail";
        }

        // 我们自己的订单好
        String orderNo = "";
        if (jsonResponse.containsKey("orderId")) {
            orderNo = jsonResponse.getString("orderId");
        }

        // 该笔订单在微信支付宝侧的唯一订单号，微信交易单号/支付宝订单号
        String channelTrxId = "";
        if (jsonResponse.containsKey("channelTrxId")) {
            channelTrxId = jsonResponse.getString("channelTrxId");
        }
        // 易宝支付回调明文 channelTrxId = ：4500000210202606027912134899
        //log.error("易宝支付回调明文 channelTrxId = ：" + channelTrxId);

        // 支付金额，元
        String payAmount = "";
        if (jsonResponse.containsKey("payAmount")) {
            payAmount = jsonResponse.getString("payAmount");
        }
        // 易宝支付回调明文 payAmount = ：0.10
        //log.error("易宝支付回调明文 payAmount = ：" + payAmount);

        // 修改订单记录, 添加支付完成标识, 同时元转分操作
        int payPrice = MoneyUtil.yuanToCent(Double.parseDouble(payAmount));
        log.info("支付回调更新订单信息......");
        orderInfoService.miniPayOrder(orderNo, channelTrxId, payPrice);
        // 订单查询填充”订单收款账户信息表“ 数据表
        GbOrderInfo orderInfo = orderInfoService.getOrderInfoByOrderNo(orderNo);
        log.info("易v宝支付回调-orderNo:{},orderInfo:{}", orderNo, JSON.toJSONString(orderInfo));
        if (ObjectUtils.isEmpty(orderInfo)) {
            return "fail";
        }
        // 写入”订单收款账户信息表“ 数据表, 主要是分账金额的计算, 后期直接定时任务分账即可
        GbOrderBusinessInfo orderBusinessInfo = new GbOrderBusinessInfo();
        // 订单id,主键
        orderBusinessInfo.setOrderNo(orderNo);
        // 团长id,外键
        orderBusinessInfo.setLeaderId(orderInfo.getLeaderId());
        // 账户id,外键
        orderBusinessInfo.setBusId(orderInfo.getBusId());
        // 易宝商户编号,冗余
        orderBusinessInfo.setMerchantNo(orderInfo.getMerchantNo());
        // 团购名称, 冗余
        orderBusinessInfo.setGroupName(orderInfo.getGroupName());
        // 订单编号,冗余
        orderBusinessInfo.setOrderSn(orderNo);
        // 微信订单号，用于发货
        orderBusinessInfo.setTransactionId(channelTrxId);
        // openid,微信发货
        orderBusinessInfo.setOpenid(orderInfo.getOpenid());

        // 计算订单分账金额
        this.calculateOrderDivideMoney(orderBusinessInfo, orderInfo.getOrderPrice(), payPrice);

        // 添加时间(下单时间)
        orderBusinessInfo.setAddTime(orderInfo.getAddTime());
        orderBusinessService.addMiniLeaderOrderBusiness(orderBusinessInfo);

        //插入交易流水表
        OrderTransactionLog transactionLog = new OrderTransactionLog();
        transactionLog.setOrderNo(orderNo);
        transactionLog.setTransactionNo("tr" + CustomIdGenerator.generateUUID());
        transactionLog.setPayAmount(Double.parseDouble(payAmount));
        transactionLog.setRemark("支付回调操作");
        transactionLog.setPayMethod("yeePay");
        transactionLog.setOperatorName("支付回调");
        transactionLog.setAddTime(TimeUtils.getTimeStamp());
        transactionLog.setPayStatus(PaymentStatusEnum.SUCCESS.getCode());
        handleInsertTransaction(transactionLog);
        // 累加商户月收款金额(gb_leader_merchant_monthly_amount 表, 替代原 Redis 月度 ZSet)
        int merchantMoney = MoneyUtil.yuanToCent(orderInfo.getOrderPrice());
        saveMerchantMonthlyAmount(orderInfo, merchantMoney);
        // 累加团购订单数量
        groupService.addGroupOrderNumber(orderInfo.getGroupId());
        delRedisKey(orderInfo.getGroupId());
        //订单对应的团长的cashType[结算到账方式,0=支付时延迟到账型,1=核销时延迟到账型]
        handleWxUploadShippingInfo(orderInfo, channelTrxId);
        // 返回
        return "success";
    }

    /**
     * 商户月收款金额落库累计(gb_leader_merchant_monthly_amount 表)
     *
     * <p>替代原 Redis 月度 ZSet(merchantzset:{leaderId}:{yyyyMM}, 仅保留 30 天、跨月即清零)的商户月收款统计;
     * ON DUPLICATE KEY UPDATE 原子累加, 并发回调安全; 落库失败仅记日志, 不影响支付回调主流程。
     *
     * @param orderInfo         已支付订单(取 leaderId/busId/merchantNo)
     * @param merchantMoneyCent 本次收款金额(单位: 分)
     */
    private void saveMerchantMonthlyAmount(GbOrderInfo orderInfo, int merchantMoneyCent) {
        if (StringUtils.isEmpty(orderInfo.getMerchantNo())) {
            log.warn("商户月收款金额落库跳过, 订单缺少商户号, orderNo:{}", orderInfo.getOrderNo());
            return;
        }
        try {
            String yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            BigDecimal amount = BigDecimal.valueOf(merchantMoneyCent).movePointLeft(2).setScale(2, RoundingMode.HALF_UP);
            merchantMonthlyAmountService.addTotalAmount(orderInfo.getLeaderId(), orderInfo.getBusId(), orderInfo.getMerchantNo(), yearMonth, amount);
        } catch (Exception e) {
            log.error("商户月收款金额落库失败, orderNo:{}, merchantNo:{}, amountCent:{}",
                    orderInfo.getOrderNo(), orderInfo.getMerchantNo(), merchantMoneyCent, e);
        }
    }

    private void delRedisKey(Long groupId) {
        String key1 = RedisConstant.RedisGroupLogsKey2 + groupId;
        redisHelper.releaseLock(key1);
        String key = RedisConstant.RedisGroupLogsKey + groupId;
        redisHelper.releaseLock(key);
    }

    public void handleWxUploadShippingInfo(GbOrderInfo orderInfo, String transactionId) {
        GbOrgLeaderInfo leaderInfo = leaderService.getLeaderInfo(orderInfo.getLeaderId());
        if (ObjectUtils.isEmpty(leaderInfo)) {
            return;
        }
        int type = leaderInfo.getCashType().intValue();
        // 团长结算到账方式: 0=支付时延迟到账型, 1=核销时延迟到账型
        // type=0 时延迟15秒再调用微信发货(上传发货信息+标记已调用微信发货), 等微信侧发货状态生效, 同时避免阻塞易宝支付回调请求
        if (type == 0) {
            log.info("支付-易宝支付回调---10分钟后再调用微信发货......");
            String orderNo = orderInfo.getOrderNo();
            wxShipmentExecutor.schedule(() -> {
                try {
                    // 首次调用(使用缓存access_token)
                    int wxFlag = callWxUploadShippingInfo(orderInfo, transactionId, false);
                    // access_token失效(微信返回40001/42001, 对应-1)时, 清除缓存并强制刷新后重试一次
                    if (wxFlag == -1) {
                        tokenHelper.removeAccessToken();
                        log.warn("[微信发货]access_token失效, 清除缓存并强制刷新后重试, orderNo:{}", orderNo);
                        wxFlag = callWxUploadShippingInfo(orderInfo, transactionId, true);
                    }
                    // 微信发货调用成功, 标记订单已调用微信发货(wx_shipment:0=未调用,1=已调用)
                    if (wxFlag == 1) {
                        orderInfoService.updateWxShipment(orderNo);
                        orderBusinessService.updateBusinessOrderSendStatus(orderNo);
                        log.info("[微信发货]延迟调用微信发货成功, orderNo:{}", orderNo);
                    } else {
                        log.warn("[微信发货]延迟调用微信发货失败, orderNo:{}, wxFlag:{}", orderNo, wxFlag);
                    }
                } catch (Exception e) {
                    log.error("[微信发货]延迟调用微信发货异常, orderNo:{}", orderNo, e);
                }
            }, 10, TimeUnit.MINUTES);
        }
    }

    // 调用微信发货信息录入(可强制刷新access_token), 返回: 1=成功, 0=业务失败, -1=access_token失效(40001/42001)
    private int callWxUploadShippingInfo(GbOrderInfo orderInfo, String transactionId, boolean forceRefreshToken) {
        // forceRefreshToken=true 时忽略Redis缓存, 强制向微信重新获取access_token
        String accessToken = tokenHelper.getAccessToken(forceRefreshToken);
        if (StringUtils.isEmpty(accessToken)) {
            log.warn("[微信发货]获取access_token失败, orderNo:{}", orderInfo.getOrderNo());
            return -1;
        }
        return WxMiniProgramHelper.uploadShippingInfo(accessToken, transactionId, orderInfo.getGroupName(), orderInfo.getOpenid());
    }

    // 新的轮询算法: 先剔除超过最高收款额度的受限账户, 再取当月累计收款金额最小的账户
    private GbLeaderMerchantMonthlyAmount getLeaderMinMoneyMerchantInfo(Long leaderId, List<GbOrgBusinessInfo> businessInfoList, Double orderAmount) {

        if (CollectionUtils.isEmpty(businessInfoList)) {
            throw new BusinessException(OrderErrorCodeEnum.LEADER_NO_ACCOUNT);
        }
        // 1. 剔除受限账户
        List<GbLeaderMerchantMonthlyAmount> availableList = handleLimitAmount(businessInfoList, orderAmount);
        if (CollectionUtils.isEmpty(availableList)) {
            // 全部账户都超过最高收款额度, 暂时无法支付
            throw new BusinessException(OrderErrorCodeEnum.ACCOUNT_LIMIT_EXCEEDED);
        }

        // 2. 取当月累计收款金额最小的账户(收款最少的优先收款, 各账户月收入趋于均衡; totalAmount 为 null 时按 0 处理)
        GbLeaderMerchantMonthlyAmount minMerchant = null;
        BigDecimal minTotal = null;
        for (GbLeaderMerchantMonthlyAmount item : availableList) {
            BigDecimal total = item.getTotalAmount() == null ? BigDecimal.ZERO : item.getTotalAmount();
            if (minMerchant == null || total.compareTo(minTotal) < 0) {
                minMerchant = item;
                minTotal = total;
            }
        }
        log.info("支付时选择当月收款金额最小的账户, leaderId:{}, busId:{}, merchantNo:{}, 当月累计收款:{}元",
                leaderId, minMerchant.getBusId(), minMerchant.getMerchantNo(), minTotal);
        return minMerchant;
    }

    // 计算订单分账金额算法
    private void calculateOrderDivideMoney(GbOrderBusinessInfo orderBusinessInfo, double orderPrice, int payPrice) {

        // 如果用户支付9块钱的话，总计手续费是 9 * 0.006 = 5.4分，四舍五入的话，就是5分钱（对团长而言），易宝手续费 9 * 0.003 = 2.7分，四舍五入就是易宝3分，平台服务费就是5-3=2分。易宝薅平台四舍五入的1分羊毛。
        // 如果用户支付8块钱的话，总计手续费是 8 * 0.006 = 4.8分，四舍五入的话，就是5分钱（对团长而言），易宝手续费 8 * 0.003 = 2.4分，四舍五入就是易宝2分，平台服务器就是5-2=3分。平台薅易宝四舍五入的1分羊毛。
        // 如果用户支付7块钱的话，总计手续费是 7 * 0.006 = 4.2分，四舍五入的话，就是4分钱（对团长而言），易宝手续费 7 * 0.003 = 2.1分，四舍五入就是易宝2分，平台服务器就是4-2=2分。大家相互公平。
        // 获取商户手续费
        GbOrgLeaderInfo leaderInfo = leaderService.getLeaderInfo(orderBusinessInfo.getLeaderId());
        int rate = leaderInfo.getCommission(); // 数值为：3，4，5，6，7，8，9，10

        // “团长”的手续费(0.6% - 1.0%)
        int all_shouxufei = MoneyUtil.calcRateByCent(payPrice, rate);
        // “支付平台(易宝)”的手续费(0.3%)
        int yibao_shouxufei = MoneyUtil.calcRateByCent(payPrice, 3);
        // "平台"的手续费：all_shouxufei - yibao_shouxufei
        int platform_shouxufei = all_shouxufei - yibao_shouxufei;
        if (platform_shouxufei <= 0) platform_shouxufei = 0;

        // 订单金额(单位：分)
        int orderFee = MoneyUtil.yuanToCent(orderPrice);
        orderBusinessInfo.setOrderFee(orderFee);
        // 实际到账金额(单位：分) = 订单支付金额 - 易宝手续费
        int receivedFee = payPrice - yibao_shouxufei;
        orderBusinessInfo.setReceivedFee(receivedFee);
        // 我们平台的服务费(单位：分)
        orderBusinessInfo.setServiceFee(platform_shouxufei);
        // 其他佣金(单位：分)
        orderBusinessInfo.setOtherFee(0);
        // 子商户分账金额(单位：分)
        int busFee = payPrice - all_shouxufei;
        if (busFee < 0) busFee = 0;
        orderBusinessInfo.setBusFee(busFee);
    }

}
