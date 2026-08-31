package cn.com.shopgroup.order.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 团长自提点商品统计报表
@Data
@TableName("gb_report_point_goods")
public class GbReportPointGoods {

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
    // 商品id,外键
    @TableField("goods_id")
    private Long goodsId;
    // 商品名称,冗余
    @TableField("goods_name")
    private String goodsName;
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
    // 商品总量,考虑包装问题
    @TableField("num_total")
    private Integer numTotal;
    // 商品单位
    @TableField("goods_unit")
    private String goodsUnit;
    // 添加时间
    @TableField("add_time")
    private Integer addTime;

}
