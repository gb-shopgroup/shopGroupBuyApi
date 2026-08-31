package cn.com.shopgroup.order.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

// 订单交易流水表
@Data
@TableName("order_transaction_log")
public class OrderTransactionLog {
    // 主键自增
    @TableId(type = IdType.AUTO)
    private Long id;
    // 关联订单号
    @TableField("order_no")
    private String orderNo;
    // 交易流水号
    @TableField("transaction_no")
    private String transactionNo;
    // 交易前订单总金额
    @TableField("pre_order_amount")
    private Double preOrderAmount;
    // 本次支付金额
    @TableField("pay_amount")
    private Double payAmount;
    // 本次退款金额
    @TableField("refund_amount")
    private Double refundAmount;
    // 支付状态: pending/success/failed/refunded
    @TableField("pay_status")
    private String payStatus;
    // 支付方式: wechat/zfb
    @TableField("pay_method")
    private String payMethod;
    // 操作人ID
    @TableField("operator_id")
    private Long operatorId;
    // 操作人姓名
    @TableField("operator_name")
    private String operatorName;
    // 添加时间
    @TableField("add_time")
    private Integer addTime;
    // 备注说明
    @TableField("remark")
    private String remark;
}
