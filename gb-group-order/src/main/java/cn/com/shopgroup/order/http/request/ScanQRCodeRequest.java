package cn.com.shopgroup.order.http.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ScanQRCodeRequest {

    @NotNull(message = "订单号")
    private String orderNo;

    @NotNull(message = "核销码")
    private String receiptCode;

}
