package cn.com.shopgroup.goods.mapper;

import cn.com.shopgroup.goods.model.GbGoodsInfo;
import cn.com.shopgroup.goods.model.GbGroupActivityGoods;
import cn.com.shopgroup.goods.model.GroupGoodsOrderNum;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface GbGroupActivityGoodsMapper extends BaseMapper<GbGroupActivityGoods> {


    // C端团购商品列表: 商品表在线(g.is_close=0) 且 该团购活动内已上架(a.is_close=0) 才展示
    // 团长在团购活动内下架商品(a.is_close=1)后, C端不再展示; 重新上架(a.is_close=0)后 C端恢复展示
    @Select("SELECT g.* FROM `gb_group_activity_goods` AS a JOIN `gb_goods_info` AS g ON g.`goods_id` = a.`goods_id` WHERE a.`group_id` = #{groupId} AND g.`is_close` = 0 AND a.`is_close` = 0 ")
    List<GbGoodsInfo> getGroupGoodsList(Long groupId);


    // 团购商品列表(团购商品表冗余字段): 商品表在线(g.is_close=0) 且 团购内已上架(a.is_close=0)
    // 用于团购详情/跟团记录/分享海报等展示场景, 保留团购价/市场价/商品名/主图等冗余字段
    @Select("SELECT a.* FROM `gb_group_activity_goods` AS a JOIN `gb_goods_info` AS g ON g.`goods_id` = a.`goods_id` WHERE a.`group_id` = #{groupId} AND g.`is_close` = 0 AND a.`is_close` = 0 ORDER BY a.`goods_id` ASC")
    List<GbGroupActivityGoods> getGroupActivityGoodsListOnline(Long groupId);


    /**
     * 团购活动内的商品行(不过滤任何状态: 商品表上下架 / 团购内上下架都不影响)
     *
     * <p>用于团长端在团购活动内切换商品上下架时读取当前状态; 若按在线条件过滤,
     * 已下架的商品行查不到就无法再切换回上架, 因此这里只按 group_id + goods_id 查团购商品表
     */
    @Select("SELECT a.* FROM `gb_group_activity_goods` AS a WHERE a.`group_id` = #{groupId} AND a.`goods_id` = #{goodsId} LIMIT 1")
    GbGroupActivityGoods getGroupActivityGoods(@Param("groupId") Long groupId, @Param("goodsId") Long goodsId);


    @Delete("DELETE FROM `gb_group_activity_goods` WHERE `group_id` = #{groupId}")
    int deleteGroupGoodsList(Long groupId);


    @Select("SELECT ag.* FROM `gb_group_activity_goods` AS ag JOIN `gb_group_activity_info` AS ai ON ag.`group_id` = ai.`group_id` AND ai.`is_check` = 1 AND ai.`is_close` = 0 AND ai.leader_id = #{leaderId} WHERE ag.`goods_id` = #{goodsId} ORDER BY ai.`group_id` DESC LIMIT 0, 1")
    GbGroupActivityGoods isGroupGoodsOnline(Long leaderId, Long goodsId);

    // 单个团购商品(C端/海报等展示场景): 商品表在线(g.is_close=0) 且 团购内已上架(a.is_close=0)
    @Select("SELECT a.* FROM `gb_group_activity_goods` AS a JOIN `gb_goods_info` AS g ON g.`goods_id` = a.`goods_id` WHERE a.`group_id` = #{groupId} AND a.`goods_id` = #{goodsId} AND g.`is_close` = 0 AND a.`is_close` = 0 limit 1")
    GbGroupActivityGoods getGroupActivityOnlineGoods(@Param("groupId") Long groupId, @Param("goodsId") Long goodsId);

    /**
     * 团购活动内商品上下架(团长端): 修改团购商品表 gb_group_activity_goods 的 is_close(0=上架, 1=下架)
     *
     * <p>只改"该团购活动"下的"该商品"行, 且通过 JOIN 团购活动表校验该活动属于当前团长(ai.leader_id),
     * 防止团长越权修改他人团购活动的商品状态; 活动或商品不存在时影响行数为 0。
     *
     * @param leaderId 团长id(活动归属校验)
     * @param groupId  团购活动id
     * @param goodsId  商品id
     * @param status   目标状态: 0=上架, 1=下架
     * @return 影响行数(1=更新成功, 0=活动/商品不存在或不属于该团长)
     */
    @Update({
            "UPDATE `gb_group_activity_goods` AS ag",
            "JOIN `gb_group_activity_info` AS ai ON ag.`group_id` = ai.`group_id`",
            "SET ag.`is_close` = #{status}",
            "WHERE ag.`group_id` = #{groupId}",
            "  AND ag.`goods_id` = #{goodsId}",
            "  AND ai.`leader_id` = #{leaderId}"
    })
    int closeLeaderActivityGoodsInfo(@Param("leaderId") Long leaderId, @Param("groupId") Long groupId,
                                     @Param("goodsId") Long goodsId, @Param("status") int status);

    /**
     * 批量查询团购活动下在线商品的"虚拟订单数"(商品级)。
     * 仅统计活动内已上架(a.is_close=0)且商品表在线(g.is_close=0)的商品, 与 C 端商品列表口径一致。
     *
     * @return 每行 = 活动id + 商品id + 该商品虚拟订单数
     */
    @Select({
            "<script>",
            "SELECT a.`group_id` AS group_id, a.`goods_id` AS goods_id, IFNULL(g.`virtual_order`, 0) AS virtual_order",
            "FROM `gb_group_activity_goods` AS a",
            "JOIN `gb_goods_info` AS g ON g.`goods_id` = a.`goods_id`",
            "WHERE a.`is_close` = 0 AND g.`is_close` = 0",
            "AND a.`group_id` IN",
            "<foreach collection='groupIds' item='gid' open='(' separator=',' close=')'>#{gid}</foreach>",
            "</script>"
    })
    List<GroupGoodsOrderNum> listGroupGoodsVirtualOrder(@Param("groupIds") List<Long> groupIds);

    /**
     * 批量统计团购活动下各商品的"真实订单数": 按订单号去重(一单多商品行/多SKU行只算1单),
     * 有效订单口径与 GbOrderInfoService.getMiniOrderSalesCount 保持一致: status &gt; 0 且 status &lt; 6
     * (已支付、未取消; 排除待支付0与已取消6)。订单明细表按 order_no 关联订单表取 group_id。
     *
     * @return 每行 = 活动id + 商品id + 该商品在本活动中的真实订单数
     */
    @Select({
            "<script>",
            "SELECT o.`group_id` AS group_id, og.`goods_id` AS goods_id, COUNT(DISTINCT o.`order_no`) AS real_order_num",
            "FROM `gb_order_info` AS o",
            "JOIN `gb_order_goods_info` AS og ON og.`order_no` = o.`order_no`",
            "WHERE o.`status` &gt; 0 AND o.`status` &lt; 6",
            "AND o.`group_id` IN",
            "<foreach collection='groupIds' item='gid' open='(' separator=',' close=')'>#{gid}</foreach>",
            "GROUP BY o.`group_id`, og.`goods_id`",
            "</script>"
    })
    List<GroupGoodsOrderNum> listGroupGoodsRealOrderNum(@Param("groupIds") List<Long> groupIds);
}
