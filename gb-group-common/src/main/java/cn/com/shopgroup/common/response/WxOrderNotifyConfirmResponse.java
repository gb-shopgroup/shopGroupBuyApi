package cn.com.shopgroup.common.response;

import lombok.Data;

//微信订单确认收货返回值
@Data
public class WxOrderNotifyConfirmResponse {
    private int code;
    private String msg;
}
