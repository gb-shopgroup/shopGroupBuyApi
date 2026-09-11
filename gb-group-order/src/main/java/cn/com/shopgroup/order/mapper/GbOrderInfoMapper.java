package cn.com.shopgroup.order.mapper;

import cn.com.shopgroup.order.model.GbOrderGoodsInfo;
import cn.com.shopgroup.order.model.GbOrderInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface GbOrderInfoMapper extends BaseMapper<GbOrderInfo> {

    // (团长)汇总订单数量, 已支付(pay_time>0), 未退款(refund_time=0), 区分已核销(verify_time>0)/未核销的数量
    @Select({
            "SELECT (CASE WHEN `verify_time` > 0 THEN 1 ELSE 0 END) AS is_receipt, COUNT(*) AS num_total",
            "FROM `gb_order_info`",
            "WHERE `leader_id` = #{leaderId}",
            "  AND `add_time` BETWEEN #{startTime} AND #{endTime}",
            "  AND `pay_time` > 0",
            "  AND `refund_time` = 0",
            "GROUP BY is_receipt",
            "ORDER BY is_receipt ASC"
    })
    List<Map<String, Object>> getSummaryOrderList(Long leaderId, int startTime, int endTime);


    // (团长)汇总订单商品数量, 已支付(pay_time>0), 未退款(refund_time=0), 区分已核销(verify_time>0)/未核销的数量
    @Select({
            "SELECT g.`goods_id`, (CASE WHEN o.`verify_time` > 0 THEN 1 ELSE 0 END) AS is_receipt,",
            "       SUM(g.`goods_num` * g.`pack_num`) AS num_total",
            "FROM `gb_order_goods_info` AS g",
            "JOIN `gb_order_info` AS o ON g.`order_no` = o.`order_no`",
            "WHERE o.`leader_id` = #{leaderId}",
            "  AND o.`add_time` BETWEEN #{startTime} AND #{endTime}",
            "  AND o.`pay_time` > 0",
            "  AND o.`refund_time` = 0",
            "GROUP BY g.`goods_id`, is_receipt",
            "ORDER BY g.`goods_id` ASC, is_receipt ASC"
    })
    List<Map<String, Object>> getSummaryOrderGoodsList(Long leaderId, int startTime, int endTime);


    // (团长)汇总订单商品数量, 已支付(pay_time>0), 未退款(refund_time=0), 区分已核销/未核销的数量, 增加提货点分组
    @Select({
            "SELECT o.`point_id`, g.`goods_id`, (CASE WHEN o.`verify_time` > 0 THEN 1 ELSE 0 END) AS is_receipt,",
            "       SUM(g.`goods_num` * g.`pack_num`) AS num_total",
            "FROM `gb_order_goods_info` AS g",
            "JOIN `gb_order_info` AS o ON g.`order_no` = o.`order_no`",
            "WHERE o.`leader_id` = #{leaderId}",
            "  AND o.`add_time` BETWEEN #{startTime} AND #{endTime}",
            "  AND o.`pay_time` > 0",
            "  AND o.`refund_time` = 0",
            "GROUP BY o.`point_id`, g.`goods_id`, is_receipt",
            "ORDER BY o.`point_id` ASC, g.`goods_id` ASC, is_receipt ASC"
    })
    List<Map<String, Object>> getSummaryOrderGoodsListByPoint(Long leaderId, int startTime, int endTime);


    // (团长)根据商品id汇总订单商品"sku"数量, 已支付(pay_time>0), 未退款(refund_time=0), 不区分是否核销
    @Select({
            "SELECT g.`sku_ids`, g.`sku_names`, SUM(g.`goods_num` * g.`pack_num`) AS num_total",
            "FROM `gb_order_goods_info` AS g",
            "JOIN `gb_order_info` AS o ON g.`order_no` = o.`order_no`",
            "WHERE o.`leader_id` = #{leaderId}",
            "  AND o.`add_time` BETWEEN #{startTime} AND #{endTime}",
            "  AND o.`pay_time` > 0",
            "  AND o.`refund_time` = 0",
            "  AND g.`goods_id` = #{goodsId}",
            "GROUP BY g.`sku_ids`, g.`sku_names`",
            "ORDER BY g.`sku_ids` ASC"
    })
    List<Map<String, Object>> getSummaryOrderGoodsSkuList(Long leaderId, Long goodsId, int startTime, int endTime);


    // (团长)根据商品id汇总订单商品"包装"数量, 已支付(pay_time>0), 未退款(refund_time=0), 不区分是否核销
    @Select({
            "SELECT g.`pack_id`, g.`pack_name`, SUM(g.`goods_num` * g.`pack_num`) AS num_total",
            "FROM `gb_order_goods_info` AS g",
            "JOIN `gb_order_info` AS o ON g.`order_no` = o.`order_no`",
            "WHERE o.`leader_id` = #{leaderId}",
            "  AND o.`add_time` BETWEEN #{startTime} AND #{endTime}",
            "  AND o.`pay_time` > 0",
            "  AND o.`refund_time` = 0",
            "  AND g.`goods_id` = #{goodsId}",
            "GROUP BY g.`pack_id`, g.`pack_name`",
            "ORDER BY g.`pack_id` ASC"
    })
    List<Map<String, Object>> getSummaryOrderGoodsPackList(Long leaderId, Long goodsId, int startTime, int endTime);


    // (团长)根据商品id汇总订单商品"sku"数量, 已支付(pay_time>0), 未退款(refund_time=0), 不区分是否核销, 增加提货点分组
    @Select({
            "SELECT o.`point_id`, g.`sku_ids`, g.`sku_names`, SUM(g.`goods_num` * g.`pack_num`) AS num_total",
            "FROM `gb_order_goods_info` AS g",
            "JOIN `gb_order_info` AS o ON g.`order_no` = o.`order_no`",
            "WHERE o.`leader_id` = #{leaderId}",
            "  AND o.`add_time` BETWEEN #{startTime} AND #{endTime}",
            "  AND o.`pay_time` > 0",
            "  AND o.`refund_time` = 0",
            "  AND g.`goods_id` = #{goodsId}",
            "GROUP BY o.`point_id`, g.`sku_ids`, g.`sku_names`",
            "ORDER BY o.`point_id` ASC, g.`sku_ids` ASC"
    })
    List<Map<String, Object>> getSummaryOrderGoodsSkuListByPoint(Long leaderId, Long goodsId, int startTime, int endTime);


    // (团长)根据商品id汇总订单商品"包装"数量, 已支付(pay_time>0), 未退款(refund_time=0), 不区分是否核销, 增加提货点分组
    @Select({
            "SELECT o.`point_id`, g.`pack_id`, g.`pack_name`, SUM(g.`goods_num` * g.`pack_num`) AS num_total",
            "FROM `gb_order_goods_info` AS g",
            "JOIN `gb_order_info` AS o ON g.`order_no` = o.`order_no`",
            "WHERE o.`leader_id` = #{leaderId}",
            "  AND o.`add_time` BETWEEN #{startTime} AND #{endTime}",
            "  AND o.`pay_time` > 0",
            "  AND o.`refund_time` = 0",
            "  AND g.`goods_id` = #{goodsId}",
            "GROUP BY o.`point_id`, g.`pack_id`, g.`pack_name`",
            "ORDER BY o.`point_id` ASC, g.`pack_id` ASC"
    })
    List<Map<String, Object>> getSummaryOrderGoodsPackListByPoint(Long leaderId, Long goodsId, int startTime, int endTime);


    // (店员)指定 提货点id 汇总订单商品数量, 已支付(pay_time>0), 未退款(refund_time=0), 区分已核销/未核销的数量
    @Select({
            "SELECT g.`goods_id`, (CASE WHEN o.`verify_time` > 0 THEN 1 ELSE 0 END) AS is_receipt,",
            "       SUM(g.`goods_num` * g.`pack_num`) AS num_total",
            "FROM `gb_order_goods_info` AS g",
            "JOIN `gb_order_info` AS o ON g.`order_no` = o.`order_no`",
            "WHERE o.`leader_id` = #{leaderId}",
            "  AND (o.`point_id` = #{pointId} OR #{pointId} <= 0)",
            "  AND o.`add_time` BETWEEN #{startTime} AND #{endTime}",
            "  AND o.`pay_time` > 0",
            "  AND o.`refund_time` = 0",
            "GROUP BY g.`goods_id`, is_receipt",
            "ORDER BY g.`goods_id` ASC, is_receipt ASC"
    })
    List<Map<String, Object>> getSummaryPointOrderGoodsList(Long leaderId, Long pointId, int startTime, int endTime);


    // (团长端首页)商品统计汇总: 商品种类总数(COUNT DISTINCT goods_id), 待核销总件数(verify_time=0), 已支付(pay_time>0), 未退款(refund_time=0)
    @Select({
            "SELECT COUNT(DISTINCT g.`goods_id`) AS goods_total,",
            "       COALESCE(SUM(CASE WHEN o.`verify_time` = 0 THEN g.`goods_num` * g.`pack_num` ELSE 0 END), 0) AS unverify_total",
            "FROM `gb_order_goods_info` AS g",
            "JOIN `gb_order_info` AS o ON g.`order_no` = o.`order_no`",
            "WHERE o.`leader_id` = #{leaderId}",
            "  AND (o.`point_id` = #{pointId} OR #{pointId} <= 0)",
            "  AND o.`pay_time` > 0",
            "  AND o.`refund_time` = 0",
            "  AND g.`goods_name` LIKE CONCAT('%', IFNULL(#{keyword}, ''), '%')"
    })
    Map<String, Object> getSummaryGoodsTotal(@Param("leaderId") Long leaderId, @Param("pointId") Long pointId,
                                             @Param("keyword") String keyword);


    // (团长端首页)商品维度统计分页列表: 每商品总件数/未核销件数, 已支付(pay_time>0), 未退款(refund_time=0), goods_name 关键字搜索, 分页
    @Select({
            "SELECT g.`goods_id`, g.`goods_name`, g.`goods_unit`,",
            "       COALESCE(SUM(g.`goods_num` * g.`pack_num`), 0) AS num_total,",
            "       COALESCE(SUM(CASE WHEN o.`verify_time` = 0 THEN g.`goods_num` * g.`pack_num` ELSE 0 END), 0) AS unverify_num",
            "FROM `gb_order_goods_info` AS g",
            "JOIN `gb_order_info` AS o ON g.`order_no` = o.`order_no`",
            "WHERE o.`leader_id` = #{leaderId}",
            "  AND (o.`point_id` = #{pointId} OR #{pointId} <= 0)",
            "  AND o.`pay_time` > 0",
            "  AND o.`refund_time` = 0",
            "  AND g.`goods_name` LIKE CONCAT('%', IFNULL(#{keyword}, ''), '%')",
            "GROUP BY g.`goods_id`, g.`goods_name`, g.`goods_unit`",
            "ORDER BY g.`goods_id` ASC",
            "LIMIT #{offset}, #{limit}"
    })
    List<Map<String, Object>> getSummaryPointGoodsPageList(@Param("leaderId") Long leaderId, @Param("pointId") Long pointId,
                                                           @Param("keyword") String keyword, @Param("offset") int offset,
                                                           @Param("limit") int limit);


    // (店员)指定 提货点id 和 商品id 汇总订单商品"sku"数量, 已支付(pay_time>0), 未退款(refund_time=0), 不区分是否核销
    @Select({
            "SELECT g.`sku_ids`, g.`sku_names`, SUM(g.`goods_num` * g.`pack_num`) AS num_total",
            "FROM `gb_order_goods_info` AS g",
            "JOIN `gb_order_info` AS o ON g.`order_no` = o.`order_no`",
            "WHERE o.`leader_id` = #{leaderId}",
            "  AND (o.`point_id` = #{pointId} OR #{pointId} <= 0)",
            "  AND o.`add_time` BETWEEN #{startTime} AND #{endTime}",
            "  AND o.`pay_time` > 0",
            "  AND o.`refund_time` = 0",
            "  AND g.`goods_id` = #{goodsId}",
            "GROUP BY g.`sku_ids`, g.`sku_names`",
            "ORDER BY g.`sku_ids` ASC"
    })
    List<Map<String, Object>> getSummaryPointOrderGoodsSkuList(Long leaderId, Long pointId, Long goodsId, int startTime, int endTime);


    // (店员)指定 提货点id 和 商品id 汇总订单商品"包装"数量, 已支付(pay_time>0), 未退款(refund_time=0), 不区分是否核销
    @Select({
            "SELECT g.`pack_id`, g.`pack_name`, SUM(g.`goods_num` * g.`pack_num`) AS num_total",
            "FROM `gb_order_goods_info` AS g",
            "JOIN `gb_order_info` AS o ON g.`order_no` = o.`order_no`",
            "WHERE o.`leader_id` = #{leaderId}",
            "  AND (o.`point_id` = #{pointId} OR #{pointId} <= 0)",
            "  AND o.`add_time` BETWEEN #{startTime} AND #{endTime}",
            "  AND o.`pay_time` > 0",
            "  AND o.`refund_time` = 0",
            "  AND g.`goods_id` = #{goodsId}",
            "GROUP BY g.`pack_id`, g.`pack_name`",
            "ORDER BY g.`pack_id` ASC"
    })
    List<Map<String, Object>> getSummaryPointOrderGoodsPackList(Long leaderId, Long pointId, Long goodsId, int startTime, int endTime);


    // (定时任务)查询"已分账且已核销、但核销完成满7天仍未完成(系统/用户未确认收货)"的订单, 用于系统自动完成收货
    // 条件: 分账(is_divide=1) + 已核销(verify_time>0)
    //       + 仍处待收货/部分收货(status in 1,2, 排除已退款/售后/已取消; 含已主动确认收货但仍待完成的2态订单)
    //       + 核销时间早于(当前时间-7天), 以核销时间为准(天然可自愈, 任务中断后仍能补处理, 且不受下单时间窗口滑出影响)
    @Select({
            "SELECT o.`order_no` AS orderNo",
            "FROM `gb_order_info` AS o",
            "JOIN `gb_order_business_info` AS b ON o.`id` = b.`order_id`",
            "WHERE b.`is_divide` = 1",
            "  AND o.`verify_time` > 0",
            "  AND o.`status` IN (1, 2)",
            "  AND o.`verify_time` <= #{verifyEndTime}",
            "ORDER BY o.`id` ASC"
    })
    List<Map<String, String>> getUnReceiptOrderIds(int verifyEndTime);


    // (定时任务)查询超时未支付(pay_time=0)且仍为待支付(status=0)的订单商品, 用于自动取消订单并回滚库存
    @Select({
            "SELECT g.`order_no`, g.`goods_id`, g.`sku_id`, g.`goods_num`, g.`pack_num`",
            "FROM `gb_order_goods_info` AS g",
            "JOIN `gb_order_info` AS o ON g.`order_no` = o.`order_no`",
            "JOIN `gb_goods_info` AS i ON g.`goods_id` = i.`goods_id`",
            "WHERE o.`pay_time` = 0",
            "  AND o.`status` = 0",
            "  AND o.`add_time` < #{time}",
            "  AND i.`is_stock` = 1",
            "ORDER BY g.`id` ASC, g.`goods_id` ASC",
            "LIMIT 0, #{limit}"
    })
    List<GbOrderGoodsInfo> getUnPayOrderGoodsList(int time, int limit);


    // (用户)查询指定时间段内某商品的购买数量, 用于限购判断
    @Select({
            "SELECT g.`goods_id`, SUM(g.`goods_num` * g.`pack_num`) AS num_total",
            "FROM `gb_order_goods_info` AS g",
            "JOIN `gb_order_info` AS o ON g.`order_no` = o.`order_no`",
            "WHERE o.`member_id` = #{memberId}",
            "  AND o.`add_time` BETWEEN #{startTime} AND #{endTime}",
            "  AND g.`goods_id` = #{goodsId}"
    })
    List<Map<String, Object>> getOrderGoodsLimit(Long memberId, Long goodsId, int startTime, int endTime);


    /**
     * 根据ID查询
     */
    GbOrderInfo selectById(Integer orderId);

    /**
     * 根据订单号查询
     */
    GbOrderInfo getOrderInfoByOrderNo(String orderNo);

    /**
     * 根据用户ID查询订单列表
     */
    List<GbOrderInfo> selectByMemberId(Long memberId);

    /**
     * 插入订单
     */
    int insert(GbOrderInfo order);

    /**
     * 更新订单状态
     */
    int updateStatus(@Param("orderNo") String orderNo, @Param("status") Integer status);

    /**
     * 更新支付时间
     */
    int updatePayTime(@Param("orderNo") String orderNo, @Param("payTime") Integer payTime);

    /**
     * 更新收货时间
     */
    int updateReceiptTime(@Param("orderNo") String orderNo, @Param("receiptTime") Integer receiptTime);

    /**
     * 检查订单号是否存在
     */
    int existsByOrderNo(String orderNo);

    List<GbOrderInfo> getAllByLeaderId(@Param("leaderId") Long leaderId);

    List<GbOrderInfo> getPaidOrderInfoBy(@Param("memberId") Long memberId, @Param("shopId") Long shopId);

    // 用户端-查询还有商品未全部收货的订单列表(已支付, 存在商品行收货数量<购买数量的订单)
    List<GbOrderInfo> getNotAllReceiptOrderList(@Param("memberId") Long memberId,
                                                @Param("leaderId") Long leaderId,
                                                @Param("shopId") Long shopId);

    Integer getSumOfGroupActivityOrder(@Param("groupId") Long groupId);

    /**
     * 团长端-聚合查询团员列表
     */
    List<Map<String, Object>> getLeaderMemberSummaryList(@Param("leaderId") Long leaderId,
                                                         @Param("keyword") String keyword,
                                                         @Param("offset") Integer offset,
                                                         @Param("pageSize") Integer pageSize);

    /**
     * 团长端-统计团员数量
     */
    Integer getLeaderMemberSummaryCount(@Param("leaderId") Long leaderId,
                                          @Param("keyword") String keyword);

    /**
     * 团长端-查询某个团员在团长下的有效订单列表
     */
    List<GbOrderInfo> getLeaderMemberOrderList(@Param("leaderId") Long leaderId,
                                               @Param("memberId") Long memberId);

    /**
     * 团长端-对账单: 统计时间范围内的有效订单数(orderTotal)/订单总金额(amountTotal,分)/退款总金额(refundAmountTotal,分)
     */
    Map<String, Object> getLeaderBillTotal(@Param("leaderId") Long leaderId,
                                           @Param("startTime") int startTime,
                                           @Param("endTime") int endTime);

    /**
     * 团长端-对账单: 按订单维度分页明细(订单号/订单金额,分/退款金额,分)
     */
    List<Map<String, Object>> getLeaderBillOrderPageList(@Param("leaderId") Long leaderId,
                                                         @Param("startTime") int startTime,
                                                         @Param("endTime") int endTime,
                                                         @Param("offset") int offset,
                                                         @Param("limit") int limit);

    /**
     * 团长端-对账单: 按商品维度分页明细(商品id/商品名称/订单金额,元/退款金额,元)
     */
    List<Map<String, Object>> getLeaderBillGoodsPageList(@Param("leaderId") Long leaderId,
                                                         @Param("startTime") int startTime,
                                                         @Param("endTime") int endTime,
                                                         @Param("offset") int offset,
                                                         @Param("limit") int limit);

    /**
     * 团长端-对账单: 按商品维度统计商品种类总数(分页总条数)
     */
    Integer getLeaderBillGoodsCount(@Param("leaderId") Long leaderId,
                                    @Param("startTime") int startTime,
                                    @Param("endTime") int endTime);
}
