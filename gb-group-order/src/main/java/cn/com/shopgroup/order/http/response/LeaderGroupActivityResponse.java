package cn.com.shopgroup.order.http.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 团长端-团购活动下拉选项响应
 * <p>
 * 仅暴露团长活动下拉/选择场景所需的字段，避免向前端泄露与本场景无关的活动详情字段。
 */
@Data
@ApiModel(value = "团长端团购活动选项")
public class LeaderGroupActivityResponse {

    @ApiModelProperty(value = "团购活动 ID")
    private Long groupId;

    @ApiModelProperty(value = "团购活动名称")
    private String groupName;

    @ApiModelProperty(value = "开团时间(秒级时间戳)")
    // 开团时间(秒级时间戳)
    private Integer startTime;

    @ApiModelProperty(value = "结束时间(秒级时间戳)")
    // 结束时间(秒级时间戳)
    private Integer endTime;

    @ApiModelProperty(value = "实际订单数量")
    private Integer orderTotal;

    @ApiModelProperty(value = "是否关闭: 0 上线 1 下线")
    private Byte isClose;

}