package cn.com.shopgroup.order.http.response;

import lombok.Data;

import java.util.Map;

@Data
public class OrderStatusResponse {

    // 全部订单
    private Long all;
    // 未支付
    private Long unpay;
    // 待核销(待收货)
    private Long unreceipt;
    // 已核销(已收货)
    private Long completed;
    // 退款中(已退款, 拒绝退款)
    private Long refunded;

    public OrderStatusResponse(){

    }

    public OrderStatusResponse(Map<String, Long> data){

        if(data == null) return;
        this.all = data.getOrDefault("all", 0l);
        this.unpay = data.getOrDefault("unpay", 0l);
        this.unreceipt = data.getOrDefault("unreceipt", 0l);
        this.completed = data.getOrDefault("completed", 0l);
        this.refunded = data.getOrDefault("refunded", 0l);
    }
}
