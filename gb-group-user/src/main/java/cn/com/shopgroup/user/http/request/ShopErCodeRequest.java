package cn.com.shopgroup.user.http.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ShopErCodeRequest {

    @NotNull(message = "店铺id不能为空")
    private Long shopId;
    @NotNull(message = "门店url")
    private String shopUrl;

}
