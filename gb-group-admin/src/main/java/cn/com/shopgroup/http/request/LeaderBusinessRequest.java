package cn.com.shopgroup.http.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class LeaderBusinessRequest {

    // 团长ID
    private Long leaderId;

    @NotNull(message = "请输入店铺名称")
    private String shopName;

    @NotNull(message = "请输入商户编号")
    private String shopCode;

}
