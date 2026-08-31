package cn.com.shopgroup.goods.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 商品库存补货表
@Data
@TableName("gb_goods_restock_info")
public class GbGoodsRestockInfo {

    // 补货id,主键自增
    @TableId(type = IdType.AUTO)
    private Long restockId;
    // 团长id,外键
    @TableField("leader_id")
    private Long leaderId;
    // 商品id,外键
    @TableField("goods_id")
    private Long goodsId;
    // skuid,外键
    @TableField("sku_id")
    private Long skuId;
    // 补货类型,1=减少2=增加
    @TableField("restock_type")
    private Byte restockType;
    // 商品数量,补货数量
    @TableField("goods_num")
    private Integer goodsNum;
    // 商品单位
    @TableField("goods_unit")
    private String goodsUnit;
    // 单据状态,1申请2审批3作废
    @TableField("restock_status")
    private Byte restockStatus;
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
