package cn.com.shopgroup.order.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 订单商品信息表
@Data
@TableName("gb_order_goods_info")
public class GbOrderGoodsInfo {
    // id,主键/外键
    @TableId(type = IdType.AUTO)
    private Long Id;
    @TableField("order_no")
    private String orderNo;
    // 商品id,外键
    @TableField("goods_id")
    private Long goodsId;
    // 商品名称
    @TableField("goods_name")
    private String goodsName;
    // 商品价格
    @TableField("goods_price")
    private Double goodsPrice;
    // 商品数量
    @TableField("goods_num")
    private Integer goodsNum;
    // 收货数量
    @TableField("receipt_num")
    private Integer receiptNum;
    //售后（退款）状态 0 无 1 待审核 2 同意 3 不同意
    @TableField("apply_refund")
    private Integer applyRefund;
    //退货数量
    @TableField("refund_goods_num")
    private Integer refundGoodsNum;
    // 商品单位
    @TableField("goods_unit")
    private String goodsUnit;
    // 商品图片
    @TableField("goods_img")
    private String goodsImg;

    // 商品类型,1普通商品2称重商品
    @TableField("goods_type")
    private Byte goodsType;
    // 包装id,外键(可能不存在)
    @TableField("pack_id")
    private Long packId;
    // 包装名称
    @TableField("pack_name")
    private String packName;
    // 包装数量
    @TableField("pack_num")
    private Integer packNum;
    // skuids, 规格1+规格2+......
    @TableField("sku_ids")
    private String skuIds;
    // sku名称, 规格1+规格2+......
    @TableField("sku_names")
    private String skuNames;
    // skuid, 外键(可能不存在)
    @TableField("sku_id")
    private Long skuId;
    //下单时间
    @TableField("add_time")
    private Integer addTime;
    //更改时间
    @TableField("update_time")
    private Integer updateTime;

}
