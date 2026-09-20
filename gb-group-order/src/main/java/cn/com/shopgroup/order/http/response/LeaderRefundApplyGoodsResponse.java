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
    // 申请退中数量(本次申请待审核数量, 用户申请时写入, 审核处理完成(同意/拒绝)后置0)
    private Integer applyRefundNum;
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
        // 申请退中数量(本次申请的待审核数量)
        int applyNum = goods.getApplyRefundNum() == null ? 0 : Math.abs(goods.getApplyRefundNum());
        this.applyRefundNum = applyNum;
        int flag = refundFlag == null ? 0 : refundFlag;
        if (applyNum > 0 && (flag == 1 || flag == 2)) {
            // 本次申请数量按退款类型落到对应字段: refundFlag=1(退待收货部分)/2(退已收货部分)
            this.refundNum = flag == 1 ? applyNum : 0;
            this.refundGoodsNum = flag == 2 ? applyNum : 0;
        } else {
            // 兜底(历史数据: 字段上线前已提交的待审核申请, 申请退中数量为0; 或退款类型未知): 按原累计口径返回, 保证数据可展示
            this.refundNum = goods.getRefundNum() == null ? 0 : goods.getRefundNum();
            this.refundGoodsNum = goods.getRefundGoodsNum() == null ? 0 : goods.getRefundGoodsNum();
        }
        // 行退款金额推算: 与本次申请数量同口径(refundFlag=1取退款数量, =2取退货退款数量)
        int num = flag == 1 ? this.refundNum : (flag == 2 ? this.refundGoodsNum : 0);
        int priceCent = MoneyUtil.yuanToCent(goods.getGoodsPrice() == null ? 0D : goods.getGoodsPrice());
        this.refundAmount = MoneyUtil.centToYuan(priceCent * num);
    }
}
