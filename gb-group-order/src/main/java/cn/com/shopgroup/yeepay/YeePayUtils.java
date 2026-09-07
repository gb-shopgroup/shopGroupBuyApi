package cn.com.shopgroup.yeepay;

import com.alibaba.fastjson2.JSON;
import com.yeepay.yop.sdk.service.common.YopClient;
import com.yeepay.yop.sdk.service.common.YopClientBuilder;
import com.yeepay.yop.sdk.service.common.request.YopRequest;
import com.yeepay.yop.sdk.service.common.response.YopResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class YeePayUtils {


    private static final YopClient client = YopClientBuilder.builder().build();

    // 以下支付参数: 原为写死的常量, 现已改为读取不同环境配置文件(pay-config, 见 application-dev/test/prod.yml),
    // 由 Spring 启动时经 YeePayConfig.init() 注入; 此处保留原值仅作兜底默认
    private static String parentMerchantNo;


    private static String WxAppId;


    private static String PayNotifyUrl;


    private static String RefundNotifyUrl;


    private static String MerNotifyUrl;

    // Spring 启动时调用(YeePayConfig.init): 将当前激活环境 yml 中 pay-config 的值注入, 配置了才覆盖默认值
    public static void init(YeePayConfig config) {

        if (config == null) {
            return;
        }
        if (config.getParentMerchantNo() != null && config.getParentMerchantNo().length() > 0) {
            parentMerchantNo = config.getParentMerchantNo();
        }
        if (config.getWxAppId() != null && config.getWxAppId().length() > 0) {
            WxAppId = config.getWxAppId();
        }
        if (config.getPayNotifyUrl() != null && config.getPayNotifyUrl().length() > 0) {
            PayNotifyUrl = config.getPayNotifyUrl();
        }
        if (config.getRefundNotifyUrl() != null && config.getRefundNotifyUrl().length() > 0) {
            RefundNotifyUrl = config.getRefundNotifyUrl();
        }
        if (config.getMerNotifyUrl() != null && config.getMerNotifyUrl().length() > 0) {
            MerNotifyUrl = config.getMerNotifyUrl();
        }
    }


    public static Map<String, String> pay(String merchantNo, String openid, String userIp, String orderNo, String goodsName, double orderAmount) {
        Map<String, String> result = new HashMap<>();
        result.put("success", "0");
        result.put("data", "");
        YopRequest request = new YopRequest("/rest/v1.0/aggpay/pre-pay", "POST");
        request.addParameter("payWay", "MINI_PROGRAM");
        request.addParameter("channel", "WECHAT");
        request.addParameter("parentMerchantNo", parentMerchantNo);
        request.addParameter("merchantNo", merchantNo);
        request.addParameter("orderId", orderNo);
        request.addParameter("orderAmount", String.valueOf(orderAmount));
        request.addParameter("notifyUrl", PayNotifyUrl);
        request.addParameter("scene", "OFFLINE");
        request.addParameter("appId", WxAppId);
        request.addParameter("userId", openid);
        request.addParameter("userIp", userIp);
        request.addParameter("goodsName", goodsName);
        request.addParameter("fundProcessType", "DELAY_SETTLE");
        try {
            log.info("调用易宝支付-支付-请求参数,request:{}", JSON.toJSONString(request));
            YopResponse response = client.request(request);
            log.info("调用易宝支付-支付-返回值,response:{}", JSON.toJSONString(response));
            Map<String, String> res = (Map) JSON.parse(response.getStringResult());
            String code = res.get("code");
            String message = res.get("message");
            if (code.equalsIgnoreCase("00000")) {
                result.put("success", "1");
                String prePayTn = res.get("prePayTn");
                result.put("data", prePayTn);
            } else {
                result.put("data", message);
            }
            log.error("result:{}", response.getResult());
            log.error("YopRequestId:{}", response.getMetadata().getYopRequestId());
        } catch (Exception ex) {
            result.put("data", ex.getMessage());
            log.error("Exception when calling, ex:", ex);
        }
        log.info("调用易宝支付-支付-返回后系统处理返回result:{}", JSON.toJSONString(result));
        return result;
    }


    public static Map<String, String> query(String orderId, String merchantNo) {
        Map<String, String> result = new HashMap<>();
        result.put("success", "0");
        result.put("data", "");
        YopRequest request = new YopRequest("/rest/v1.0/trade/order/query", "GET");
        request.addParameter("parentMerchantNo", parentMerchantNo);
        request.addParameter("merchantNo", merchantNo);
        request.addParameter("orderId", orderId);
        try {
            log.info("调用易宝支付-查询订单-请求参数,request:{}", JSON.toJSONString(request));
            YopResponse response = client.request(request);
            log.info("调用易宝支付-查询订单-返回值,response:{}", JSON.toJSONString(response));
            Map<String, String> res = (Map) JSON.parse(response.getStringResult());
            String code = res.get("code");
            String message = res.get("message");
            if (code.equalsIgnoreCase("OPR00000")) {
                String status = res.get("status");
                String fundControlCsStatus = res.get("fundControlCsStatus");
                String csUnFrozenCompleteDate = res.containsValue("csUnFrozenCompleteDate") ? res.get("csUnFrozenCompleteDate") : "";
                result.put("success", "1");
                result.put("data", fundControlCsStatus);
                result.put("csUnFrozenCompleteDate", csUnFrozenCompleteDate);
            } else {
                result.put("data", message);
            }
            log.error("result:{}", response.getResult());
        } catch (Exception ex) {
            result.put("data", ex.getMessage());
            log.error("Exception when calling, ex:", ex);
        }
        log.info("调用易宝支付-查询订单-返回后系统处理返回result:{}", JSON.toJSONString(result));
        return result;
    }


    public static Map<String, String> divide(String orderId, String merchantNo, double amount, String remark, double amount2, String remark2) {
        Map<String, String> result = new HashMap<>();
        result.put("success", "0");
        result.put("data", "");
        String divideRequestId = UUID.randomUUID().toString();
        String divideRule = "AMOUNT";
        List<YeePayDivideOrderItem> list = new ArrayList<>();
        list.add(new YeePayDivideOrderItem(parentMerchantNo, amount, remark));
        list.add(new YeePayDivideOrderItem(merchantNo, amount2, remark2));
        String divideDetail = JSON.toJSONString(list);
        YopRequest request = new YopRequest("/rest/v1.0/divide/apply", "POST");
        request.addParameter("orderId", orderId);
        request.addParameter("merchantNo", merchantNo);
        request.addParameter("divideRule", divideRule);
        request.addParameter("divideDetail", divideDetail);
        request.addParameter("divideRequestId", divideRequestId);
        request.addParameter("parentMerchantNo", parentMerchantNo);
        try {
            log.info("调用易宝支付-申请订单分账-请求参数,request:{}", JSON.toJSONString(request));
            YopResponse response = client.request(request);
            log.info("调用易宝支付-申请订单分账-返回值,response:{}", JSON.toJSONString(response));
            Map<String, String> res = (Map) JSON.parse(response.getStringResult());
            String code = res.get("code");
            String message = res.get("message");
            if (code.equalsIgnoreCase("OPR00000")) {
                String status = res.get("status");
                String uniqueDivideNo = res.get("uniqueDivideNo");
                if (status.equalsIgnoreCase("SUCCESS")) {
                    result.put("success", "1");
                    result.put("data", status);
                    result.put("uniqueDivideNo", uniqueDivideNo);
                } else {
                    result.put("data", status);
                    result.put("uniqueDivideNo", uniqueDivideNo);
                }
            } else {
                result.put("data", message);
            }
            log.info("result:{}", response.getResult());
        } catch (Exception ex) {
            result.put("data", ex.getMessage());
            log.error("Exception when calling, ex:", ex);
        }
        log.info("调用易宝支付-申请订单分账-返回后系统处理返回result:{}", JSON.toJSONString(result));
        return result;
    }


    public static Map<String, String> cash(String merchantNo, String amount, String bankNo) {
        Map<String, String> result = new HashMap<>();
        result.put("success", "0");
        result.put("data", "");
        String requestNo = UUID.randomUUID().toString();
        String receiveType = "REAL_TIME";
        YopRequest request = new YopRequest("/rest/v1.0/account/withdraw/order", "POST");
        request.addParameter("parentMerchantNo", parentMerchantNo);
        request.addParameter("requestNo", requestNo);
        request.addParameter("merchantNo", merchantNo);
        request.addParameter("bankAccountNo", bankNo);
        request.addParameter("receiveType", receiveType);
        request.addParameter("orderAmount", amount);
        try {
            log.info("调用易宝支付-提现-下单-请求参数,request:{}", JSON.toJSONString(request));
            YopResponse response = client.request(request);
            log.info("调用易宝支付-提现-下单-返回值,response:{}", JSON.toJSONString(response));
            Map<String, String> res = (Map) JSON.parse(response.getStringResult());
            String returnCode = res.get("returnCode");
            String returnMsg = res.containsKey("returnMsg") ? res.get("returnMsg") : "没有消息返回";
            if (returnCode.equalsIgnoreCase("UA00000")) {
                String status = res.get("status");
                String orderNo = res.get("orderNo");
                result.put("success", "1");
                result.put("data", orderNo);
                result.put("status", status);
            } else {
                result.put("data", returnMsg);
            }
            log.info("result:{}", response.getResult());
        } catch (Exception ex) {
            result.put("data", ex.getMessage());
            log.error("Exception when calling, ex:", ex);
        }
        log.info("调用易宝支付-提现-下单-返回后系统处理返回result:{}", JSON.toJSONString(result));
        return result;
    }


    public static Map<String, String> add(YeePayMerchant merchant) {
        Map<String, String> result = new HashMap<>();
        result.put("success", "0");
        result.put("data", "");
        String requestNo = UUID.randomUUID().toString();
        String type = "TO_PUBLIC_MANAGER";
        String settlementProduct = "D1";
        YopRequest request = new YopRequest("/rest/v1.0/mer/receiver/apply", "POST");
        request.addParameter("requestNo", requestNo);
        request.addParameter("notifyUrl", MerNotifyUrl);
        request.addParameter("merchantNo", parentMerchantNo);
        request.addParameter("type", type);
        request.addParameter("receiverName", merchant.getReceiverName());
        request.addParameter("licenceNo", merchant.getLicenceNo());
        request.addParameter("mobile", merchant.getMobile());
        request.addParameter("legalName", merchant.getLegalName());
        request.addParameter("legalLicenceNo", merchant.getLegalLicenceNo());
        request.addParameter("bankCode", merchant.getBankCode());
        request.addParameter("settlementProduct", settlementProduct);
        try {
            log.info("调用易宝支付-申请入账方-请求参数,request:{}", JSON.toJSONString(request));
            YopResponse response = client.request(request);
            log.info("调用易宝支付-申请入账方-返回值,response:{}", JSON.toJSONString(response));
            Map<String, String> res = (Map) JSON.parse(response.getStringResult());
            String returnCode = res.get("returnCode");
            String returnMsg = res.get("returnMsg");
            if (returnCode.equalsIgnoreCase("NIG00000")) {
                String status = res.get("status");
                String receiverNo = res.get("receiverNo");
                result.put("success", "1");
                result.put("data", receiverNo);
                result.put("status", "status");
            } else {
                result.put("data", returnMsg);
            }
            log.info("result:{}", response.getResult());
        } catch (Exception ex) {
            result.put("data", ex.getMessage());
            log.error("Exception when calling, ex:", ex);
        }
        log.info("调用易宝支付-申请入账方-返回后系统处理返回result:{}", JSON.toJSONString(result));
        return result;
    }


    public static Map<String, String> refund(String merchantNo, String orderNo, String refundAmount) {
        Map<String, String> result = new HashMap<>();
        result.put("success", "0");
        result.put("data", "");
        String refundRequestId = UUID.randomUUID().toString();
        String description = "用户申请退款";
        String refundAccountType = "";
        YopRequest request = new YopRequest("/rest/v1.0/trade/refund", "POST");
        request.addParameter("parentMerchantNo", parentMerchantNo);
        request.addParameter("orderId", orderNo);
        request.addParameter("merchantNo", merchantNo);
        request.addParameter("refundRequestId", refundRequestId);
        request.addParameter("refundAmount", refundAmount);
        request.addParameter("description", description);
        request.addParameter("notifyUrl", RefundNotifyUrl);
        try {
            log.info("调用易宝支付-申请退款-请求参数,request:{}", JSON.toJSONString(request));
            YopResponse response = client.request(request);
            log.info("调用易宝支付-申请退款-返回值,response:{}", JSON.toJSONString(response));
            Map<String, String> res = (Map) JSON.parse(response.getStringResult());
            String code = res.get("code");
            String message = res.get("message");
            if (code.equalsIgnoreCase("OPR00000")) {
                String status = res.get("status");
                String uniqueRefundNo = res.get("uniqueRefundNo");
                result.put("success", "1");
                result.put("data", uniqueRefundNo);
                result.put("status", status);
            } else {
                result.put("data", message);
            }
            log.info("result:{}", response.getResult());
        } catch (Exception ex) {
            result.put("data", ex.getMessage());
            log.error("Exception when calling, ex:", ex);
        }
        log.info("调用易宝支付-申请退款-返回后系统处理返回result:{}", JSON.toJSONString(result));
        return result;
    }


}
