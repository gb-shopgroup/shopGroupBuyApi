package cn.com.shopgroup.order.http.response;

import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.order.model.GbOrderGoodsInfo;
import cn.com.shopgroup.order.model.GbOrderInfo;
import lombok.Data;

import java.util.List;

@Data
public class RefundOrderInfoResponse {

    // 订单信息
    private String orderNo;
    private String orderTime;
    private Double orderPrice;

    // 团长信息和店铺名称
    private Long leaderId;
    private Long shopId;
    private String shopName;

    // 团购信息
    private Long groupId;
    private String groupName;
    // 拒绝退款理由
    private String reason;
    private Integer receiptTime;

    // 微信支付交易号
    // 微信发货和收货都需要这个
    private String payno;

    // 订单状态:0 待支付,1 待收货 2 部分收货 3 已提货 4 已退款, 5 售后 6 已取消
    private Integer status;

    // 收货方式：1=自提,2=邮寄
    private Byte receiptType;
    private String trueName;
    private String telephone;

    // 自提点信息
    private Long pointId;
    private String pointName;
    private String pointAddress;
    private String receiptCode;

    // 下单用户信息（内部调用使用）
    private String nickname;
    private String mobile;

    // 微信发货是否已调用:0=未调用,1=已调用
    private Integer wxShipment;

    // 确认收货操作标记:0=未操作,1=已操作
    private Integer clickConfirmFlag;
    // 订单可退款商品列表
    private List<RefundOrderGoodsResponse> refundGoods;

    public RefundOrderInfoResponse() {

    }

    public RefundOrderInfoResponse(GbOrderInfo data) {

        // 订单信息
        this.orderNo = data.getOrderNo();
        this.orderTime = TimeUtils.getFormatTimeStamp(data.getAddTime());
        this.orderPrice = data.getOrderPrice();

        // 团长信息和店铺名称
        this.leaderId = data.getLeaderId();
        this.shopId = data.getShopId();
        this.shopName = data.getShopName();

        // 团购信息
        this.groupId = data.getGroupId();
        this.groupName = data.getGroupName();

        this.payno = data.getPayNo();
        // 拒绝退款理由
        this.reason = data.getRefundReason();
        this.receiptTime = data.getReceiptTime();
        this.status = data.getStatus();
        // 商品信息
        List<GbOrderGoodsInfo> goodsList = data.getGoodsInfoList();
        // 收货方式：1=自提, 2=邮寄
        this.receiptType = data.getReceiptType();
        this.trueName = data.getTrueName();
        this.telephone = data.getTelephone();

        // 自提点信息
        this.pointId = data.getPointId();
        this.pointName = data.getPointName();
        this.pointAddress = data.getPointAddress();
        this.receiptCode = data.getReceiptCode();

        // 下单用户信息（内部调用使用）
        this.nickname = data.getNickname();
        this.mobile = data.getMobile();

        // 微信发货是否已调用:0=未调用,1=已调用
        this.wxShipment = data.getWxShipment();

        // 确认收货操作标记:0=未操作,1=已操作
        this.clickConfirmFlag = data.getClickConfirmFlag();
    }

}
