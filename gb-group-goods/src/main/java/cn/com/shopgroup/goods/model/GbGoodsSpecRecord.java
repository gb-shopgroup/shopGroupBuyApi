package cn.com.shopgroup.goods.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 商品与规格关联表
@Data
@TableName("gb_goods_spec_record")
public class GbGoodsSpecRecord {

    // id,主键自增
    @TableId(type = IdType.AUTO)
    private Long id;
    // 团长id,外键
    @TableField("leader_id")
    private Long leaderId;
    // 商品id,外键
    @TableField("goods_id")
    private Long goodsId;
    // 规格id,主键
    @TableField("spec_id")
    private Long specId;
}
