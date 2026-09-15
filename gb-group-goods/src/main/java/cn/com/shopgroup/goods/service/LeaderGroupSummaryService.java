package cn.com.shopgroup.goods.service;

import cn.com.shopgroup.common.utils.MoneyUtil;
import cn.com.shopgroup.goods.http.response.leader.LeaderGroupSummaryResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
@Service
public class LeaderGroupSummaryService {

    /**
     * 已取消订单状态码, 与 OrderStatusEnum.CANCELED.getCode() 保持一致
     */
    private static final int ORDER_STATUS_CANCELED = 6;

    @Resource
    private JdbcTemplate jdbcTemplate;

    /**
     * 批量查询多个团购活动的订单汇总数据
     *
     * @param groupIds 团购id集合
     * @return key=groupId, value=汇总数据(无订单的团购不在返回结果中, 调用方按需判空)
     */
    public Map<Long, LeaderGroupSummaryResponse> getSummaryByGroupIds(Collection<Long> groupIds) {

        if (groupIds == null || groupIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // 1) 构建 IN 占位符: (?, ?, ...)
        StringBuilder placeholders = new StringBuilder("(");
        List<Object> params = new ArrayList<>(groupIds.size());
        boolean first = true;
        for (Long gid : groupIds) {
            if (gid == null) {
                continue;
            }
            if (!first) {
                placeholders.append(",");
            }
            placeholders.append("?");
            params.add(gid);
            first = false;
        }
        if (first) {
            // 全部为 null
            return Collections.emptyMap();
        }
        placeholders.append(")");

        // 2) 聚合查询: 按 group_id 分组, 排除已取消订单
        // pay_fee / refund_fee 单位为分(cent), 返回时通过 MoneyUtil 转为元
        String sql = "SELECT group_id, " +
                "       COALESCE(SUM(pay_fee), 0) AS total_fee_cent, " +
                "       COALESCE(SUM(refund_fee), 0) AS refund_fee_cent, " +
                "       COUNT(*) AS order_num " +
                "FROM gb_order_info " +
                "WHERE group_id IN " + placeholders + " " +
                "  AND status <> ? " +
                "GROUP BY group_id";

        params.add(ORDER_STATUS_CANCELED);

        // 3) 执行查询, 转为 Map
        Map<Long, LeaderGroupSummaryResponse> result = new HashMap<>();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, params.toArray());
        for (Map<String, Object> row : rows) {

            Object gidObj = row.get("group_id");
            if (gidObj == null) {
                continue;
            }
            Long groupId = ((Number) gidObj).longValue();

            long totalCent = toLong(row.get("total_fee_cent"));
            long refundCent = toLong(row.get("refund_fee_cent"));
            int orderNum = (int) toLong(row.get("order_num"));

            LeaderGroupSummaryResponse summary = new LeaderGroupSummaryResponse();
            // 分 -> 元, 保留两位小数
            summary.setTotalFee(MoneyUtil.centToYuan((int) totalCent));
            summary.setRefundFee(MoneyUtil.centToYuan((int) refundCent));
            summary.setOrderNum(orderNum);

            result.put(groupId, summary);
        }
        return result;
    }

    private static long toLong(Object value) {

        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }
}