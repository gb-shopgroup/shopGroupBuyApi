package cn.com.shopgroup.goods.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 商品sku信息表
@Data
@TableName("gb_goods_sku_info")
public class GbGoodsSkuInfo {

    // skuid,主键自增
    @TableId(type = IdType.AUTO)
    private Long skuId;
    // 商品id,外键
    @TableField("goods_id")
    private Long goodsId;
    // 团长id,外键
    @TableField("leader_id")
    private Long leaderId;
    // skuids,规格1+规格2+......
    @TableField("sku_ids")
    private String skuIds;
    // sku名称,规格1+规格2+......
    @TableField("sku_names")
    private String skuNames;
    // 进货价格
    @TableField("cost_price")
    private Double costPrice;
    // 销售价格
    @TableField("sales_price")
    private Double salesPrice;
    // 市场价格
    @TableField("market_price")
    private Double marketPrice;
    // 商品库存
    @TableField("goods_num")
    private Integer goodsNum;
    // 商品包装
    @TableField("pack_num")
    private Integer packNum;
    // 商品单位
    @TableField("goods_unit")
    private String goodsUnit;
    // 商品图片
    @TableField("goods_img")
    private String goodsImg;
    // 是否禁用
    @TableField("is_close")
    private Byte isClose;
    // 添加时间
    @TableField("add_time")
    private Integer addTime;

}
