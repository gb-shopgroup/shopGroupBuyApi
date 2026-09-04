package cn.com.shopgroup.order.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 退款原因配置表
@Data
@TableName("gb_refund_reason")
public class GbRefundReason {
    // 原因id,主键自增
    @TableId(type = IdType.AUTO)
    private Long id;
    // 退款原因文本
    @TableField("reason")
    private String reason;
    // 排序,数值越小越靠前
    @TableField("sort")
    private Integer sort;
    // 状态,0=停用,1=启用
    @TableField("status")
    private Integer status;
    // 创建时间,秒级时间戳
    @TableField("add_time")
    private Integer addTime;
    // 更新时间,秒级时间戳
    @TableField("update_time")
    private Integer updateTime;
}
