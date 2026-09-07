package cn.com.shopgroup.order.http.response;

import cn.com.shopgroup.order.model.GbOrderGoodsInfo;
import lombok.Data;

@Data
public class RefundOrderGoodsResponse {
    //订单商品id
    private Long id;
    //实体商品id
    private Long goodsId;
    //商品名
    private String goodsName;
    //图片
    private String goodsImg;
    //图片
    private Double goodsPrice;
    //购买总数量
    private Integer goodsNum;
    //可退数量
    private Integer refundGoodsNum;
    //单位
    private String goodsUnit;
    //商品介绍
    private String goodsInfo;
    // SKU信息(订单商品冗余字段, 用户选择规格时输出, 供前端识别规格)
    private Long skuId;
    private String skuIds;

    public RefundOrderGoodsResponse() {

    }

    /**
     * @param goods        订单商品
     * @param canRefundNum 可退数量(由退款类型计算: 退款=待收货部分数量, 退货退款=已收货部分数量)
     */
    public RefundOrderGoodsResponse(GbOrderGoodsInfo goods, Integer canRefundNum) {
        this.id = goods.getId();
        this.goodsId = goods.getGoodsId();
        this.goodsName = goods.getGoodsName();
        this.goodsImg = goods.getGoodsImg();
        this.goodsPrice = goods.getGoodsPrice();
        // 购买总数量
        this.goodsNum = goods.getGoodsNum();
        // 可退数量
        this.refundGoodsNum = canRefundNum;
        this.goodsUnit = goods.getGoodsUnit();
        this.skuId = goods.getSkuId();
        this.skuIds = goods.getSkuIds();
        // 商品介绍: 普通商品输出规格名称, 称重商品输出包装名称
        if (goods.getGoodsType() != null && goods.getGoodsType() == 1) {
            this.goodsInfo = goods.getSkuNames();
        } else {
            this.goodsInfo = goods.getPackName();
        }
    }
}
