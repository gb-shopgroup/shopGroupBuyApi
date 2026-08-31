package cn.com.shopgroup.order.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 团长分账统计报表
@Data
@TableName("gb_report_business_info")
public class GbReportBusinessInfo {

    // 报表id
    @TableId(type = IdType.AUTO)
    private Long reportId;
    // 团长id
    @TableField("leader_id")
    private Long leaderId;
    // 账户id
    @TableField("bus_id")
    private Long busId;
    // 账户名称
    @TableField("bus_name")
    private String busName;
    // 统计名称
    @TableField("report_name")
    private String reportName;
    // 开始时间
    @TableField("start_time")
    private Integer startTime;
    // 结束时间
    @TableField("end_time")
    private Integer endTime;
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
    // 添加时间
    @TableField("add_time")
    private Integer addTime;

}
