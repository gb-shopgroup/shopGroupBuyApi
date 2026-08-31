package cn.com.shopgroup.goods.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 团购帮卖信息表
@Data
@TableName("gb_group_sales_info")
public class GbGroupSalesInfo {

    // 帮卖id,主键自增
    @TableId(type = IdType.AUTO)
    private Long salesId;
    // 团购id,外键
    @TableField("group_id")
    private Long groupId;
    // 团长id,外键
    @TableField("leader_id")
    private Long leaderId;
    // 数据隔离id
    @TableField("isolation_id")
    private Integer isolationId;
    // 是否禁用
    @TableField("is_close")
    private Byte isClose;
    // 添加人员
    @TableField("staff_id")
    private Long staffId;
    // 添加人员姓名
    @TableField("staff_name")
    private String staffName;
    // 添加时间
    @TableField("add_time")
    private Long addTime;

}
