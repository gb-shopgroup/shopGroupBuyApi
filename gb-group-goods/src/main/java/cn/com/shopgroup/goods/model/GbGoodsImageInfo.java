package cn.com.shopgroup.goods.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 商品图片表
@Data
@TableName("gb_goods_image_info")
public class GbGoodsImageInfo {

    // 图片id,主键自增
    @TableId(type = IdType.AUTO)
    private Long imgId;
    // 商品id,外键
    @TableField("goods_id")
    private Long goodsId;
    // 图片类型,1主图, 2 banner图
    @TableField("goods_type")
    private Byte goodsType;
    // 图片地址
    @TableField("goods_img")
    private String goodsImg;

}
