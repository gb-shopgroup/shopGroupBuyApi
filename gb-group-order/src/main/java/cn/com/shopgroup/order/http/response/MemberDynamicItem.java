package cn.com.shopgroup.order.http.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 团员动态-单条记录
 */
@Data
@ApiModel("团员动态项")
public class MemberDynamicItem {

    @ApiModelProperty("时间，HH:mm")
    private String time;

    @ApiModelProperty("动作类型：view=查看，order=跟团下单")
    private String action;

    @ApiModelProperty("动作文本，如：查看了XXXX团购 / 跟团下单 麒麟大西瓜")
    private String content;

    public MemberDynamicItem() {
    }

    public MemberDynamicItem(String time, String action, String content) {
        this.time = time;
        this.action = action;
        this.content = content;
    }
}
