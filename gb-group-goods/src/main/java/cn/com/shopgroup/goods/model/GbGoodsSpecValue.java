package cn.com.shopgroup.goods.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 商品规格值表
@Data
@TableName("gb_goods_spec_value")
public class GbGoodsSpecValue {

    // 规格值id,主键自增
    @TableId(type = IdType.AUTO)
    private Long valId;
    // 商品id,外键
    @TableField("goods_id")
    private Long goodsId;
    // 团长id,外键
    @TableField("leader_id")
    private Long leaderId;
    // 规格id,外键
    @TableField("spec_id")
    private Long specId;
    // 规格值
    @TableField("spec_val")
    private String specVal;
    // 是否禁用
    @TableField("is_close")
    private Byte isClose;
    // 添加时间
    @TableField("add_time")
    private Integer addTime;

}
