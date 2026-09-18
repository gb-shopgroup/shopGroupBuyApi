package cn.com.shopgroup.order.http.request;

import lombok.Data;

//团长端-批量查询用户退款申请记录参数
@Data
public class LeaderRefundApplyListRequest {

    private String keyword;

    private Integer page;

    private Integer pageSize;
}
