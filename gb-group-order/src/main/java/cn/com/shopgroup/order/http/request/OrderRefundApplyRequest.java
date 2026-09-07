package cn.com.shopgroup.order.http.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
public class OrderRefundApplyRequest {
    //退款类型标识 1 退款 2 退货退款
    @NotNull(message = "退款类型不能为空")
    private Integer refundFlag;
    @NotNull(message = "订单号不能为空")
    private String orderNo;
    @NotEmpty(message = "订单商品不能为空")
    //map key Long订单商品ID
    private Map<Long, OrderRefundGoodsRequest> refundGoodsMap;
    //申请原因
    @NotNull(message = "申请原因不能为空")
    private String actionReason;
    //附加说明
    private String extraReason;
}
