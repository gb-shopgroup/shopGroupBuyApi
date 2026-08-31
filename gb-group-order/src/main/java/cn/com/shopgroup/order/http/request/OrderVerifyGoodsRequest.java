package cn.com.shopgroup.order.http.request;

import lombok.Data;

@Data
public class OrderVerifyGoodsRequest {

    // 订单商品表id
    private Long id;
    // 收货数量
    private Integer num;
}
