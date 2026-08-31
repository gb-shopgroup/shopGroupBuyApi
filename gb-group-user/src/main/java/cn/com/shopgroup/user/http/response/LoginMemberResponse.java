package cn.com.shopgroup.user.http.response;

import cn.com.shopgroup.user.model.GbMemberInfo;
import lombok.Data;

@Data
public class LoginMemberResponse {

    //private int id;
    private String name;
    private String avatar;
    private String mobile;
    private String openid;
    private String token;

    public LoginMemberResponse() {

    }

    public LoginMemberResponse(GbMemberInfo data) {

        //this.id = data.getMemberId();
        this.name = data.getNickname();
        this.avatar = data.getAvatar();
        this.mobile = data.getMobile();
        this.openid = data.getOpenid();
    }

}
