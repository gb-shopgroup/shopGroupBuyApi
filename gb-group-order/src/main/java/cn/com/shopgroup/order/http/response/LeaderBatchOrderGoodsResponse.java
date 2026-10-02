package cn.com.shopgroup.order.http.response;

import cn.com.shopgroup.order.model.GbOrderGoodsInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LeaderBatchOrderGoodsResponse {
    private Long id;
    private Long goodsId;
    private String goodsName;
    private String goodsImg;
    private Double goodsPrice;
    // 退款数量(退待收货部分, 申请累计)
    private Integer refundNum;
    // 商品单位
    private String goodsUnit;
    // 规格信息(普通商品=SKU名称, 称重商品=包装名称)
    private String goodsInfo;
    // SKU信息(订单商品冗余字段, 用户选择规格时输出, 供前端识别规格)
    private Long skuId;
    private String skuIds;

    public LeaderBatchOrderGoodsResponse() {

    }

    public LeaderBatchOrderGoodsResponse(GbOrderGoodsInfo goods) {
        this.id = goods.getId();
        this.goodsId = goods.getGoodsId();
        this.goodsName = goods.getGoodsName();
        this.goodsImg = goods.getGoodsImg();
        this.goodsPrice = goods.getGoodsPrice();
        this.refundNum = goods.getRefundNum();
        this.goodsUnit = goods.getGoodsUnit();
        // 输出订单冗余的SKU结构化字段
        this.skuId = goods.getSkuId();
        this.skuIds = goods.getSkuIds();
        this.goodsInfo = goods.getSkuNames();
    }

    // 列表转换
    public static List<LeaderBatchOrderGoodsResponse> getOrderGoodsResponseList(List<GbOrderGoodsInfo> lists) {

        List<LeaderBatchOrderGoodsResponse> data = new ArrayList<>();
        for (GbOrderGoodsInfo item : lists) {
            LeaderBatchOrderGoodsResponse goodsResponse = new LeaderBatchOrderGoodsResponse();
            Integer refundNum = 0;
            refundNum = item.getGoodsNum().intValue() - item.getReceiptNum();
            goodsResponse = new LeaderBatchOrderGoodsResponse(item);
            goodsResponse.setRefundNum(refundNum);
            data.add(goodsResponse);
        }
        return data;
    }
}
