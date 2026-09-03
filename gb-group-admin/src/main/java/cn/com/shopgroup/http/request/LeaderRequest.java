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

    // 结算到账方式,默认0=支付时延迟到账型,1=核销时延迟到账型(不传默认0)
    @Min(value = 0, message = "到账方式最小是0")
    @Max(value = 1, message = "到账方式最大是1")
    private Integer cashType;


}
