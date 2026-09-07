package cn.com.shopgroup.order.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 订单退款记录信息表
@Data
@TableName("gb_order_goods_refund_record")
public class GbOrderGoodsRefundRecord {
    // 主键自增
    @TableId(type = IdType.AUTO)
    private Long id;
    // 订单号
    @TableField("order_no")
    private String orderNo;
    //退款商品描述
    @TableField("refund_goods_msg")
    private String refundGoodsMsg;
    // 退款类型: 1=退款(退待收货部分) 2=退货退款(退已收货部分); 团长端审核未回传类型时, 拒绝恢复流程据此兜底
    @TableField("refund_flag")
    private Integer refundFlag;
    // 本次申请退款金额(单位:分); 团长端审核未回传金额时, 拒绝恢复流程据此兜底
    @TableField("refund_amount")
    private Integer refundAmount;
    // 操作人id
    @TableField("operate_id")
    private Long operateId;
    // 操作人姓名
    @TableField("operate_name")
    private String operateName;
    //状态 0 待审核 1 同意 2不同意
    @TableField("is_agree")
    private Integer isAgree;
    // 申请原因
    @TableField("action_reason")
    private String actionReason;
    // 补充原因
    @TableField("extra_reason")
    private String extraReason;
    // 添加时间
    @TableField("add_time")
    private Integer addTime;
}
