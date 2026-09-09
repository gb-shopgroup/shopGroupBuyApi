package cn.com.shopgroup.order.http.request;

import lombok.Data;

//用户端查询订单售后列表参数
@Data
public class MemberOrderRefundListRequest {

    // 1 待审核 2 同意 3 不同意
    private Integer status;
    // 商品名称(模糊查询, 可选; 命中该订单售后商品行即返回)
    private String goodsName;
    //不传
    private Integer page;

    private Integer pageSize;


}
