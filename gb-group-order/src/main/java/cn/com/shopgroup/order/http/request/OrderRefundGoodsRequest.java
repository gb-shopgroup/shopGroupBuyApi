package cn.com.shopgroup.order.http.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class OrderRefundGoodsRequest {

    // 订单商品ID
    @NotNull(message = "订单商品id不能为空")
    private Long orderGoodsId;

    // 商品数量
    @NotNull(message = "申请退货数量不能为空")
    private Integer refundNum;

    // 退款金额
    @NotNull(message = "申请退款金额不能为空")
    private Double refundAmount;

}
