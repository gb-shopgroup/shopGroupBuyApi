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
    //退款类型标识: 1=退款(退待收货部分) 2=退货退款(退已收货部分); 申请时携带, 审核时回传; 旧版本客户端可能不传
    private Integer refundFlag;
    @NotEmpty(message = "退款订单商品不能为空")
    //map key Long订单商品ID
    private Map<Long, OrderRefundGoodsRequest> refundGoodsMap;
}
