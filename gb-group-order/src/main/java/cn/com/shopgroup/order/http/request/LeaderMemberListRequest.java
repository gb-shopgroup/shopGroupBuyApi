package cn.com.shopgroup.order.http.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 团长端-我的团员列表查询请求
 */
@Data
@ApiModel("团员列表查询请求")
public class LeaderMemberListRequest {

    @ApiModelProperty("搜索关键字：团员手机号或昵称")
    private String keyword;

    @ApiModelProperty("页码，默认1")
    private Integer page;

    @ApiModelProperty("每页数量，默认10，最大20")
    private Integer pageSize;
}
