package cn.com.shopgroup.common.utils;

import lombok.Data;

import java.awt.image.BufferedImage;

@Data
public class PosterDTO {

    private String goodsName;
    private String saleText;
    private String priceText;
    private String btnText;
    private String imgUrl;
    // 团长店铺二维码(店铺小程序码), 非空时绘制在海报右下角; 为空则不绘制
    private BufferedImage qrCodeImage;

}