package cn.com.shopgroup.goods.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

// 团购活动信息表
@Data
@TableName("gb_group_activity_info")
public class GbGroupActivityInfo {

    // 团购id,主键自增
    @TableId(type = IdType.AUTO)
    private Long groupId;
    // 团购分类id,外键
    @TableField("cat_id")
    private Long catId;
    // 团长id,外键
    @TableField("leader_id")
    private Long leaderId;
    // 数据隔离id
    @TableField("isolation_id")
    private Integer isolationId;
    // 商品提货方式,1自提2邮递
    @TableField("pickup_style")
    private Byte pickupStyle;
    // 团购名称
    @TableField("group_name")
    private String groupName;
    // 团购主图
    @TableField("group_img")
    private String groupImg;
    // 团购主图
    @TableField("group_img2")
    private String groupImg2;
    // 团购主图
    @TableField("group_img3")
    private String groupImg3;
    // 团购价格/最小价格
    @TableField("group_price")
    private Double groupPrice;
    // 团购价格/最大价格
    @TableField("group_price2")
    private Double groupPrice2;
    // 市场价格/划线价格
    @TableField("market_price")
    private Double marketPrice;

    // 开团时间
    @TableField("start_time")
    private Integer startTime;
    // 结束时间
    @TableField("end_time")
    private Integer endTime;
    // 团购介绍
    @TableField("group_info")
    private String groupInfo;
    // 实际订单数量
    @TableField("order_total")
    private Integer orderTotal;
    // 虚拟订单数量
    @TableField("virtual_order")
    private Integer virtualOrder;
    // 是否禁用,0上线1下线
    @TableField("is_close")
    private Byte isClose;
    // 排列顺序,平台算法
    @TableField("sort_order")
    private Integer sortOrder;
    // 添加人员id
    @TableField("staff_id")
    private Long staffId;
    // 添加人员姓名
    @TableField("staff_name")
    private String staffName;
    // 平台审核 0 待审核 1 通过 2 不通过
    @TableField("is_check")
    private Byte isCheck;
    // 审核备注
    @TableField("check_remark")
    private String checkRemark;
    // 添加时间
    @TableField("add_time")
    private Integer addTime;

    // 团购商品列表
    // 不是订单表里面的字段哦
    @TableField(exist = false)
    private List<GbGroupActivityGoods> lists;


}
