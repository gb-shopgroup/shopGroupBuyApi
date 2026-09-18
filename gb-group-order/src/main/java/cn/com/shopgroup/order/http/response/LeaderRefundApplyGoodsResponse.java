package cn.com.shopgroup.order.http.response;

import cn.com.shopgroup.common.utils.MoneyUtil;
import cn.com.shopgroup.order.model.GbOrderGoodsInfo;
import lombok.Data;

// 团长端-退款申请订单的商品行明细(与审核入参 OrderRefundGoodsRequest 字段对齐:
// orderGoodsId=审核 refundGoodsMap 的 key, refundNum/refundAmount=本次申请数量/金额, 供团长端直接组装审核请求)
@Data
public class LeaderRefundApplyGoodsResponse {
    // 订单商品行id(审核回传 refundGoodsMap 的 key)
    private Long orderGoodsId;
    // 商品id
    private Long goodsId;
    // 商品名称
    private String goodsName;
    // 商品图片
    private String goodsImg;
    // 规格信息(订单商品冗余的SKU名称)
    private String goodsInfo;
    // 商品单位
    private String goodsUnit;
    // 商品单价(单位:元)
    private Double goodsPrice;
    // 购买数量
    private Integer goodsNum;
    // 收货数量
    private Integer receiptNum;
    // 行售后(审核)状态: 0=无 1=待审核 2=同意 3=不同意
    private Integer applyRefund;
    // 累计退款数量(退待收货部分, 申请占坑累计; 待审核行含本次申请, 历史已同意部分保留)
    private Integer refundNum;
    // 累计退货退款数量(退已收货部分, 申请占坑累计; 待审核行含本次申请, 历史已同意部分保留)
    private Integer refundGoodsNum;
    // 行退款金额(单位:元): 按 refundFlag 取对应占坑数量 x 商品单价推算
    // (商品维度退款金额不落库, 与审核端口径一致)
    private Double refundAmount;

    public LeaderRefundApplyGoodsResponse(GbOrderGoodsInfo goods, Integer refundFlag) {
        this.orderGoodsId = goods.getId();
        this.goodsId = goods.getGoodsId();
        this.goodsName = goods.getGoodsName();
        this.goodsImg = goods.getGoodsImg();
        this.goodsInfo = goods.getSkuNames();
        this.goodsUnit = goods.getGoodsUnit();
        this.goodsPrice = goods.getGoodsPrice();
        this.goodsNum = goods.getGoodsNum();
        this.receiptNum = goods.getReceiptNum();
        this.applyRefund = goods.getApplyRefund();
        this.refundNum = goods.getRefundNum();
        this.refundGoodsNum = goods.getRefundGoodsNum();
        // 行退款金额推算: refundFlag=1取退款数量(退待收货), =2取退货退款数量(退已收货)
        int flag = refundFlag == null ? 0 : refundFlag;
        int num = 0;
        if (flag == 1) {
            num = goods.getRefundNum() == null ? 0 : goods.getRefundNum();
        } else if (flag == 2) {
            num = goods.getRefundGoodsNum() == null ? 0 : goods.getRefundGoodsNum();
        }
        int priceCent = MoneyUtil.yuanToCent(goods.getGoodsPrice() == null ? 0D : goods.getGoodsPrice());
        this.refundAmount = MoneyUtil.centToYuan(priceCent * num);
    }
}
