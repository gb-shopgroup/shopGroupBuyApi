package cn.com.shopgroup.order.http.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
//用户首页-查询团活动请求参数
@Data
public class MemberGroupListRequest {
    //团长id
    @NotNull(message = "团长id不能为空")
    private Long leaderId;
    // 团购名称
    private String name;
    //分类id
    private Long catId;
    //页码
    private Integer page;
    //每页条数
    private Integer pageSize;


}
