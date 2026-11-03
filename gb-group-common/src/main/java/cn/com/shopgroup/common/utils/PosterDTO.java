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

    // ==================== 新版竖版商品海报(朋友圈分享样式)扩展字段 ====================
    // 店铺logo网络地址(顶部深绿色条展示, 获取失败自动跳过)
    private String shopLogoUrl;
    // 店铺名称(顶部深绿色条展示)
    private String shopName;
    // 团购活动名(商品名上方展示, 为空时不绘制该行)
    private String groupName;
    // 价格数字(如 1.8, 与 priceUnit 拆开便于差异化字号渲染)
    private String priceValue;
    // 价格单位(如 元/袋)
    private String priceUnit;
    // 底部提示语(深绿色底条展示, 为空时使用默认文案)
    private String footerText;

}
