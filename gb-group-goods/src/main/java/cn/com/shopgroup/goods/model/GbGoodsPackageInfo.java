package cn.com.shopgroup.goods.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 商品包装信息表
@Data
@TableName("gb_goods_package_info")
public class GbGoodsPackageInfo {

    // 包装id,主键自增
    @TableId(type = IdType.AUTO)
    private Long packId;
    // 团长id,外键
    @TableField("leader_id")
    private Long leaderId;
    // 商品id,外键
    @TableField("goods_id")
    private Long goodsId;
    // 包装名称
    @TableField("pack_name")
    private String packName;
    // 销售价格
    @TableField("sales_price")
    private Double salesPrice;
    // 市场价格
    @TableField("market_price")
    private Double marketPrice;
    // 包装数量
    @TableField("pack_num")
    private Integer packNum;
    // 商品单位
    @TableField("goods_unit")
    private String goodsUnit;
    // 是否禁用
    @TableField("is_close")
    private Byte isClose;
    // 添加时间
    @TableField("add_time")
    private Integer addTime;

}
