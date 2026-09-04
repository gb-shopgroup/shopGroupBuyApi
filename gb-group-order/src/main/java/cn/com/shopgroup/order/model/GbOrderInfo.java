package cn.com.shopgroup.order.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

// 订单信息表
@Data
@TableName("gb_order_info")
public class GbOrderInfo {
    // 订单id,主键自增
    @TableId(type = IdType.AUTO)
    private Long Id;
    @TableField("order_no")
    private String orderNo;
    // 用户id,外键
    @TableField("member_id")
    private Long memberId;
    // 团购id,外键
    @TableField("group_id")
    private Long groupId;
    // 团长id,外键
    @TableField("leader_id")
    private Long leaderId;
    // 数据隔离id
    @TableField("isolation_id")
    private Integer isolationId;
    // 店铺id,冗余
    @TableField("shop_id")
    private Long shopId;
    // 店铺名称,冗余
    @TableField("shop_name")
    private String shopName;
    // 手机号码,冗余
    @TableField("mobile")
    private String mobile;
    // 微信昵称,冗余
    @TableField("nickname")
    private String nickname;
    // 微信头像,冗余
    @TableField("avatar")
    private String avatar;
    // openid,冗余
    @TableField("openid")
    private String openid;
    // 团购名称,冗余
    @TableField("group_name")
    private String groupName;
    // 团购价格,冗余
    @TableField("group_price")
    private Double groupPrice;
    // 订单价格,冗余（帮卖价格）
    @TableField("order_price")
    private Double orderPrice;

    // 收款账户id,外键
    @TableField("bus_id")
    private Long busId;
    // 易宝商户编号,冗余
    @TableField("merchant_no")
    private String merchantNo;
    // 订单状态:0 待支付,1 待收货 2 部分收货 3 已提货 4 已退款, 5 售后 6 已取消
    @TableField("status")
    private Integer status;
    // 支付金额,单位：分
    @TableField("pay_fee")
    private Integer payFee;
    // 支付时间
    @TableField("pay_time")
    private Integer payTime;
    // 支付流水号,来自支付接口
    @TableField("pay_no")
    private String payNo;
    // 退款金额,单位：分
    @TableField("refund_fee")
    private Integer refundFee;
    // 退款时间
    @TableField("refund_time")
    private Integer refundTime;
    // 退款流水号,来自退款接口
    @TableField("refund_no")
    private String refundNo;

    // 退款人员
    @TableField("refund_staff")
    private String refundStaff;
    // 拒退理由
    @TableField("refund_reason")
    private String refundReason;

    // 收货方式,1=自提2=邮寄
    @TableField("receipt_type")
    private Byte receiptType;
    // 收货姓名,自提和邮寄都需要
    @TableField("true_name")
    private String trueName;
    // 收货电话,自提和邮寄都需要
    @TableField("telephone")
    private String telephone;
/*    // 是否部分收货
    @TableField("is_part_receipt")
    private Byte isPartReceipt;*/
    // 收货时间
    @TableField("receipt_time")
    private Integer receiptTime;

    // 核销时间
    @TableField("verify_time")
    private Integer verifyTime;

    // 自提点id,自提/外键
    @TableField("point_id")
    private Long pointId;
    // 自提点名称,自提
    @TableField("point_name")
    private String pointName;
    // 详细地址,自提
    @TableField("point_address")
    private String pointAddress;

    // 核销码,自提
    @TableField("receipt_code")
    private String receiptCode;
    // 核销人员id
    @TableField("staff_id")
    private Long staffId;
    // 核销人员姓名
    @TableField("staff_name")
    private String staffName;
    // 自提点id,实际领取自提点
    @TableField("point_id2")
    private Long pointId2;
    // 自提点名称,实际领取自提点
    @TableField("point_name2")
    private String pointName2;
    // 订单备注,c端客户使用
    @TableField("remark")
    private String remark;
    // 下单时间
    @TableField("add_time")
    private Integer addTime;
    @TableField("update_time")
    private Integer updateTime;
    // 微信发货是否已调用,0=未调用,1=已调用
    @TableField("wx_shipment")
    private Integer wxShipment;
    // 确认收货操作标记,0=未操作,1=已操作
    @TableField("click_confirm_flag")
    private Integer clickConfirmFlag;

    // 不是订单表里面的字段哦
    @TableField(exist = false)
    private List<GbOrderGoodsInfo> goodsInfoList;

}
