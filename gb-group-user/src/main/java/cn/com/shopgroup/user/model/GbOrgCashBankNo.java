package cn.com.shopgroup.user.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 银行卡信息表
@Data
@TableName("gb_org_cash_bank_no")
public class GbOrgCashBankNo {

    // 银行卡id, 主键自增
    @TableId(type = IdType.AUTO)
    private Long banknoId;
    // 团长id,外键
    @TableField("leader_id")
    private Long leaderId;
    // 银行
    @TableField("bank_name")
    private String bankName;
    // 银行卡
    @TableField("bank_no")
    private String bankNo;
    // 姓名
    @TableField("true_name")
    private String trueName;

}
