package cn.com.shopgroup.order.http.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 团员动态-按日期分组
 */
@Data
@ApiModel("团员动态日期分组")
public class MemberDynamicGroup {

    @ApiModelProperty("日期文案，如：今天、08-17、2025-08-05")
    private String date;

    @ApiModelProperty("当天动态列表")
    private List<MemberDynamicItem> items;

    public MemberDynamicGroup() {
    }

    public MemberDynamicGroup(String date, List<MemberDynamicItem> items) {
        this.date = date;
        this.items = items;
    }
}
