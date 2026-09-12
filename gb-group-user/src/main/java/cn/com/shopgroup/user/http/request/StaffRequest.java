package cn.com.shopgroup.user.http.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class StaffRequest {

    // 员工Id (修改时候使用)
    private Long id;

    // 员工姓名
    private String name;
    // 手机号码
    @NotNull(message = "手机不能为空")
    private String mobile;
    // 员工备注
    private String remark;
    // 权限设置(逗号间隔)
    private String auth;
    // 所属提货点(逗号间隔)
    private String point;


}
