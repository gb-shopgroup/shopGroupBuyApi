package cn.com.shopgroup.user.http.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class MemberSwapLeaderRequest {

    //openid
    @NotNull(message = "openid不能为空")
    private String openid;

    // 团长ID
    @NotNull(message = "团长ID不能为空")
    private Long leaderId;

}
