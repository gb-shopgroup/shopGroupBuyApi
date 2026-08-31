package cn.com.shopgroup.goods.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 团购商品信息表
@Data
@TableName("gb_group_activity_goods")
public class GbGroupActivityGoods {

    // 团购id,外键
    @TableField("group_id")
    private Long groupId;
    // 商品id,外键
    @TableField("goods_id")
    private Long goodsId;

    // 商品名称,冗余
    @TableField("goods_name")
    private String goodsName;
    // 商品类型,冗余
    @TableField("goods_type")
    private Byte goodsType;
    // 商品主图,冗余
    @TableField("group_img")
    private String groupImg;
    // 团购价格
    @TableField("group_price")
    private Double groupPrice;
    // 市场价格
    @TableField("market_price")
    private Double marketPrice;

}
