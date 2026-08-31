package cn.com.shopgroup.user.http.response;

import lombok.Data;

@Data
public class LeaderResponse {

    // 团长id(加密)
    private String lid = "";
    // 员工id(加密)
    private String sid = "";
    // 店铺名称
    private String shop = "";
    // 员工姓名
    private String name = "";
    // 是否超管
    private Boolean isSuper = false;
    // 权限列表(逗号间隔)
    private String auth = "";
    // 提货点列表(逗号间隔)
    private String pointIds = "";

    public LeaderResponse(){

    }
}
