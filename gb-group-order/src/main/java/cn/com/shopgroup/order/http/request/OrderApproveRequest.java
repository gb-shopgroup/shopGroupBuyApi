package cn.com.shopgroup.order.http.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * 审核请求参数
 */
@Data
public class OrderApproveRequest {
    @NotNull(message = "审核订单商品不能为空")
    //key String 是订单号
    private Map<String, OrderRefundInfoRequest> refundOrderGoodsMap;
    //审核结果 1 同意 2 拒绝
    @NotNull(message = "审核结果不能为空")
    private Integer status;
    private String reason;
}
