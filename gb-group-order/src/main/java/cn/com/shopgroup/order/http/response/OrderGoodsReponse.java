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
    // 购买数量
    private Integer goodsNum;
    // 收货数量
    private Integer receiptNum;
    // 售后(审核)状态:0 无 1 待审核 2 同意 3 不同意
    private Integer applyRefund;
    // 退货退款数量(退已收货部分, 申请累计)
    private Integer refundGoodsNum;
    // 退款数量(退待收货部分, 申请累计)
    private Integer refundNum;
    // 商品单位
    private String goodsUnit;
    // 规格信息(普通商品=SKU名称, 称重商品=包装名称)
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
        this.refundNum = goods.getRefundNum();
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
