package cn.com.shopgroup.order.http.request;

import lombok.Data;

import java.util.Map;

@Data
public class OrderVerifyRequest {

    // 订单id
    private String orderNo;
    // 提货点id
    private Long pid;
    // 商品收货数量映射: key=订单商品id, value=核销数量
    private Map<Long, OrderVerifyGoodsRequest> goodsMap;
}
