package cn.com.shopgroup.user.http.request;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ShopRequest {

    @NotNull(message = "店铺id不能为空")
    private Long shopId;
    @NotNull(message = "门店名称不能为空")
    private String name;
    @ApiModelProperty(value = "店铺简称")
    private String shortName;
    @NotNull(message = "联系电话不能为空")
    private String mobile;
    //门店banner
    private String banner;
    @ApiModelProperty(value = "店铺信息介绍")
    private String shopInfo;
    // 店铺logo
    @TableField("店铺logo")
    private String shopLogo;
    //店铺二维码图,用户扫码查看待核销订单使用
    @TableField("店铺二维码图")
    private String shopCodeUrl;

}
