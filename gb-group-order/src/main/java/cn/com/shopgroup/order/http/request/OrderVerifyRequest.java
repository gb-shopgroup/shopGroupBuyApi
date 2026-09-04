package cn.com.shopgroup.order.http.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
public class OrderVerifyRequest {

    // 订单号
    @NotNull(message = "订单号不能为空")
    private String orderNo;
    // 提货点id
    @NotNull(message = "提货点id不能为空")
    private Long pid;
    // 商品收货数量映射: key=订单商品id, value=核销数量
    private Map<Long, OrderVerifyGoodsRequest> goodsMap;
}
