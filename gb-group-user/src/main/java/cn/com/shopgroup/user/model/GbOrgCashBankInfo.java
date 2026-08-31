package cn.com.shopgroup.user.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("gb_org_cash_bank_info")
public class GbOrgCashBankInfo {

    // 银行id
    @TableId(type = IdType.AUTO)
    private Long bankId;
    // 银行名称
    @TableField("band_name")
    private String bankName;
    // 是否禁用
    @TableField("is_close")
    private Byte isClose;
    // 添加时间
    @TableField("add_time")
    private Integer addTime;

}
