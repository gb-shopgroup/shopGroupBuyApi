package cn.com.shopgroup.order.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

// 团长下的每个收款账户月收入表
@Data
@TableName("gb_leader_merchant_monthly_amount")
public class GbLeaderMerchantMonthlyAmount {

    // 记录id,主键自增
    @TableId(type = IdType.AUTO)
    private Long id;
    // 团长id,外键,冗余(便于团长端聚合)
    @TableField("leader_id")
    private Long leaderId;
    //账户id
    @TableField("bus_id")
    private Long busId;
    // 商户号
    @TableField("merchant_no")
    private String merchantNo;
    // 年_月,如2026-10
    @TableField("order_year_month")
    private String yearMonth;
    // 每个月的总收入(单位: 元)
    @TableField("total_amount")
    private BigDecimal totalAmount;

}
