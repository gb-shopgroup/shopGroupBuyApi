package cn.com.shopgroup.http.request;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class LeaderRequest {

    @NotNull(message = "请输入姓名")
    private String name;

    @NotNull(message = "请输入手机号")
    private String mobile;

    @NotNull(message = "请输入店铺名称")
    private String shopName;

    @NotNull(message = "请输入商户编号")
    private String shopCode;

    @Min(value = 3, message = "最小是3")
    @Max(value = 10, message = "最大是10")
    private Byte commission;


}
