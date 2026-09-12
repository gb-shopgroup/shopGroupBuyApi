package cn.com.shopgroup.order.http.request;

import lombok.Data;

//用户端查询订单列表参数
@Data
public class MemberOrderListRequest {

    //商品名称
    private String goodsName;
    //订单状态[不传 全部 订单状态:0 待支付,1 待收货 2 部分收货 3 已提货 4 已退款, 5 售后(5 售后同时返回4 已退款的订单) 6 已取消]
    private Integer status;

    private Integer page;

    private Integer pageSize;
}
