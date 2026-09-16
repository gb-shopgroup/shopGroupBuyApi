package cn.com.shopgroup.service.impl;

import cn.com.shopgroup.goods.mapper.GbGroupActivityInfoMapper;
import cn.com.shopgroup.goods.model.GbGroupActivityInfo;
import cn.com.shopgroup.service.TaskGoodsService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class TaskGoodsServiceImpl implements TaskGoodsService {

    @Resource
    private GbGroupActivityInfoMapper mapper;

    /**
     * 到期团购活动自动下线:
     * 1) 查询在线(is_close=0)且 当前时间>=结束时间(end_time) 的活动, 每次最多 limit 条
     * 2) 逐条抢占式下线: 仅当记录仍为在线(is_close=0)时才置为下线(is_close=1),
     *    条件更新承担防重, 人工已提前下线/并发重复执行都不会误更新
     */
    @Override
    public int closeExpiredGroupActivity(int nowTime, int limit) {

        // 1. 查询到期的在线团购活动(end_time 为空的历史数据不处理)
        LambdaQueryWrapper<GbGroupActivityInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(GbGroupActivityInfo::getGroupId, GbGroupActivityInfo::getGroupName);
        queryWrapper.eq(GbGroupActivityInfo::getIsClose, 0);
        queryWrapper.le(GbGroupActivityInfo::getEndTime, nowTime);
        queryWrapper.orderByAsc(GbGroupActivityInfo::getGroupId);
        queryWrapper.last("limit 0, " + Math.max(limit, 1));
        List<GbGroupActivityInfo> list = mapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return 0;
        }

        // 2. 逐条抢占式下线(is_close: 0 -> 1)
        int closeCount = 0;
        for (GbGroupActivityInfo item : list) {
            LambdaUpdateWrapper<GbGroupActivityInfo> updateWrapper = Wrappers.lambdaUpdate();
            updateWrapper.set(GbGroupActivityInfo::getIsClose, 1);
            updateWrapper.eq(GbGroupActivityInfo::getGroupId, item.getGroupId());
            updateWrapper.eq(GbGroupActivityInfo::getIsClose, 0);
            int rows = mapper.update(null, updateWrapper);
            if (rows > 0) {
                closeCount++;
                log.info("到期团购活动自动下线:groupId={}, groupName={}", item.getGroupId(), item.getGroupName());
            }
        }
        return closeCount;
    }

}
