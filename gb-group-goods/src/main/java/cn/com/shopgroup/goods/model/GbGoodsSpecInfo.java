package cn.com.shopgroup.goods.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

// 商品规格信息表
@Data
@TableName("gb_goods_spec_info")
public class GbGoodsSpecInfo {

    // 规格id,主键自增
    @TableId(type = IdType.AUTO)
    private Long specId;
    // 商品id,外键
    @TableField("goods_id")
    private Long goodsId;
    // 团长id,外键
    @TableField("leader_id")
    private Long leaderId;
    // 规格名称
    @TableField("spec_name")
    private String specName;
    // 是否设置价格
    @TableField("is_price")
    private Byte isPrice;
    // 是否设置库存
    @TableField("is_stock")
    private Byte isStock;
    // 排列顺序
    @TableField("sort_order")
    private Integer sortOrder;
    // 是否禁用
    @TableField("is_close")
    private Byte isClose;
    // 添加时间
    @TableField("add_time")
    private Integer addTime;

    // 规格值列表
    @TableField(exist = false)
    private List<GbGoodsSpecValue> specValueList;


}
