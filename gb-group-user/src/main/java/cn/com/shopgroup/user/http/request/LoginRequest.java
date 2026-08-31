package cn.com.shopgroup.user.http.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class LoginRequest {

    @NotNull(message = "请输入账号")
    private String username;
    @NotNull(message = "请输入密码")
    private String password;
    @NotNull(message = "请输入验证码")
    private String code;

}
