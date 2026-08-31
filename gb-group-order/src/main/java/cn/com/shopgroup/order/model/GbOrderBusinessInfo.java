package cn.com.shopgroup.order.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 订单收款账户信息表
@Data
@TableName("gb_order_business_info")
public class GbOrderBusinessInfo {

    // id,主键
    @TableId(type = IdType.AUTO)
    private Long Id;
    // 订单号
    @TableField("order_no")
    private String orderNo;
    // 账户id,外键
    @TableField("bus_id")
    private Long busId;
    // 易宝商户编号,冗余
    @TableField("merchant_no")
    private String merchantNo;

    // 团长id,外键
    @TableField("leader_id")
    private Long leaderId;
    // 团购名称,冗余
    @TableField("group_name")
    private String groupName;

    // 订单编号,冗余
    @TableField("order_sn")
    private String orderSn;
    // 是否发货,微信发货
    @TableField("is_send")
    private Byte isSend;
    // 微信单号,微信发货
    @TableField("transaction_id")
    private String transactionId;
    // openid,微信发货
    @TableField("openid")
    private String openid;
    // 发货时间,微信发货
    @TableField("send_time")
    private Integer sendTime;
    // 是否解冻,微信冻结
    @TableField("is_unfreeze")
    private Byte isUnfreeze;
    // 解冻时间,微信冻结
    @TableField("unfreeze_time")
    private Integer unfreezeTime;
    // 是否分账
    @TableField("is_divide")
    private Byte isDivide;
    // 分账状态
    @TableField("divide_status")
    private String divideStatus;
    // 分账时间
    @TableField("divide_time")
    private Integer divideTime;
    // 分账流水号
    @TableField("divide_no")
    private String divideNo;
    // 订单金额
    @TableField("order_fee")
    private Integer orderFee;
    // 实到金额
    @TableField("received_fee")
    private Integer receivedFee;
    // 分账金额
    @TableField("bus_fee")
    private Integer busFee;
    // 平台服务费
    @TableField("service_fee")
    private Integer serviceFee;
    // 其他佣金
    @TableField("other_fee")
    private Integer otherFee;
    // 数据状态: 0=已支付(支付回调),1=已发货(定时任务),2=已解冻(t+2),3=已分账(定时任务),4=已提现(用户申请),5=已退款(用户申请)
    @TableField("comm_status")
    private Byte commStatus;
    // 添加时间(下单时间)
    @TableField("add_time")
    private Integer addTime;


}
