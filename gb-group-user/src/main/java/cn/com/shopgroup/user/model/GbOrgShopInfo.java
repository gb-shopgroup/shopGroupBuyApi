package cn.com.shopgroup.user.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 团长店铺信息表
@Data
@TableName("gb_org_shop_info")
public class GbOrgShopInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("leader_id")
    private Long leaderId;
    // 店铺名称
    @TableField("shop_name")
    private String shopName;
    // 店铺logo
    @TableField("shop_logo")
    private String shopLogo;
    // 店铺banner
    @TableField("shop_banner")
    private String shopBanner;
    // 联系电话
    @TableField("shop_mobile")
    private String shopMobile;
    //店铺二维码图,用户扫码查看待核销订单使用
    @TableField("shop_code_url")
    private String shopCodeUrl;
    // 店铺介绍
    @TableField("shop_info")
    private String shopInfo;

}
