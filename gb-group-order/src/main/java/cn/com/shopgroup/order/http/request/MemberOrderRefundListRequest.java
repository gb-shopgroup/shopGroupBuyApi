package cn.com.shopgroup.order.http.request;

import lombok.Data;

//用户端查询订单售后列表参数
@Data
public class MemberOrderRefundListRequest {

    // 1 待审核 2 同意 3 不同意
    private Integer status;
    //不传
    private Integer page;

    private Integer pageSize;


}
