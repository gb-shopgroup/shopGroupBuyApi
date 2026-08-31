package cn.com.shopgroup.order.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 团长自提点统计报表
@Data
@TableName("gb_report_point_info")
public class GbReportPointInfo {

    // 报表id,主键自增
    @TableId(type = IdType.AUTO)
    private Long reportId;
    // 团长id,外键
    @TableField("leader_id")
    private Long leaderId;
    // 自提点id,外键
    @TableField("point_id")
    private Long pointId;
    // 自提点名称,冗余
    @TableField("point_name")
    private String pointName;
    // 统计名称,月份
    @TableField("report_name")
    private String reportName;
    // 开始时间
    @TableField("start_time")
    private Integer startTime;
    // 结束时间
    @TableField("end_time")
    private Integer endTime;
    // 订单总量
    @TableField("order_total")
    private Integer orderTotal;
    // 订单总额
    @TableField("sales_total")
    private Double salesTotal;
    // 添加时间
    @TableField("add_time")
    private Integer addTime;

}
