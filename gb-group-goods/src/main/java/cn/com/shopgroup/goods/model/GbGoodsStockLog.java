package cn.com.shopgroup.goods.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 商品库存日志表
@Data
@TableName("gb_goods_stock_log")
public class GbGoodsStockLog {

    // 日志id,主键自增
    @TableId(type = IdType.AUTO)
    private Long logId;
    // 团长id,外键
    @TableField("leader_id")
    private Long leaderId;
    // 商品id,外键
    @TableField("goods_id")
    private Long goodsId;
    // skuid,外键
    @TableField("sku_id")
    private Long skuId;
    // 变化类型,1=减少2=增加
    @TableField("log_type")
    private Byte logType;
    // 商品数量
    @TableField("goods_num")
    private Integer goodsNum;
    // 商品单位
    @TableField("goods_unit")
    private String goodsUnit;
    // 关联单据类型,1订单2补货
    @TableField("data_type")
    private Byte dataType;
    // 关联单据id,订单id或者补货id
    @TableField("data_id")
    private Long dataId;
    // 添加时间
    @TableField("add_time")
    private Integer addTime;

}
