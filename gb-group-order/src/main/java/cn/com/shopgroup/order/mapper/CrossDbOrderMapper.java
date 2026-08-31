/*
package cn.com.shopgroup.order.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.Map;

*/
/**
 * todo:先不考虑
 * 订单域跨库查询 Mapper。
 * 分库后用户域数据在 group_buy_user、商品域数据在 group_buy_goods，
 * 此处通过跨库 SQL 读取（要求三个库部署在同一 MySQL 实例）。
 *//*

@Mapper
@Repository
public interface CrossDbOrderMapper {

    // 会员信息（group_buy_user）
    @Select("SELECT `member_id` AS memberId, `mobile`, `nickname`, `avatar`, `openid` FROM `group_buy_user`.`gb_member_info` WHERE `member_id` = #{memberId}")
    Map<String, Object> selectMemberInfo(Long memberId);

    // 团购活动信息（group_buy_goods）
    @Select("SELECT `group_id` AS groupId, `leader_id` AS leaderId, `isolation_id` AS isolationId, `group_name` AS groupName, `pickup_style` AS pickupStyle, `is_close` AS isClose FROM `group_buy_goods`.`gb_group_activity_info` WHERE `group_id` = #{groupId}")
    Map<String, Object> selectGroupInfo(Long groupId);

    // 提货点信息（group_buy_user）
    @Select("SELECT `point_id` AS pointId, `point_name` AS pointName, `point_address` AS pointAddress FROM `group_buy_user`.`gb_org_point_info` WHERE `point_id` = #{pointId}")
    Map<String, Object> selectPointInfo(Long pointId);

    // 商品信息（group_buy_goods）
    @Select("SELECT `goods_id` AS goodsId, `goods_name` AS goodsName, `sales_price` AS salesPrice, `is_stock` AS isStock, `is_limit` AS isLimit, `limit_num` AS limitNum, `goods_unit` AS goodsUnit, `goods_img` AS goodsImg, `goods_type` AS goodsType, `goods_num` AS goodsNum FROM `group_buy_goods`.`gb_goods_info` WHERE `goods_id` = #{goodsId}")
    Map<String, Object> selectGoodsInfo(Long goodsId);

    // 商品包装信息（group_buy_goods）
    @Select("SELECT `pack_id` AS packId, `sales_price` AS salesPrice, `pack_num` AS packNum FROM `group_buy_goods`.`gb_goods_package_info` WHERE `pack_id` = #{packId}")
    Map<String, Object> selectGoodsPackageInfo(Long packId);

    // 商品SKU信息（group_buy_goods）
    @Select("SELECT `sku_id` AS skuId, `sales_price` AS salesPrice, `pack_num` AS packNum FROM `group_buy_goods`.`gb_goods_sku_info` WHERE `sku_id` = #{skuId}")
    Map<String, Object> selectGoodsSkuInfo(Long skuId);

    // 团长门店信息（group_buy_user）
    @Select("SELECT `leader_id` AS leaderId, `shop_name` AS shopName FROM `group_buy_user`.`gb_org_shop_info` WHERE `leader_id` = #{leaderId}")
    Map<String, Object> selectShopInfo(Long leaderId);

    // 商品库存（group_buy_goods）
    @Select("SELECT `goods_num` AS goodsNum FROM `group_buy_goods`.`gb_goods_info` WHERE `goods_id` = #{goodsId}")
    Integer selectGoodsStock(Long goodsId);

    // 扣减商品库存（group_buy_goods）
    @Update("UPDATE `group_buy_goods`.`gb_goods_info` SET `goods_num` = `goods_num` - #{num} WHERE `goods_id` = #{goodsId} AND `goods_num` > 0 AND `is_stock` = 1")
    int updateReduceGoodsStock(Long goodsId, Integer num);

}
*/
