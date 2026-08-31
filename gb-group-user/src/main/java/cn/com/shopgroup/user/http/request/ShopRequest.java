package cn.com.shopgroup.user.http.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ShopRequest {

    @NotNull(message = "店铺id不能为空")
    private Long shopId;
    @NotNull(message = "门店名称不能为空")
    private String name;
    @NotNull(message = "联系电话不能为空")
    private String mobile;
    @NotNull(message = "门店banner不能为空")
    private String banner;
    @ApiModelProperty(value = "店铺信息介绍")
    private String shopInfo;

}
