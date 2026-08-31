package cn.com.shopgroup.goods.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 团购帮卖设置表
@Data
@TableName("gb_group_sales_set")
public class GbGroupSalesSet {

    // 团购id,主键/外键
    @TableField("group_id")
    private Long groupId;
    // 商品id,主键/外键
    @TableField("goods_id")
    private Long goodsId;
    // skuid,主键/外键
    @TableField("sku_id")
    private Long skuId;
    // 团长id,外键
    @TableField("leader_id")
    private Long leaderId;
    // 帮卖类型,1=固定佣金2=自由定价
    @TableField("sales_type")
    private Byte salesType;
    // 帮卖价格,固定佣金
    @TableField("sales_price")
    private Double salesPrice;
    // 帮卖佣金,固定佣金
    @TableField("commission")
    private Double commission;
    // 最低帮卖价格,自由定价
    @TableField("min_price")
    private Double minPrice;
    // 最高帮卖价格,自由定价
    @TableField("max_price")
    private Double maxPrice;
    // 佣金分账时机,1支付完毕后2收货完毕后
    @TableField("trigger_event")
    private Byte triggerEvent;
    // 是否禁用
    @TableField("is_close")
    private Byte isClose;
    // 添加人员id
    @TableField("staff_id")
    private Long staffId;
    // 添加人员姓名
    @TableField("staff_name")
    private String staffName;
    // 添加时间
    @TableField("add_time")
    private Integer addTime;

}
