package cn.com.shopgroup.order.http.request;

import lombok.Data;

//团长端查询订单列表参数
@Data
public class LeaderOrderListRequest {

    //商品名称 或是手机号
    private String keyword;
    //订单状态[不传 全部 订单状态:0 待支付,1 待收货 2 部分收货 3 已提货 4 已退款, 5 售后 6 已取消]
    private Integer status;
    //团活动Id
    private Long groupId;
    //自提点id
    private Long pointId;

    private Integer page;

    private Integer pageSize;
}
