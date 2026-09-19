package cn.com.shopgroup.goods.service.impl;

import cn.com.shopgroup.common.utils.MoneyUtil;
import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.goods.http.response.leader.LeaderGroupFollowRecordResponse;
import cn.com.shopgroup.goods.http.response.leader.LeaderGroupGenTuanResponse;
import cn.com.shopgroup.goods.http.response.leader.LeaderGroupSummaryResponse;
import cn.com.shopgroup.goods.service.LeaderGroupSummaryService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LeaderGroupSummaryServiceImpl implements LeaderGroupSummaryService {

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
    @Override
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

    /**
     * 查询单个团购活动的跟团统计(团员人数、订单数、订单总支付金额)
     *
     * 统计口径(与用户端跟团人数一致): 已支付(pay_time>0)、未取消(status!=6)的有效订单; 团员人数按 member_id 去重, 下单数为有效订单条数
     * 订单总支付金额(totalAmount): 支付流水总金额 SUM(pay_fee), 单位分转元, 不扣除退款(与字段注释"不管退的, 支付总金额"保持一致)
     */
    @Override
    public LeaderGroupGenTuanResponse getGenTuanByGroupId(Long groupId) {

        LeaderGroupGenTuanResponse result = new LeaderGroupGenTuanResponse();
        result.setMemberNum(0);
        result.setOrderNum(0);
        result.setTotalAmount(0D);
        if (groupId == null || groupId <= 0) {
            return result;
        }

        // 一次查询同时得出: 去重团员人数 + 有效订单数 + 订单总支付金额(分)
        String sql = "SELECT COUNT(DISTINCT member_id) AS member_num, " +
                "       COUNT(*) AS order_num, " +
                "       COALESCE(SUM(pay_fee), 0) AS total_fee_cent " +
                "FROM gb_order_info " +
                "WHERE group_id = ? " +
                "  AND pay_time > 0 " +
                "  AND status <> ? " +
                //"  AND refund_time = 0 " +
                "  AND member_id > 0";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, groupId, ORDER_STATUS_CANCELED);
        if (rows != null && !rows.isEmpty()) {
            Map<String, Object> row = rows.get(0);
            result.setMemberNum((int) toLong(row.get("member_num")));
            result.setOrderNum((int) toLong(row.get("order_num")));
            // 分 -> 元, 保留两位小数
            result.setTotalAmount(MoneyUtil.centToYuan((int) toLong(row.get("total_fee_cent"))));
        }
        return result;
    }

    /**
     * 查询单个团购活动的跟团记录(真实订单数据, 按购买时间倒序)
     *
     * 统计口径(与跟团统计一致): 已支付(pay_time>0)、未取消(status!=6)的有效订单;
     * 1) 查订单表取 手机号/姓名(昵称)/头像/购买时间(支付时间);
     * 2) 按订单号批量查订单商品表, 拼接购买商品描述(商品名/规格)与购买数量(商品数量合计)
     */
    @Override
    public List<LeaderGroupFollowRecordResponse> getFollowRecordListByGroupId(Long groupId, int limit) {

        List<LeaderGroupFollowRecordResponse> result = new ArrayList<>();
        if (groupId == null || groupId <= 0 || limit <= 0) {
            return result;
        }

        // 1. 查询该团购的有效订单(已支付、未取消), 按支付时间倒序
        String orderSql = "SELECT order_no, mobile, nickname, avatar, pay_time, add_time " +
                "FROM gb_order_info " +
                "WHERE group_id = ? " +
                "  AND pay_time > 0 " +
                "  AND status <> ? " +
                "  AND member_id > 0 " +
                "ORDER BY pay_time DESC, id DESC " +
                "LIMIT ?";
        List<Map<String, Object>> orderRows =
                jdbcTemplate.queryForList(orderSql, groupId, ORDER_STATUS_CANCELED, limit);
        if (CollectionUtils.isEmpty(orderRows)) {
            return result;
        }

        // 2. 批量查询订单商品, 按 order_no 关联
        List<String> orderNos = new ArrayList<>(orderRows.size());
        for (Map<String, Object> row : orderRows) {
            Object orderNo = row.get("order_no");
            if (orderNo != null) {
                orderNos.add(orderNo.toString());
            }
        }
        Map<String, List<Map<String, Object>>> goodsMap = new HashMap<>();
        if (!orderNos.isEmpty()) {
            StringBuilder placeholders = new StringBuilder("(");
            List<Object> params = new ArrayList<>(orderNos.size());
            for (int i = 0; i < orderNos.size(); i++) {
                if (i > 0) {
                    placeholders.append(",");
                }
                placeholders.append("?");
                params.add(orderNos.get(i));
            }
            placeholders.append(")");
            String goodsSql = "SELECT order_no, goods_name, sku_names, goods_num " +
                    "FROM gb_order_goods_info " +
                    "WHERE order_no IN " + placeholders;
            List<Map<String, Object>> goodsRows = jdbcTemplate.queryForList(goodsSql, params.toArray());
            if (!CollectionUtils.isEmpty(goodsRows)) {
                for (Map<String, Object> row : goodsRows) {
                    Object orderNo = row.get("order_no");
                    if (orderNo == null) {
                        continue;
                    }
                    goodsMap.computeIfAbsent(orderNo.toString(), k -> new ArrayList<>()).add(row);
                }
            }
        }

        // 3. 组装跟团记录
        for (Map<String, Object> row : orderRows) {
            Object orderNoObj = row.get("order_no");
            String orderNo = orderNoObj == null ? "" : orderNoObj.toString();

            LeaderGroupFollowRecordResponse record = new LeaderGroupFollowRecordResponse();
            record.setMobile(toStr(row.get("mobile")));
            record.setName(toStr(row.get("nickname")));
            record.setAvatar(toStr(row.get("avatar")));
            // 购买时间: 取支付时间, 兜底下单时间
            int payTime = (int) toLong(row.get("pay_time"));
            int addTime = (int) toLong(row.get("add_time"));
            record.setBuyTime(TimeUtils.getFormatTimeStamp(payTime > 0 ? payTime : addTime));

            // 购买商品描述: 多件商品按 商品名/规格 拼接; 购买数量: 商品数量合计
            List<Map<String, Object>> goodsRows = goodsMap.get(orderNo);
            int buyNum = 0;
            StringBuilder goodsDesc = new StringBuilder();
            if (!CollectionUtils.isEmpty(goodsRows)) {
                for (Map<String, Object> goodsRow : goodsRows) {
                    String goodsName = toStr(goodsRow.get("goods_name"));
                    String skuNames = toStr(goodsRow.get("sku_names"));
                    if (goodsDesc.length() > 0) {
                        goodsDesc.append("，");
                    }
                    goodsDesc.append(goodsName);
                    if (!skuNames.isEmpty()) {
                        goodsDesc.append("/").append(skuNames);
                    }
                    buyNum += (int) toLong(goodsRow.get("goods_num"));
                }
            }
            record.setGoodsDesc(goodsDesc.toString());
            record.setBuyNum(buyNum);
            result.add(record);
        }
        return result;
    }

    private static String toStr(Object value) {

        return value == null ? "" : value.toString();
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
