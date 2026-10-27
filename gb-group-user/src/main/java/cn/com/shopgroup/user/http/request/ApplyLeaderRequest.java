package cn.com.shopgroup.user.http.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ApplyLeaderRequest {

    // 姓名
    @NotNull(message = "姓名不能为空")
    private String name;
    // 手机号码
    @NotNull(message = "手机号码不能为空")
    private String mobile;

}
