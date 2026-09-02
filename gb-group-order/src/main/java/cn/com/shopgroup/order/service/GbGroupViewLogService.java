package cn.com.shopgroup.order.service;

import cn.com.shopgroup.common.cache.RedisHelper;
import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.goods.model.GbGroupActivityInfo;
import cn.com.shopgroup.goods.service.GbGroupActivityInfoService;
import cn.com.shopgroup.order.mapper.GbGroupViewLogMapper;
import cn.com.shopgroup.order.model.GbGroupViewLog;
import cn.com.shopgroup.user.model.GbMemberInfo;
import cn.com.shopgroup.user.service.GbMemberInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 团购查看记录(埋点)服务
 */
@Service
@Slf4j
public class GbGroupViewLogService {

    // 查看防抖key前缀 + 成员id + ":" + 团购id
    private static final String VIEW_ONCE_KEY = "GroupViewOnce:";
    // 防抖时间窗口(秒)：同一用户同一团购在窗口内只记录一次
    private static final long VIEW_ONCE_EXPIRE = 60L;
    // 团员详情动态里最多取的查看记录条数
    private static final int DETAIL_VIEW_LIMIT = 100;

    @Resource
    private GbGroupViewLogMapper viewLogMapper;

    @Resource
    private GbGroupActivityInfoService groupActivityInfoService;

    @Resource
    private GbMemberInfoService memberInfoService;

    @Resource
    private RedisHelper redisHelper;

    /**
     * 记录一次"查看团购"（防抖：同一用户同一团购 60 秒内只记一次）
     *
     * @return true=已入库 false=未记录(参数错误/团购不存在/防抖命中)
     */
    public Boolean recordView(Long memberId, Long groupId) {
        if (memberId == null || memberId <= 0 || groupId == null || groupId <= 0) {
            return false;
        }
        // 防抖: 60秒窗口内同一用户看同一团购只记一次
        String onceKey = VIEW_ONCE_KEY + memberId + ":" + groupId;
        if (redisHelper.hasKey(onceKey)) {
            return false;
        }
        try {
            GbGroupActivityInfo activity = groupActivityInfoService.getMiniGroupActivityInfo(groupId);
            if (ObjectUtils.isEmpty(activity)) {
                return false;
            }
            GbGroupViewLog viewLog = new GbGroupViewLog();
            viewLog.setMemberId(memberId);
            viewLog.setGroupId(groupId);
            viewLog.setGroupName(activity.getGroupName());
            // 团长id: 优先活动归属团长, 其次用户来源团长
            Long leaderId = activity.getLeaderId();
            // 用户信息冗余(用于团长端脱敏展示)
            GbMemberInfo member = memberInfoService.getMemberInfo(memberId);
            if (!ObjectUtils.isEmpty(member)) {
                if (leaderId == null || leaderId <= 0) {
                    leaderId = member.getLeaderId();
                }
                viewLog.setMobile(member.getMobile());
                viewLog.setNickname(member.getNickname());
                viewLog.setAvatar(member.getAvatar());
            }
            if (leaderId == null || leaderId <= 0) {
                return false;
            }
            viewLog.setLeaderId(leaderId);
            viewLog.setViewTime(TimeUtils.getTimeStamp());

            int rows = viewLogMapper.insert(viewLog);
            if (rows > 0) {
                redisHelper.setCacheObject(onceKey, 1, VIEW_ONCE_EXPIRE, TimeUnit.SECONDS);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("记录查看团购埋点失败 memberId:{} groupId:{}", memberId, groupId, e);
            return false;
        }
    }

    /**
     * 团员详情-某成员在团长下的查看次数
     */
    public Integer getViewCount(Long leaderId, Long memberId) {
        LambdaQueryWrapper<GbGroupViewLog> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(GbGroupViewLog::getLeaderId, leaderId);
        wrapper.eq(GbGroupViewLog::getMemberId, memberId);
        Long count = viewLogMapper.selectCount(wrapper);
        return count == null ? 0 : count.intValue();
    }

    /**
     * 团员列表-批量统计查看次数: memberId -> viewCount
     */
    public Map<Long, Integer> getViewCountMap(Long leaderId, List<Long> memberIds) {
        Map<Long, Integer> result = new HashMap<>();
        if (CollectionUtils.isEmpty(memberIds)) {
            return result;
        }
        List<Map<String, Object>> rows = viewLogMapper.countViewsByMembers(leaderId, memberIds);
        if (CollectionUtils.isEmpty(rows)) {
            return result;
        }
        for (Map<String, Object> row : rows) {
            result.put(toLong(row.get("memberId")), toInt(row.get("viewCount")));
        }
        return result;
    }

    /**
     * 团员列表-每个成员最近一条查看记录: memberId -> {groupName, viewTime}
     */
    public Map<Long, Map<String, Object>> getLastViewMap(Long leaderId, List<Long> memberIds) {
        Map<Long, Map<String, Object>> result = new HashMap<>();
        if (CollectionUtils.isEmpty(memberIds)) {
            return result;
        }
        List<Map<String, Object>> rows = viewLogMapper.getLastViewByMembers(leaderId, memberIds);
        if (CollectionUtils.isEmpty(rows)) {
            return result;
        }
        for (Map<String, Object> row : rows) {
            Long memberId = toLong(row.get("memberId"));
            // 同一成员若返回多条(同一秒多条记录), 取第一条
            if (!result.containsKey(memberId)) {
                result.put(memberId, row);
            }
        }
        return result;
    }

    /**
     * 团员详情-某成员最近的查看记录(详情动态用, 最多50条)
     */
    public List<GbGroupViewLog> getRecentViewList(Long leaderId, Long memberId) {
        LambdaQueryWrapper<GbGroupViewLog> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(GbGroupViewLog::getLeaderId, leaderId);
        wrapper.eq(GbGroupViewLog::getMemberId, memberId);
        wrapper.orderByDesc(GbGroupViewLog::getViewTime);
        wrapper.last("limit " + DETAIL_VIEW_LIMIT);
        List<GbGroupViewLog> list = viewLogMapper.selectList(wrapper);
        return list == null ? new ArrayList<>() : list;
    }

    private Long toLong(Object obj) {
        if (obj == null) {
            return 0L;
        }
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        try {
            return Long.valueOf(obj.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private Integer toInt(Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        try {
            return Integer.valueOf(obj.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
