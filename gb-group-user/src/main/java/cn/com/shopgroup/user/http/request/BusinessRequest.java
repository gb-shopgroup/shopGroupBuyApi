package cn.com.shopgroup.user.http.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class BusinessRequest {

    // 账户id
    private Long id;

    // 账户类型, 1=一般企业2=小微企业3=个体户4=个人
    @NotNull(message = "类型不能为空")
    private Byte type;
    // 账户名称
    @NotNull(message = "名称不能为空")
    private String name;
    // 法人姓名
    @NotNull(message = "法人不能为空")
    private String legal;
    // 身份证号
    private String no;
    // 身份证正面
    private String front;
    // 身份证反面
    private String back;
    // 纳税额度,单位：万（收款提醒）
    private Integer tax;

}
