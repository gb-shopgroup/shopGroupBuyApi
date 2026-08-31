package cn.com.shopgroup.user.http.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class MemberRequest {

    @NotNull(message = "姓名不能为空")
    private String name;

    @NotNull(message = "头像不能为空")
    private String avatar;

    @NotNull(message = "手机不能为空")
    private String mobile;

    @NotNull(message = "openid不能为空")
    private String openid;

    // 团长ID
    private Long leader;

    // 地图定位
    private String map;

}
