package cn.com.shopgroup.user.http.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class OCBusinessRequest {

    // 账户id
    @NotNull(message = "busId不能为空")
    private Long busId;

    @NotNull(message = "0 启用 1 禁用")
    private Integer status;

}
