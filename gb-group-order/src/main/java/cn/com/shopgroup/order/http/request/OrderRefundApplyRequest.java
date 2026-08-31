package cn.com.shopgroup.order.http.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
public class OrderRefundApplyRequest {

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
