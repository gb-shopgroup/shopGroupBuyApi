package cn.com.shopgroup.order.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 订单核销记录表: 核销成功后落库, 记录订单主要信息/核销商品明细/核销人/核销类型
@Data
@TableName("gb_order_verify_record")
public class GbOrderVerifyRecord {
    // 主键自增
    @TableId(type = IdType.AUTO)
    private Long id;
    // 订单号
    @TableField("order_no")
    private String orderNo;
    // 用户id(下单人)
    @TableField("member_id")
    private Long memberId;
    // 用户昵称(下单人)
    @TableField("nickname")
    private String nickname;
    // 用户手机号(下单人)
    @TableField("mobile")
    private String mobile;
    // 团长id
    @TableField("leader_id")
    private Long leaderId;
    // 团购活动id
    @TableField("group_id")
    private Long groupId;
    // 团购活动名称
    @TableField("group_name")
    private String groupName;
    // 下单自提点id
    @TableField("point_id")
    private Long pointId;
    // 下单自提点名称
    @TableField("point_name")
    private String pointName;
    // 订单实付金额(单位:分)
    @TableField("pay_fee")
    private Integer payFee;
    // 核销码
    @TableField("receipt_code")
    private String receiptCode;
    // 核销类型: 0=团长后台核销, 2=用户扫码核销
    @TableField("verify_type")
    private Integer verifyType;
    // 核销人id: 团长后台核销=团长/店员id, 用户扫码核销=用户memberId
    @TableField("staff_id")
    private Long staffId;
    // 核销人姓名: 团长后台核销=团长/店员姓名, 用户扫码核销=用户昵称
    @TableField("staff_name")
    private String staffName;
    // 核销(实际领取)自提点id
    @TableField("verify_point_id")
    private Long verifyPointId;
    // 核销(实际领取)自提点名称
    @TableField("verify_point_name")
    private String verifyPointName;
    // 核销商品明细(JSON数组, 仅本次核销数量>0的商品行):
    // 每行 goodsId/goodsName/skuNames/goodsPrice(元)/goodsUnit/goodsNum(购买数)/verifyNum(本次核销数)/receiptNum(累计已核销数)
    @TableField("verify_goods_msg")
    private String verifyGoodsMsg;
    // 核销时间(秒级时间戳)
    @TableField("add_time")
    private Integer addTime;
}
