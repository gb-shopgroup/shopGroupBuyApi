package cn.com.shopgroup.http.response;

import lombok.Data;

@Data
public class UserResponse {

    private String name;
    private String avatar;
    private String token;

}
