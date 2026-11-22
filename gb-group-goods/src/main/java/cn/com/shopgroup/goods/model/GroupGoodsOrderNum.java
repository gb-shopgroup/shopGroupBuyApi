package cn.com.shopgroup.goods.model;

import lombok.Data;

/**
 * 团购商品订单数统计投影对象(非表实体, 仅用于承载聚合查询结果)
 *
 * <p>虚拟订单口径已由"团购活动级"改为"商品级":
 * 商品已团数 = 该商品在本团购活动中的真实订单数 + 该商品 gb_goods_info.virtual_order;
 * 团购活动级虚拟订单数 = 活动下在线商品 virtual_order 之和。
 */
@Data
public class GroupGoodsOrderNum {

    // 团购活动id(按活动汇总虚拟订单数时使用)
    private Long groupId;
    // 商品id(按商品统计已团数时使用)
    private Long goodsId;
    // 商品虚拟订单数 / 活动下商品虚拟订单数之和(SQL 聚合结果为 BIGINT, 用 Long 承接)
    private Long virtualOrder;
    // 该商品在本团购活动中的真实订单数(COUNT 结果为 BIGINT, 用 Long 承接)
    private Long realOrderNum;
    // 已团数 = realOrderNum + virtualOrder
    private Long groupNum;
}
