package cn.com.shopgroup.goods.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 团购帮卖价格表
@Data
@TableName("gb_group_sales_price")
public class GbGroupSalesPrice {

    // 团购id,主键/外键
    @TableField("group_id")
    private Long groupId;
    // 商品id,主键/外键
    @TableField("goods_id")
    private Long goodsId;
    // skuid,主键/外键
    @TableField("sku_id")
    private Long skuId;
    // 团长id,主键/外键
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

}
