package cn.com.shopgroup.user.http.response;

import cn.com.shopgroup.user.model.GbOrgShopInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "团长端-我的店铺返回信息")
public class ShopResponse {
    @ApiModelProperty(value = "店铺id")
    private Long id;
    @ApiModelProperty(value = "店铺名称")
    private String name;
    @ApiModelProperty(value = "联系电话")
    private String mobile;
    @ApiModelProperty(value = "店铺banner")
    private String banner;
    @ApiModelProperty(value = "店铺二维码图,用户扫码查看待核销订单使用")
    private String shopCodeUrl;
    @ApiModelProperty(value = "店铺信息介绍")
    private String shopInfo;

    public ShopResponse() {

    }

    public ShopResponse(GbOrgShopInfo data) {
        this.id = data.getId();
        this.name = data.getShopName();
        this.mobile = data.getShopMobile();
        this.banner = data.getShopBanner();
        this.shopCodeUrl = data.getShopCodeUrl();
        this.shopInfo = data.getShopInfo();
    }

}
