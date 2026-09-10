package cn.com.shopgroup.order.http.request;

import lombok.Data;

//用户端查询订单售后列表参数
@Data
public class MemberOrderRefundListRequest {

    // 售后审核状态:1 待审核 2 同意 3 不同意(不传=查询全部售后状态)
    private Integer status;
    // 商品名称(模糊查询, 可选; 仅返回包含该商品售后行的记录)
    private String goodsName;
    // 页码(从 1 开始, 不传默认 1)
    private Integer page;

    // 每页条数(默认 10)
    private Integer pageSize;


}
