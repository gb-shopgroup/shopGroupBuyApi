package cn.com.shopgroup.order.http.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

//团长端-批量查询用户退款申请记录参数
@Data
public class LeaderApproveListRequest {

    @NotNull(message = "请选择具体团购活动查询")
    private Long groupId;

    private String goodsName;

    private Integer page;

    private Integer pageSize;
}
