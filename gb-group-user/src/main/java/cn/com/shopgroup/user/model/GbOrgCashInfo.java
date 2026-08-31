package cn.com.shopgroup.user.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 团长提现信息表
@Data
@TableName("gb_org_cash_info")
public class GbOrgCashInfo {

    // 提现id
    @TableId(type = IdType.AUTO)
    private Long cashId;
    // 账户id,外键
    @TableField("bus_id")
    private Long busId;
    // 团长id,外键
    @TableField("leader_id")
    private Long leaderId;
    // 提现金额
    @TableField("cash_fee")
    private Integer cashFee;
    // 提现银行
    @TableField("cash_bank")
    private String cashBank;
    // 提现银行卡
    @TableField("cash_bank_no")
    private String cashBankNo;
    // 提现人姓名
    @TableField("true_name")
    private String trueName;
    // 提现状态
    @TableField("cash_status")
    private Byte cashStatus;
    // 提现流水号
    @TableField("cash_no")
    private String cashNo;
    // 到账时间
    @TableField("cash_time")
    private Integer cashTime;
    // 申请时间
    @TableField("add_time")
    private Integer addTime;

}
