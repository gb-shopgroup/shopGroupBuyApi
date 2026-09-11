package cn.com.shopgroup.goods.http.request.leader;

import lombok.Data;

@Data
public class MemberGroupListRequest {

    // 团长id(0=新用户未绑定团长, 此时必须传经纬度)
    private Long leaderId;
    // 经度, 精度为10米级(leaderId=0时必填)
    private Double longitude;
    // 纬度, 精度为10米级(leaderId=0时必填)
    private Double latitude;

    // 页码, 默认1
    private Integer page;
    // 每页条数, 默认10, 最大20
    private Integer pageSize;
}
