package cn.com.shopgroup.order.http.response;

import cn.com.shopgroup.order.model.GbOrderGoodsInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OrderGoodsReponse {
    private Long id;
    private Long goodsId;
    private String goodsName;
    private String goodsImg;
    private Double goodsPrice;
    private Integer goodsNum;
    private Integer receiptNum;
    private Integer applyRefund;
    private Integer refundGoodsNum;//商品退数量
    private String goodsUnit;
    private String goodsInfo;
    // SKU信息(订单商品冗余字段, 用户选择规格时输出, 供前端识别规格)
    private Long skuId;
    private String skuIds;

    public OrderGoodsReponse(){

    }

    public OrderGoodsReponse(GbOrderGoodsInfo goods){
        this.id = goods.getId();
        this.goodsId = goods.getGoodsId();
        this.goodsName = goods.getGoodsName();
        this.goodsImg = goods.getGoodsImg();
        this.goodsPrice = goods.getGoodsPrice();
        this.goodsNum = goods.getGoodsNum();
        this.receiptNum = goods.getReceiptNum();
        this.applyRefund = goods.getApplyRefund();
        this.refundGoodsNum = goods.getRefundGoodsNum();
        this.goodsUnit = goods.getGoodsUnit();
        // 输出订单冗余的SKU结构化字段
        this.skuId = goods.getSkuId();
        this.skuIds = goods.getSkuIds();
        if(goods.getGoodsType() == 1){
            // 普通商品
            this.goodsInfo = goods.getSkuNames();
        }else{
            // 称重商品
            this.goodsInfo = goods.getPackName();
        }
    }

    // 列表转换
    public static List<OrderGoodsReponse> getOrderGoodsReponseList(List<GbOrderGoodsInfo> lists){

        List<OrderGoodsReponse> data = new ArrayList<>();
        for(GbOrderGoodsInfo item : lists){
            data.add(new OrderGoodsReponse(item));
        }
        return data;
    }
}
