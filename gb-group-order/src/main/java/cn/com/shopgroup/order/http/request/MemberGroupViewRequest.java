package cn.com.shopgroup.order.http.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户查看团购详情-埋点上报请求
 */
@Data
@ApiModel("查看团购埋点上报请求")
public class MemberGroupViewRequest {

    @ApiModelProperty(value = "团购id", required = true)
    private Long groupId;

    @ApiModelProperty("埋点来源：1 首页进入详情(默认)，2 分享进入")
    private Integer source;
}
