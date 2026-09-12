package cn.com.shopgroup.controller;

import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.common.utils.MoneyUtil;
import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.common.wxmini.WxMiniAccessTokenHelper;
import cn.com.shopgroup.common.wxmini.WxMiniProgramHelper;
import cn.com.shopgroup.yeepay.YeePayUtils;
import cn.com.shopgroup.goods.service.GbGoodsInfoService;
import cn.com.shopgroup.goods.service.GbGoodsSkuInfoService;
import cn.com.shopgroup.order.model.GbOrderBusinessInfo;
import cn.com.shopgroup.order.model.GbOrderGoodsInfo;
import cn.com.shopgroup.order.service.GbOrderBusinessInfoService;
import cn.com.shopgroup.order.service.GbOrderCommissionInfoService;
import cn.com.shopgroup.order.service.GbOrderInfoService;
import cn.com.shopgroup.service.BusinessOrderService;
import cn.com.shopgroup.service.TaskOrderService;
import cn.com.shopgroup.user.service.GbArticleInfoService;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class TaskController {

    @Resource
    private BusinessOrderService service;

    @Resource
    private GbOrderBusinessInfoService orderBusinessInfoService;

    @Resource
    private GbOrderInfoService orderInfoService;

    @Resource
    private GbOrderCommissionInfoService commissionInfoService;

    @Resource
    private WxMiniAccessTokenHelper helper;

    @Resource
    private TaskOrderService taskOrderService;

    @Resource
    private GbGoodsInfoService goodsService;

    @Resource
    private GbGoodsSkuInfoService skuService;

    @Resource
    private GbArticleInfoService articleInfoService;


    // 微信订单发货
    @GetMapping("/task/order/send")
    public String send(@RequestParam("orderNo") String orderNo) {

        // 查询订单
        GbOrderBusinessInfo orderInfo = service.getOrderBusinessInfo(orderNo);
        log.info("/task/order/send orderNo:{},orderInfo:{}",orderNo, JSON.toJSONString(orderInfo));
        if (ObjectUtils.isEmpty(orderInfo)) {
            return "order is not exist";
        }
        // 是否已经发货
        if (orderInfo.getIsSend() == 1) return "order is already send";

        // 再获取访问令牌
        String accessToken = helper.getAccessToken(false);
        log.info("微信订单发货：accessToken = " + accessToken);

        // 订单发货参数
        String transactionId = orderInfo.getTransactionId();
        String goodsName = orderInfo.getGroupName();
        String openid = orderInfo.getOpenid();

        // 请求发货
        int isSendOK = WxMiniProgramHelper.uploadShippingInfo(accessToken, transactionId, goodsName, openid);
        if (isSendOK == 1) {
            // 更新发货标识
            service.updateBusinessOrderSendStatus(orderNo);
            return "ok";
        } else {
            // 考虑 accessToken 失效问题
            if (isSendOK == -1) helper.removeAccessToken();
            return "error";
        }
    }

    // 订单分账
    @GetMapping("/task/order/divide")
    public String divide(@RequestParam("orderNo") String orderNo) {

        // 先查询订单
        GbOrderBusinessInfo orderInfo = service.getOrderBusinessInfo(orderNo);
        log.info("/task/order/divide orderNo:{},orderInfo:{}",orderNo,JSON.toJSONString(orderInfo));
        if (ObjectUtils.isEmpty(orderInfo)) {
            return "order is not exist";
        }

        // 是否已经分账
        if (orderInfo.getIsDivide() == 1) {
            return "order is already divide";
        }

        // 分账商户
        String merchantNo = orderInfo.getMerchantNo();

        // 平台服务费分账
        String remark = "平台服务费";
        double amount = MoneyUtil.centToYuan(orderInfo.getServiceFee());

        // 商户自己分账
        String remark2 = "用户支付商品订单费用";
        Double amount2 = MoneyUtil.centToYuan(orderInfo.getBusFee());

        // 开始分账
        Map<String, String> res = YeePayUtils.divide(orderNo, merchantNo, amount, remark, amount2, remark2);

        // 分账结果
        if (Integer.parseInt(res.get("success")) == 0) {

            String uniqueDivideNo = res.containsKey("uniqueDivideNo") ? res.get("uniqueDivideNo") : "";
            log.info("订单分装失败：订单号 = " + orderNo + " , 易宝分账流水号 = " + uniqueDivideNo + " 原因：" + res.get("data"));
            return "error";
        } else {

            String status = res.get("data");
            String uniqueDivideNo = res.get("uniqueDivideNo");
            // CAS 更新成功才视为首次分账成功; 与定时任务同口径维护分账信息明细
            if (Boolean.TRUE.equals(service.updateBusinessOrderDivideStatus(orderNo, status, uniqueDivideNo))) {
                commissionInfoService.saveDivideCommissionInfo(orderInfo, uniqueDivideNo);
            } else {
                log.warn("/task/order/divide 订单分账状态未更新(已分账)：订单号 = " + orderNo);
            }
            return "ok";
        }
    }

    // 查询订单
    @GetMapping("/task/order/query")
    public String query(@RequestParam("orderNo") String orderNo) {
        // 先查询订单
        GbOrderBusinessInfo orderInfo = service.getOrderBusinessInfo(orderNo);
        if (ObjectUtils.isEmpty(orderInfo)) {
            return "order is not exist";
        }

        // 查询订单
        Map<String, String> res = YeePayUtils.query(orderNo, orderInfo.getMerchantNo());
        if (Integer.parseInt(res.get("success")) == 0) {
            log.warn("查询订单失败：订单号 = " + orderNo + " , 原因：" + res.get("data"));
            return "error";
        } else {
            // 解冻状态：INIT=处理中；FROZEN=已冻结；UN_FROZEN=已解冻
            String status = res.get("data");
            if (status.equalsIgnoreCase("UN_FROZEN")) {
                service.updateBusinessOrderFreezeStatus(orderNo);
            }
            return "ok-" + status;
        }
    }

    // 分账前部分退款，存在问题，实际到款金额-退款金额-平台服务费=商户分账金额。
    // 分账后部分退款的话，没有问题，子商户本来就自己承担平台服务费的差额。
    // 退款(原始订单金额, 包括易宝手续费)
    // 同步原始订单表和商户订单表的退款状态
    @GetMapping("/task/order/refund")
    public String refund(@RequestParam("orderNo") String orderNo) {

        // 先查询订单
        GbOrderBusinessInfo orderInfo = service.getOrderBusinessInfo(orderNo);
        if (orderInfo == null) return "order is not exist";

        // 是否已经退款
        if (orderInfo.getCommStatus() == 5) return "order is already refund";

        // 已经分账后还能退款嘛？
        //if(orderInfo.getIsDivide() == 1) return "order is already divide";

        // 申请退款
        String merchantNo = orderInfo.getMerchantNo();
        // 分转元
        double amount = MoneyUtil.centToYuan(orderInfo.getOrderFee());
        Map<String, String> res = YeePayUtils.refund(merchantNo, orderNo, String.valueOf(amount));

        // 查看是否成功
        if (Integer.parseInt(res.get("success")) == 0) {
            return "error - " + res.get("data");
        } else {
            // 同步原始订单表和商户订单表的退款状态
            orderBusinessInfoService.editMiniLeaderOrderBusinessRefundStatus(orderNo);
            orderInfoService.editMiniLeaderRefundOrder(orderNo);
            // 退款成功, 回补该订单全部商品库存(商品总库存 + SKU库存)
            List<GbOrderGoodsInfo> goodsList = orderInfoService.getOrderGoodsList(orderNo);
            for (GbOrderGoodsInfo goods : goodsList) {
                int packNum = goods.getPackNum() == null || goods.getPackNum() == 0 ? 1 : goods.getPackNum();
                int stockNum = goods.getGoodsNum() * packNum;
                goodsService.increaseGoodsStock(goods.getGoodsId(), stockNum);
                if (goods.getSkuId() != null && goods.getSkuId() > 0) {
                    skuService.increaseGoodsStock(goods.getSkuId(), stockNum);
                }
            }
            return "ok";
        }
    }

    // 提现
    @GetMapping("/task/order/cash")
    public String cash(@RequestParam("id") int id, @RequestParam("val") int val) {

        // 提现参数
        String merchantNo = "";
        String amount = "";
        String bankNo = "";

        if (id == 1) {
            // 商户提现
            merchantNo = "10093512255";
            amount = String.valueOf(val);
            bankNo = "6236680130003919525";
        } else {
            // 平台提现
            merchantNo = "10093508056";
            amount = String.valueOf(val);
            bankNo = "696443163";
        }

        // 申请提现
        Map<String, String> res = YeePayUtils.cash(merchantNo, amount, bankNo);

        // 结果
        if (Integer.parseInt(res.get("success")) == 0) {

            return "error - " + res.get("data");

        } else {

            // REQUEST_RECEIVE = 请求已接收
            // REQUEST_ACCEPT = 请求已受理
            // FAIL = 失败
            // REMITING = 银行正在处理中
            String status = res.get("status");
            return "ok - " + status;
        }
    }

    // 自动完成收货(手动触发, 与定时任务同一套逻辑): 已分账核销、核销满7天仍未完成收货的订单自动完成
    @GetMapping("/task/order/verify")
    public String verify() {

        // 当前时间
        int nowTime = TimeUtils.getTimeStamp();

        // 核销完成时间截止点(核销时间早于当前时间-7天)
        int verifyEndTime = nowTime - 7 * 24 * 60 * 60;

        // 先查询订单
        List<Map<String, String>> orderNos = taskOrderService.getUnReceiptOrderIds(verifyEndTime);
        // 在修改订单
        if (CollectionUtil.isNotEmpty(orderNos)) {
            for (Map<String, String> item : orderNos) {

                String orderNo = item.get("orderNo");
                taskOrderService.receiptOrder(orderNo, nowTime);
                log.info("自动完成收货============ orderNo = " + orderNo + ", receiptTime = " + nowTime);
            }
        }

        // 返回
        return "ok";
    }

    // 超时未支付订单自动取消并恢复库存(手动触发, 与定时任务同一套逻辑)
    @GetMapping("/task/order/backstock")
    public String backstock() {

        // 处理下单时间早于(当前时间-30分钟)的待支付订单: 状态改为已取消 + 恢复商品/SKU库存
        int time = TimeUtils.getTimeStamp() - 30 * 60;
        int cancelCount = taskOrderService.cancelTimeoutUnpaidOrderAndReturnStock(time, 100);
        log.info("手动触发超时未支付订单取消: 本次取消订单数=" + cancelCount);
        return "ok,cancelCount=" + cancelCount;
    }

    // 查询文章信息(联调测试接口)
    @GetMapping("/task/test/user")
    public JsonResult testUser(@RequestParam("aId") Long aId) {
        return JsonResult.success(articleInfoService.getMiniArticleInfo(aId));
    }


}
