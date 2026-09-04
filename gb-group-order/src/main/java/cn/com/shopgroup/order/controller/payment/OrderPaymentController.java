package cn.com.shopgroup.order.controller.payment;

import cn.com.shopgroup.common.cache.RedisConstant;
import cn.com.shopgroup.common.cache.RedisHelper;
import cn.com.shopgroup.common.merchant.MerchantInfo;
import cn.com.shopgroup.common.merchant.MerchantService;
import cn.com.shopgroup.common.utils.CustomIdGenerator;
import cn.com.shopgroup.common.utils.IpUtils;
import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.common.utils.MoneyUtil;
import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.common.wxmini.WxMiniAccessTokenHelper;
import cn.com.shopgroup.common.wxmini.WxMiniProgramHelper;
import cn.com.shopgroup.common.yeepay.YeepayConfig;
import cn.com.shopgroup.common.yeepay.YeepayUtils;
import cn.com.shopgroup.goods.service.GbGroupActivityInfoService;
import cn.com.shopgroup.order.constants.OrderStatusEnum;
import cn.com.shopgroup.order.constants.PaymentStatusEnum;
import cn.com.shopgroup.order.model.GbOrderBusinessInfo;
import cn.com.shopgroup.order.model.GbOrderInfo;
import cn.com.shopgroup.order.model.OrderTransactionLog;
import cn.com.shopgroup.order.service.GbOrderBusinessInfoService;
import cn.com.shopgroup.order.service.GbOrderInfoService;
import cn.com.shopgroup.order.service.OrderTransactionLogService;
import cn.com.shopgroup.user.model.GbOrgBusinessInfo;
import cn.com.shopgroup.user.model.GbOrgLeaderInfo;
import cn.com.shopgroup.user.service.GbOrgBusinessInfoService;
import cn.com.shopgroup.user.service.GbOrgLeaderInfoService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
    private MerchantService merchantService;

    @Resource
    private GbOrgBusinessInfoService businessService;
    @Resource
    private GbOrgLeaderInfoService leaderService;
    @Resource
    private GbGroupActivityInfoService groupService;
    @Resource
    private OrderTransactionLogService transactionLogService;
    // 支付回调
    @Resource
    private YeepayConfig yeepayConfig;
    @Resource
    private WxMiniAccessTokenHelper tokenHelper;
    //订单支付时间 15分钟，900秒；
    private static final int limitPayOrderTime = 900;

    // 发起支付
    @GetMapping("/order/pay")
    public JsonResult pay(@RequestParam("orderNo") String orderNo, @RequestParam("openid") String openid) {
        log.info("[订单支付/order/pay] params->orderNo:{},openid:{}", orderNo, openid);
        // 防刷
        String key = RedisConstant.RedisOrderPayKey + orderNo;
        if (redisHelper.hasKey(key)) {
            return JsonResult.fail("不要重复发起支付");
        } else {
            redisHelper.setCacheObject(key, orderNo, RedisConstant.RedisOrderPayExpired, TimeUnit.SECONDS);
        }

        // 用户openid
        if (StringUtils.isEmpty(openid)) {
            return JsonResult.fail("openid不存在");
        }
        // 查询订单信息
        GbOrderInfo orderInfo = orderInfoService.getOrderInfoByOrderNo(orderNo);
        log.info("[order/pay] params->orderNo:{},getOrderInfo:{}", orderNo, JSON.toJSONString(orderInfo));
        if (ObjectUtils.isEmpty(orderInfo)) {
            return JsonResult.fail("订单不存在");
        }
        int nowTime = TimeUtils.getTimeStamp();
        int orderAddTime = orderInfo.getAddTime().intValue();
        if (nowTime - orderAddTime > limitPayOrderTime) {
            return JsonResult.fail("订单已超时不能支付");
        }
        int orderStatus = orderInfo.getStatus().intValue();
        if (orderStatus != OrderStatusEnum.UNPAID.getCode()) {
            return JsonResult.fail("只有待支付订单才能支付");
        }
        // 订单价格和商品名称(团购名称)
        Double orderAmount = orderInfo.getOrderPrice();
        String goodsName = orderInfo.getGroupName();
        // 查询团长的收款账户
        Long leaderId = orderInfo.getLeaderId();
        GbOrgLeaderInfo leaderInfo = leaderService.getLeaderInfo(leaderId);
        if (ObjectUtils.isEmpty(leaderInfo)) {
            return JsonResult.fail("团长信息有误");
        }
        List<GbOrgBusinessInfo> businessList = businessService.getMiniBusinessList(leaderId);
        if (CollectionUtils.isEmpty(businessList)) {
            return JsonResult.fail("该团长没有收款账户");
        }
        /**
         * 去掉账户限制最多收款额度
         */
        List<GbOrgBusinessInfo> businessInfoList = handleLimitAmount(businessList, orderAmount);
        if (CollectionUtils.isEmpty(businessInfoList)) {
            return JsonResult.fail("该团长下的收款账户收款额度已经全部受限制，暂时不能支付");
        }
        // 新算法
        MerchantInfo merchantInfo = this.getLeaderMinMoneyMerchantInfo(leaderId, businessInfoList);
        Long busId = merchantInfo.getBusId();
        String merchantNo = merchantInfo.getMerchantNo();

        // 把 收款账户busId 同步到订单表里面
        orderInfoService.miniBusinessOrder(orderNo, busId, merchantNo);

        // 客户IP地址
        String userIp = IpUtils.getClientIp();

        // 调用易宝支付
        String notifyUrl = yeepayConfig.getUrl();
        Map<String, String> result = YeepayUtils.pay(notifyUrl, merchantNo, openid, userIp, orderNo, goodsName, orderAmount);
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
            return JsonResult.fail();
        }
    }

    private void handleInsertTransaction(OrderTransactionLog transactionLog) {
        transactionLogService.addTransactionLog(transactionLog);
    }

    /**
     * 排除受限账户
     *
     * @param businessList
     * @return
     */
    private List<GbOrgBusinessInfo> handleLimitAmount(List<GbOrgBusinessInfo> businessList, Double orderAmount) {
        List<GbOrgBusinessInfo> list = new ArrayList<>();
        //double (元-> 分)
        int orderFee = MoneyUtil.yuanToCent(orderAmount);
        for (GbOrgBusinessInfo gbs : businessList) {
            MerchantInfo meInfo = merchantService.getLeaderMerchantInfo(gbs.getLeaderId(), gbs.getBusId());
            if (!ObjectUtils.isEmpty(meInfo)) {
                //如果账户超过设置最高收款金额，剔除
                if ((meInfo.getMoney() + orderFee) * 100 > gbs.getLimitAmount() * 10000) {
                    continue;
                }
            }
            list.add(gbs);
        }
        return list;
    }

    // 支付回调
    @PostMapping("/order/notify")
    public String notifyPay(HttpServletRequest req) {

        // 获取响应数据(密文)
        final String contentTypeStr = req.getContentType();
        if (!StringUtils.startsWith(contentTypeStr, "application/x-www-form-urlencoded")) {
            throw new IllegalArgumentException("RSA回调请求仅支持form格式");
        }
        // 加密签名后的业务数据
        String response = req.getParameter("response");
        // 解密成明文
        String plaintText = DigitalEnvelopeUtils.decrypt(response, "RSA2048");
        // 将明文转化为Json对象
        JSONObject jsonResponse = JSONObject.parse(plaintText);

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
        orderInfoService.miniPayOrder(orderNo, channelTrxId, payPrice);

        // 订单查询填充”订单收款账户信息表“ 数据表
        GbOrderInfo orderInfo = orderInfoService.getOrderInfoByOrderNo(orderNo);

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
        // 累加商户收款金额
        int merchantMoney = MoneyUtil.yuanToCent(orderInfo.getOrderPrice());
        merchantService.addMerchantMoney(orderInfo.getLeaderId(), orderInfo.getBusId(), merchantMoney);
        // 累加团购订单数量
        groupService.addGroupOrderNumber(orderInfo.getGroupId());

        // 累加Redis订单数量
        String key = RedisConstant.RedisOrderTotalKey + orderInfo.getGroupId();
        redisHelper.increment(key);
        //订单对应的团长的cashType[结算到账方式,0=支付时延迟到账型,1=核销时延迟到账型]
        handleWxUploadShippingInfo(orderInfo, channelTrxId);
        // 返回
        return "success";
    }

    public void handleWxUploadShippingInfo(GbOrderInfo orderInfo, String transactionId) {
        GbOrgLeaderInfo leaderInfo = leaderService.getLeaderInfo(orderInfo.getLeaderId());
        if (ObjectUtils.isEmpty(leaderInfo)) {
            return;
        }
        int type = leaderInfo.getCashType().intValue();
        if (type == 0) {
            String accessToken = tokenHelper.getAccessToken(false);
            String name = orderInfo.getGroupName();
            int wxFlag = WxMiniProgramHelper.uploadShippingInfo(accessToken, transactionId, name, orderInfo.getOpenid());
            // 微信发货调用成功, 标记订单已调用微信发货(wx_shipment:0=未调用,1=已调用)
            if (wxFlag == 1) {
                orderInfoService.updateWxShipment(orderInfo.getOrderNo());
            }
        }
    }

    // 新的轮询算法: 按照收款金额从小到大排列, 取第一个即可
    private MerchantInfo getLeaderMinMoneyMerchantInfo(Long leaderId, List<GbOrgBusinessInfo> businessInfoList) {

        // 就一个收款账户, 直接返回
        if (businessInfoList.size() == 1) {
            Long tempId = businessInfoList.get(0).getBusId();
            String tempNo = businessInfoList.get(0).getCheckCustId();
            return new MerchantInfo(tempId, tempNo, 0);
        }

        // 初始化Redis信息
        for (GbOrgBusinessInfo item : businessInfoList) {
            if (item.getIsClose() == 0) {
                // 初始化商户和收款金额
                merchantService.initMerchantInfo(item.getBusId(), item.getCheckCustId());
                merchantService.initMerchantMoney(leaderId, item.getBusId(), 0);
            } else {
                // 考虑关闭某个商户的情况
                merchantService.removeMerchantMoney(leaderId, item.getBusId());
            }
        }

        // 获取最小收款商户金额
        return merchantService.getLeaderMonthMinMoneyMerchantInfo(leaderId);
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

    // 测试分账算法和轮询算法
    //@GetMapping("/order/order/test")
    public String test() {

        Long leaderId = 1l;
        List<GbOrgBusinessInfo> businessInfoList = new ArrayList<>();
        GbOrgBusinessInfo temp01 = new GbOrgBusinessInfo();
        temp01.setBusId(10l);
        temp01.setLeaderId(leaderId);
        temp01.setCheckCustId("9001");
        businessInfoList.add(temp01);
        GbOrgBusinessInfo temp02 = new GbOrgBusinessInfo();
        temp02.setBusId(11l);
        temp02.setLeaderId(leaderId);
        temp02.setCheckCustId("9002");
        businessInfoList.add(temp02);
        GbOrgBusinessInfo temp03 = new GbOrgBusinessInfo();
        temp03.setBusId(12l);
        temp03.setLeaderId(leaderId);
        temp03.setCheckCustId("9003");
        businessInfoList.add(temp03);

        // 随机一个待支付金额
        Random random = new Random();
        double raw = 7 + random.nextDouble() * 2;
        BigDecimal decimal = new BigDecimal(raw);
        double orderPrice = decimal.setScale(2, RoundingMode.HALF_UP).doubleValue();
        System.out.println("本次支付金额 orderPrice = " + orderPrice + " 元");

        // 获取收款商户
        MerchantInfo merchantInfo = this.getLeaderMinMoneyMerchantInfo(leaderId, businessInfoList);
        System.out.println("本次收款账户 busId = " + merchantInfo.getBusId());
        System.out.println("本次收款账户 merchantNo = " + merchantInfo.getMerchantNo());

        // 计算分账金额
        GbOrderBusinessInfo orderBusinessInfo = new GbOrderBusinessInfo();
        orderBusinessInfo.setLeaderId(leaderId);
        int payPrice = MoneyUtil.yuanToCent(orderPrice);
        this.calculateOrderDivideMoney(orderBusinessInfo, orderPrice, payPrice);
        System.out.println("计算分账金额--订单金额 orderFee = " + MoneyUtil.centToYuan(orderBusinessInfo.getOrderFee()) + " 元");
        System.out.println("计算分账金额--实到金额 receivedFee = " + MoneyUtil.centToYuan(orderBusinessInfo.getReceivedFee()) + " 元");
        System.out.println("计算分账金额--平台抽成 serviceFee = " + MoneyUtil.centToYuan(orderBusinessInfo.getServiceFee()) + " 元");
        System.out.println("计算分账金额--商户分账 busFee = " + MoneyUtil.centToYuan(orderBusinessInfo.getBusFee()) + " 元");

        // 累加商户收款金额
        int merchantMoney = MoneyUtil.yuanToCent(orderPrice);
        merchantService.addMerchantMoney(leaderId, merchantInfo.getBusId(), merchantMoney);

        // 查询Redis缓存信息
        for (GbOrgBusinessInfo temp : businessInfoList) {

            MerchantInfo info = merchantService.getLeaderMerchantInfo(leaderId, temp.getBusId());
            System.out.println("Redis缓存 busId = " + info.getBusId());
            System.out.println("Redis缓存 merchantNo = " + info.getMerchantNo());
            System.out.println("Redis缓存 busMoney = " + MoneyUtil.centToYuan(info.getMoney()) + " 元");
        }

        return "ok";
    }


}
