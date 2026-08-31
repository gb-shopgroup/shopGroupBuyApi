package cn.com.shopgroup.order.http.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * 审核请求退款参数对象
 */
@Data
public class OrderRefundInfoRequest {
    @NotNull(message = "退款订单号不能为空")
    private String orderNo;
    @NotEmpty(message = "退款订单商品不能为空")
    //map key Long订单商品ID
    private Map<Long, OrderRefundGoodsRequest> refundGoodsMap;
}
