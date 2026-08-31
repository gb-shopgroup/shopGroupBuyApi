package cn.com.shopgroup.user.http.response;

import lombok.Data;

@Data
public class UserResponse {

    private String name;
    private String avatar;
    private String token;

}
