package cn.com.shopgroup.goods.service;

import cn.com.shopgroup.goods.http.response.leader.LeaderGroupSummaryResponse;
import java.util.Collection;
import java.util.Map;

/**
 * 团长端 - 团购活动订单汇总服务
 *
 * 说明:
 * - 团购活动数据在 gb-group-goods 模块, 订单数据在 gb-group-order 模块
 * - 两个模块是独立部署服务, 且存在 Maven 循环依赖(gb-group-order -> gb-group-goods),
 *   无法直接调用 GbOrderInfoService / GbOrderInfoMapper
 * - 多个服务共用同一个 MySQL 库(group_purchase), 因此直接通过 JdbcTemplate
 *   聚合查询 gb_order_info 表, 一次 IN 查询所有 groupId, 避免 N+1
 * - 统计口径: status != 6(已取消) 的订单, 与首页团长汇总保持一致
 */
public interface LeaderGroupSummaryService {

    /**
     * 批量查询多个团购活动的订单汇总数据
     *
     * @param groupIds 团购id集合
     * @return key=groupId, value=汇总数据(无订单的团购不在返回结果中, 调用方按需判空)
     */
    Map<Long, LeaderGroupSummaryResponse> getSummaryByGroupIds(Collection<Long> groupIds);

}