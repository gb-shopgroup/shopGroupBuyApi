package cn.com.shopgroup.order.service;

import cn.com.shopgroup.order.model.GbGroupViewLog;
import java.util.List;
import java.util.Map;

/**
 * 团购查看记录(埋点)服务
 */
public interface GbGroupViewLogService {

    /**
     * 记录一次"查看团购"（防抖：同一用户同一团购 60 秒内只记一次）
     *
     * @return true=已入库 false=未记录(参数错误/团购不存在/防抖命中)
     */
    Boolean recordView(Long memberId, Long groupId);

    /**
     * 团员详情-某成员在团长下的查看次数
     */
    Integer getViewCount(Long leaderId, Long memberId);

    /**
     * 团员列表-批量统计查看次数: memberId -> viewCount
     */
    Map<Long, Integer> getViewCountMap(Long leaderId, List<Long> memberIds);

    /**
     * 团员列表-每个成员最近一条查看记录: memberId -> {groupName, viewTime}
     */
    Map<Long, Map<String, Object>> getLastViewMap(Long leaderId, List<Long> memberIds);

    /**
     * 团员详情-某成员最近的查看记录(详情动态用, 最多50条)
     */
    List<GbGroupViewLog> getRecentViewList(Long leaderId, Long memberId);

}