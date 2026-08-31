package cn.com.shopgroup.goods.http.request.leader;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class LeaderGroupListRequest {

    // 团购名称
    private String name;
    // 状态：0 全部  1 活动中 2 未开始 3 已结束
    private Integer status;
    //分类id[分类id不能为空]
    @NotNull(message = "分类id不能为空")
    private Long catId;
    //页码
    private Integer page;
    //每页条数
    private Integer pageSize;


}
