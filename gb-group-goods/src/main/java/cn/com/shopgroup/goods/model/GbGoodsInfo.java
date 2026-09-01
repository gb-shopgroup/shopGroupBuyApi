package cn.com.shopgroup.goods.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// 商品信息表
@Data
@TableName("gb_goods_info")
public class GbGoodsInfo {

    // 商品id,主键自增
    @TableId(type = IdType.AUTO)
    private Long goodsId;
    // 分类id,外键
    @TableField("cat_id")
    private Long catId;
    // 团长id,外键
    @TableField("leader_id")
    private Long leaderId;
    // 商品类型,1普通商品2称重商品
    @TableField("goods_type")
    private Byte goodsType;
    // 商品名称
    @TableField("goods_name")
    private String goodsName;
    // 商品主图
    @TableField("goods_img")
    private String goodsImg;
    // 进货价格
    @TableField("cost_price")
    private Double costPrice;
    // 销售价格
    @TableField("sales_price")
    private Double salesPrice;
    // 市场价格
    @TableField("market_price")
    private Double marketPrice;

    // 是否启用库存, 1=启用
    @TableField("is_stock")
    private Byte isStock;
    // 商品库存,总库存
    @TableField("goods_num")
    private Integer goodsNum;

    // 是否启用限购, 1=启用
    @TableField("is_limit")
    private Byte isLimit;
    // 限购数量
    @TableField("limit_num")
    private Integer limitNum;

    // 商品单位
    @TableField("goods_unit")
    private String goodsUnit;
    // 商品介绍
    @TableField("goods_info")
    private String goodsInfo;
    // 是否禁用 0 开启 1关闭
    @TableField("is_close")
    private Byte isClose;
    // 平台审核（
    @TableField("is_check")
    private Byte isCheck;
    // 审核备注 0待审核 1 通过 2 不同意
    @TableField("check_remark")
    private String checkRemark;
    // 添加时间
    @TableField("add_time")
    private Integer addTime;

}
