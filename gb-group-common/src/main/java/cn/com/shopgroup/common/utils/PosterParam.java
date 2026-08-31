package cn.com.shopgroup.common.utils;

import lombok.Data;

import java.awt.image.BufferedImage;
import java.io.InputStream;

@Data
public class PosterParam {

    // 店铺logo网络地址
    public String shopLogoUrl;
    // 店铺名称
    public String shopName;
    // 展示时间
    public String showTime;
    // 爆款推荐本地图片路径
    public InputStream hotImgLocalPath;
    // 商品网络图片
    public String goodsImgUrl;
    // 商品名称
    public String goodsName;
    // 售价
    public String price;
    // 销量
    public String sales;
    // 二维码本地图片路径
    public BufferedImage ercodeImage;
    // 长按识别,跟团购买
    public String tipText;

}
