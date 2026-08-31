package cn.com.shopgroup.order.http.request;

import lombok.Data;

@Data
public class OrderRefuseRequest {

    private String orderNo;
    private String reason;
}
