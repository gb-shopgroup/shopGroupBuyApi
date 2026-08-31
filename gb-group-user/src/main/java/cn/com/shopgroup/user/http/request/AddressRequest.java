package cn.com.shopgroup.user.http.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AddressRequest {

    @NotNull(message = "姓名不能为空")
    private String name;
    @NotNull(message = "电话不能为空")
    private String telephone;

    @NotNull(message = "省份不能为空")
    private String province;
    @NotNull(message = "城市不能为空")
    private String city;
    @NotNull(message = "区县不能为空")
    private String district;
    @NotNull(message = "地址不能为空")
    private String address;

}
