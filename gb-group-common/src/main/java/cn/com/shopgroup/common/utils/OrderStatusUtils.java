/*
package cn.com.shopgroup.common.utils;

import java.util.HashMap;
import java.util.Map;

public class OrderStatusUtils {


    public static Map<String,String> getOrderStatus(boolean isMember, byte isPay, byte isRefund, byte isReceipt, int receiptTime, int verifyTime){


        Map<String, String> res = new HashMap<>();
        res.put("status", "all");
        res.put("statusText", "未知状态");

        if(isPay == 0){


            res.put("status", "unpay");
            res.put("statusText", "未支付");
        } else {
            if(isRefund > 0){


                res.put("status", "refunded");
                if(isRefund == 1){
                    res.put("statusText", "退款中");
                }else if(isRefund == 2){
                    res.put("statusText", "已退款");
                }else if(isRefund == 3){
                    res.put("statusText", "拒绝退款");
                }else{
                    res.put("statusText", "未知退款状态");
                }
            }else{
                if(isReceipt == 0){

   
                    res.put("status", "unreceipt");
                    if(isMember){
                        res.put("statusText", "待收货");
                    }else{
                        res.put("statusText", "待核销");
                    }
                }else{


                    if(isMember){

                        if(receiptTime > 0){
                            res.put("status", "completed");
                            res.put("statusText", "已收货");
                        }else{
                            res.put("status", "unreceipt");
                            res.put("statusText", "待收货");
                        }
                    }else{

                        if(verifyTime > 0){
                            res.put("status", "completed");
                            res.put("statusText", "已核销");
                        }else{
                            res.put("status", "unreceipt");
                            res.put("statusText", "待核销");
                        }
                    }
                }
            }
        }
        return res;
    }


}
*/
