package cn.com.shopgroup.order.http.response;

import lombok.Data;

import java.util.List;

// 团长端-用户退款申请记录分页返回
@Data
public class LeaderRefundApplyListResponse {
    // 申请记录总数(不受分页影响)
    private Long total;
    // 当前页码
    private Integer page;
    // 每页数量
    private Integer pageSize;
    // 申请记录列表
    private List<LeaderRefundApplyResponse> list;
}
