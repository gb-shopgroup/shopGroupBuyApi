package cn.com.shopgroup.user.service.impl;

import cn.com.shopgroup.common.cache.RedisConstant;
import cn.com.shopgroup.common.cache.RedisHelper;
import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.user.http.response.PointResponse;
import cn.com.shopgroup.user.mapper.GbOrgPointInfoMapper;
import cn.com.shopgroup.user.model.GbOrgPointInfo;
import cn.com.shopgroup.user.service.GbOrgPointInfoService;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @date: 2026/8/19 04:37
 * @version: 1.0.0
 */
@Service
@Slf4j
public class GbOrgPointInfoServiceImpl implements GbOrgPointInfoService {
    @Resource
    private GbOrgPointInfoMapper orgPointInfoMapper;

    @Resource
    private RedisHelper redisHelper;


    public GbOrgPointInfo getPointInfo(Long pointId) {

        return orgPointInfoMapper.selectById(pointId);
    }


    public List<GbOrgPointInfo> getMiniPointList(Long leaderId) {

        LambdaQueryWrapper<GbOrgPointInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrgPointInfo::getLeaderId, leaderId);
        queryWrapper.eq(GbOrgPointInfo::getIsClose, (byte) 0);
        queryWrapper.orderByAsc(GbOrgPointInfo::getPointId);
        queryWrapper.last("limit 0, 20");
        List<GbOrgPointInfo> result = orgPointInfoMapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }


    public List<GbOrgPointInfo> getMiniLeaderPointList(Long leaderId) {
        LambdaQueryWrapper<GbOrgPointInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrgPointInfo::getLeaderId, leaderId);
        queryWrapper.eq(GbOrgPointInfo::getIsClose, (byte) 0);
        queryWrapper.orderByAsc(GbOrgPointInfo::getPointId);
        queryWrapper.last("limit 0,20");
        List<GbOrgPointInfo> result = orgPointInfoMapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }


    public Long addMiniLeaderPoint(Long leaderId, GbOrgPointInfo info) {
        GbOrgPointInfo data = new GbOrgPointInfo();
        data.setLeaderId(leaderId);
        data.setPointName(info.getPointName());
        data.setPointAddress(info.getPointAddress());
        data.setPointImg(info.getPointImg());
        data.setLongitude(info.getLongitude());
        data.setLatitude(info.getLatitude());
        data.setPointScope(info.getPointScope());
        data.setPointInfo(info.getPointInfo());
        data.setIsClose((byte) 0);
        data.setPerson(info.getPerson());
        data.setPhone(info.getPhone());
        data.setAddTime(TimeUtils.getTimeStamp());
        orgPointInfoMapper.insert(data);
        return data.getPointId();
    }


    public boolean editMiniLeaderPoint(Long leaderId, GbOrgPointInfo info) {

        // pointId 是更新定位条件, 缺失直接判失败(避免拼出无定位条件的 update)
        if (info.getPointId() == null) {
            return false;
        }
        LambdaUpdateWrapper<GbOrgPointInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.set(GbOrgPointInfo::getPointName, info.getPointName());
        updateWrapper.set(GbOrgPointInfo::getPointAddress, info.getPointAddress());
        if (info.getPointImg() != null && info.getPointImg().length() > 0) {
            updateWrapper.set(GbOrgPointInfo::getPointImg, info.getPointImg());
        }
        updateWrapper.set(GbOrgPointInfo::getLongitude, info.getLongitude());
        updateWrapper.set(GbOrgPointInfo::getLatitude, info.getLatitude());
        // 自提范围: 非空且>0才更新(不传保持原值), 原代码 info.getPointScope() 未判空直接 .intValue() 会 NPE
        if (info.getPointScope() != null && info.getPointScope() > 0) {
            updateWrapper.set(GbOrgPointInfo::getPointScope, info.getPointScope());
        }
        updateWrapper.set(GbOrgPointInfo::getPointInfo, info.getPointInfo());
        // 修复: 联系人/电话原来被误放在 eq(WHERE) 条件, 导致 1) 无法修改联系人/电话 2) controller 漏传时条件为 null 更新永不命中;
        // 现改为非空才更新(不传保持原值), 编辑与新增的修改口径保持一致
        if (!StringUtils.isEmpty(info.getPerson())) {
            updateWrapper.set(GbOrgPointInfo::getPerson, info.getPerson());
        }
        if (!StringUtils.isEmpty(info.getPhone())) {
            updateWrapper.set(GbOrgPointInfo::getPhone, info.getPhone());
        }
        // 权限收敛: 仅允许团长修改自己的提货点
        updateWrapper.eq(GbOrgPointInfo::getLeaderId, leaderId);
        updateWrapper.eq(GbOrgPointInfo::getPointId, info.getPointId());
        int flag = orgPointInfoMapper.update(updateWrapper);
        return flag > 0;
    }


    public boolean closeMiniLeaderPoint(Long leaderId, Long pointId, int status) {

        LambdaUpdateWrapper<GbOrgPointInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.set(GbOrgPointInfo::getIsClose, status);
        updateWrapper.eq(GbOrgPointInfo::getPointId, pointId);
        updateWrapper.eq(GbOrgPointInfo::getLeaderId, leaderId);
        int flag = orgPointInfoMapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }


    public boolean updatePointErcode(Long pointId, String ercode) {

        LambdaUpdateWrapper<GbOrgPointInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.set(GbOrgPointInfo::getPointErcode, ercode);
        updateWrapper.eq(GbOrgPointInfo::getPointId, pointId);
        int flag = orgPointInfoMapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }


    public List<PointResponse> getGroupPoint(Long leaderId) {
        // 先读缓存再读数据库
        String key = RedisConstant.RedisPointListKey + leaderId;
        if (redisHelper.hasKey(key) == false) {
            // 查询数据库
            List<GbOrgPointInfo> result = getMiniPointList(leaderId);
            log.info("getMiniPointList leaderId:{}", leaderId, JSON.toJSONString(result));
            if (CollectionUtils.isEmpty(result)) {
                log.info("【getMiniPointList】 leaderId:{}", leaderId + "自提点列表为空");
                return new ArrayList<>();
            } else {
                List<PointResponse> data = PointResponse.getPointResponseList(result);
                // 缓存到Redis
                redisHelper.setCacheObject(key, data, RedisConstant.RedisPointListExpired, TimeUnit.SECONDS);
                return data;
            }
        } else {
            // 读取数据库
            List<PointResponse> data = redisHelper.getCacheObject(key);
            return data;
        }
    }

    @Override
    public List<GbOrgPointInfo> getPointListForLeader(Long leaderId, String name) {
        LambdaQueryWrapper<GbOrgPointInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrgPointInfo::getLeaderId, leaderId);
        if (!StringUtils.isEmpty(name)) {
            queryWrapper.like(GbOrgPointInfo::getPointName, name);
        }
        queryWrapper.orderByAsc(GbOrgPointInfo::getPointId);
        queryWrapper.last("limit 0,20");
        List<GbOrgPointInfo> result = orgPointInfoMapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }


    @Override
    public List<GbOrgPointInfo> getPointListByIds(Collection<Long> pointIds) {
        // 空集合直接返回, 避免生成非法 IN ()
        if (pointIds == null || pointIds.isEmpty()) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<GbOrgPointInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.in(GbOrgPointInfo::getPointId, pointIds);
        List<GbOrgPointInfo> result = orgPointInfoMapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }

    @Override
    public List<GbOrgPointInfo> getPointListByLeaderIds(Collection<Long> leaderIds) {
        // 空集合直接返回, 避免生成非法 IN ()
        if (leaderIds == null || leaderIds.isEmpty()) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<GbOrgPointInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.in(GbOrgPointInfo::getLeaderId, leaderIds);
        queryWrapper.eq(GbOrgPointInfo::getIsClose, (byte) 0);
        List<GbOrgPointInfo> result = orgPointInfoMapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }
}
