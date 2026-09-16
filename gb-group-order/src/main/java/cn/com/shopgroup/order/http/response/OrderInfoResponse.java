package cn.com.shopgroup.order.http.response;

import cn.com.shopgroup.common.utils.MoneyUtil;
import cn.com.shopgroup.order.model.GbOrderGoodsInfo;
import cn.com.shopgroup.order.model.GbOrderInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单信息（金额统一为 Double，单位：元）
 * 字段名与实体 GbOrderInfo 完全一致, 仅金额字段由「分」改为「元」
 */
@Data
public class OrderInfoResponse {

    // 订单id,主键自增
    private Long Id;
    // 订单号
    private String orderNo;
    // 用户id,外键
    private Long memberId;
    // 团购id,外键
    private Long groupId;
    // 团长id,外键
    private Long leaderId;
    // 数据隔离id
    private Integer isolationId;
    // 店铺id,冗余
    private Long shopId;
    // 店铺名称,冗余
    private String shopName;
    // 手机号码,冗余
    private String mobile;
    // 微信昵称,冗余
    private String nickname;
    // 微信头像,冗余
    private String avatar;
    // openid,冗余
    private String openid;
    // 团购名称,冗余
    private String groupName;
    // 团购价格,冗余，单位元
    private Double groupPrice;
    // 订单价格,冗余（帮卖价格），单位元
    private Double orderPrice;
    // 收款账户id,外键
    private Long busId;
    // 易宝商户编号,冗余
    private String merchantNo;
    // 订单状态:0 待支付,1 待收货 2 部分收货 3 已提货 4 已退款, 5 售后 6 已取消
    private Integer status;
    // 支付金额，单位元
    private Double payFee;
    // 支付时间
    private Integer payTime;
    // 支付流水号,来自支付接口
    private String payNo;
    // 退款金额，单位元
    private Double refundFee;
    // 退款时间
    private Integer refundTime;
    // 退款流水号,来自退款接口
    private String refundNo;
    // 退款人员
    private String refundStaff;
    // 拒退理由
    private String refundReason;
    // 收货方式,1=自提2=邮寄
    private Byte receiptType;
    // 收货姓名,自提和邮寄都需要
    private String trueName;
    // 收货电话,自提和邮寄都需要
    private String telephone;
    // 收货时间
    private Integer receiptTime;
    // 核销时间
    private Integer verifyTime;
    // 自提点id,自提/外键
    private Long pointId;
    // 自提点名称,自提
    private String pointName;
    // 详细地址,自提
    private String pointAddress;
    // 核销码,自提
    private String receiptCode;
    // 核销人员id
    private Long staffId;
    // 核销人员姓名
    private String staffName;
    // 自提点id,实际领取自提点
    private Long pointId2;
    // 自提点名称,实际领取自提点
    private String pointName2;
    // 订单备注,c端客户使用
    private String remark;
    // 下单时间
    private Integer addTime;
    // 更新时间
    private Integer updateTime;
    // 微信发货是否已调用,0=未调用,1=已调用
    private Integer wxShipment;
    // 确认收货操作标记,0=未操作,1=已操作
    private Integer clickConfirmFlag;
    // 订单商品列表(订单表以外的关联字段)
    private List<GbOrderGoodsInfo> goodsInfoList;

    public OrderInfoResponse() {
    }

    public OrderInfoResponse(GbOrderInfo data) {

        this.Id = data.getId();
        this.orderNo = data.getOrderNo();
        this.memberId = data.getMemberId();
        this.groupId = data.getGroupId();
        this.leaderId = data.getLeaderId();
        this.isolationId = data.getIsolationId();
        this.shopId = data.getShopId();
        this.shopName = data.getShopName();
        this.mobile = data.getMobile();
        this.nickname = data.getNickname();
        this.avatar = data.getAvatar();
        this.openid = data.getOpenid();
        this.groupName = data.getGroupName();
        this.groupPrice = data.getGroupPrice();
        this.orderPrice = data.getOrderPrice();
        this.busId = data.getBusId();
        this.merchantNo = data.getMerchantNo();
        this.status = data.getStatus();
        // 金额字段: 分 -> 元
        this.payFee = MoneyUtil.centToYuan(data.getPayFee());
        this.payTime = data.getPayTime();
        this.payNo = data.getPayNo();
        // 金额字段: 分 -> 元
        this.refundFee = MoneyUtil.centToYuan(data.getRefundFee());
        this.refundTime = data.getRefundTime();
        this.refundNo = data.getRefundNo();
        this.refundStaff = data.getRefundStaff();
        this.refundReason = data.getRefundReason();
        this.receiptType = data.getReceiptType();
        this.trueName = data.getTrueName();
        this.telephone = data.getTelephone();
        this.receiptTime = data.getReceiptTime();
        this.verifyTime = data.getVerifyTime();
        this.pointId = data.getPointId();
        this.pointName = data.getPointName();
        this.pointAddress = data.getPointAddress();
        this.receiptCode = data.getReceiptCode();
        this.staffId = data.getStaffId();
        this.staffName = data.getStaffName();
        this.pointId2 = data.getPointId2();
        this.pointName2 = data.getPointName2();
        this.remark = data.getRemark();
        this.addTime = data.getAddTime();
        this.updateTime = data.getUpdateTime();
        this.wxShipment = data.getWxShipment();
        this.clickConfirmFlag = data.getClickConfirmFlag();
        this.goodsInfoList = data.getGoodsInfoList();
    }

    // 列表转化
    public static List<OrderInfoResponse> getResponseList(List<GbOrderInfo> lists) {

        List<OrderInfoResponse> data = new ArrayList<>();
        if (lists == null) {
            return data;
        }
        for (GbOrderInfo item : lists) {
            data.add(new OrderInfoResponse(item));
        }
        return data;
    }
}
