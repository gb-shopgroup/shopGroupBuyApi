package cn.com.shopgroup.order.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 订单分账/佣金信息表
@Data
@TableName("gb_order_commission_info")
public class GbOrderCommissionInfo {

    // 明细id,主键自增
    @TableId(type = IdType.AUTO)
    private Long commId;
    // 订单id,外键
    @TableField("order_id")
    private Long orderId;
    // 分账方类型,1=商户货款2=平台抽成3=分账佣金4=帮卖佣金
    @TableField("comm_type")
    private Byte commType;
    // 分账方id
    @TableField("comm_user")
    private Long commUser;
    // 分账方姓名,冗余
    @TableField("comm_name")
    private String commName;
    // 订单金额,单位:分
    @TableField("order_fee")
    private Integer orderFee;
    // 分账金额,单位:分
    @TableField("comm_fee")
    private Integer commFee;
    // 分账状态,0未分账,1已分账
    @TableField("comm_status")
    private Byte commStatus;
    // 分账时间,请求接口时间
    @TableField("comm_time")
    private Integer commTime;
    // 分账流水号,来自分账接口
    @TableField("comm_no")
    private String commNo;
    // 分账备注,系统自动备注
    @TableField("comm_remark")
    private String commRemark;
    // 添加时间
    @TableField("add_time")
    private Integer addTime;

}
