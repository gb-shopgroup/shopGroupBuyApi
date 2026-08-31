package cn.com.shopgroup.common.yeepay;

import com.alibaba.fastjson2.JSON;
import com.yeepay.yop.sdk.service.common.YopClient;
import com.yeepay.yop.sdk.service.common.YopClientBuilder;
import com.yeepay.yop.sdk.service.common.request.YopRequest;
import com.yeepay.yop.sdk.service.common.response.YopResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class YeepayUtils {


    private static final YopClient client = YopClientBuilder.builder().build();

    private static final Logger log = LoggerFactory.getLogger(YeepayUtils.class);


    private static final String parentMerchantNo = "10093520300";


    private static final String WxAppId = "wx959de27ff559b753";


    private static final String PayNotifyUrl = "https://api.shopgroup.com.cn/order/order/notify";


    private static final String RefundNotifyUrl = "https://api.shopgroup.com.cn/leader/refund/notify";


    private static final String MerNotifyUrl = "https://api.shopgroup.com.cn/admin/leader/notify";


    public static Map<String, String> pay(String notifyUrl, String merchantNo, String openid, String userIp, String orderNo, String goodsName, double orderAmount) {


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

        if (notifyUrl == null || notifyUrl.length() == 0) notifyUrl = PayNotifyUrl;
        request.addParameter("notifyUrl", notifyUrl);

        request.addParameter("scene", "OFFLINE");

        request.addParameter("appId", WxAppId);

        request.addParameter("userId", openid);

        request.addParameter("userIp", userIp);

        request.addParameter("goodsName", goodsName);

        request.addParameter("fundProcessType", "DELAY_SETTLE");

        try {
            YopResponse response = client.request(request);
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


            YopResponse response = client.request(request);
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


        return result;
    }


    public static Map<String, String> divide(String orderId, String merchantNo, double amount, String remark, double amount2, String remark2) {


        Map<String, String> result = new HashMap<>();
        result.put("success", "0");
        result.put("data", "");


        String divideRequestId = UUID.randomUUID().toString();


        String divideRule = "AMOUNT";


        List<YeepayDivideOrderItem> list = new ArrayList<>();

        list.add(new YeepayDivideOrderItem(parentMerchantNo, amount, remark));

        list.add(new YeepayDivideOrderItem(merchantNo, amount2, remark2));

        String divideDetail = JSON.toJSONString(list);


        YopRequest request = new YopRequest("/rest/v1.0/divide/apply", "POST");
        request.addParameter("orderId", orderId);
        request.addParameter("merchantNo", merchantNo);
        request.addParameter("divideRule", divideRule);
        request.addParameter("divideDetail", divideDetail);
        request.addParameter("divideRequestId", divideRequestId);
        request.addParameter("parentMerchantNo", parentMerchantNo);


        try {

            YopResponse response = client.request(request);
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


            log.error("result:{}", response.getResult());
        } catch (Exception ex) {

            result.put("data", ex.getMessage());
            log.error("Exception when calling, ex:", ex);
        }

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


            YopResponse response = client.request(request);
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


            log.error("result:{}", response.getResult());

        } catch (Exception ex) {

            result.put("data", ex.getMessage());
            log.error("Exception when calling, ex:", ex);
        }


        return result;
    }


    public static Map<String, String> add(YeepayMerchant merchant) {


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

            YopResponse response = client.request(request);
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


            log.error("result:{}", response.getResult());

        } catch (Exception ex) {

            result.put("data", ex.getMessage());
            log.error("Exception when calling, ex:", ex);
        }


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

            YopResponse response = client.request(request);
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


            log.error("result:{}", response.getResult());

        } catch (Exception ex) {

            result.put("data", ex.getMessage());
            log.error("Exception when calling, ex:", ex);
        }


        return result;
    }


}
