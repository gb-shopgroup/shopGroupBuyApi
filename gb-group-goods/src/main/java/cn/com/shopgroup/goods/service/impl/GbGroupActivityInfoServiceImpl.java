package cn.com.shopgroup.goods.service.impl;

import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.goods.mapper.GbGroupActivityGoodsMapper;
import cn.com.shopgroup.goods.mapper.GbGroupActivityInfoMapper;
import cn.com.shopgroup.goods.model.GbGoodsInfo;
import cn.com.shopgroup.goods.model.GbGroupActivityGoods;
import cn.com.shopgroup.goods.model.GbGroupActivityInfo;
import cn.com.shopgroup.goods.service.GbGroupActivityInfoService;
import cn.com.shopgroup.user.model.GbOrgPointInfo;
import cn.com.shopgroup.user.service.GbOrgMessageInfoService;
import cn.com.shopgroup.user.service.GbOrgPointInfoService;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class GbGroupActivityInfoServiceImpl implements GbGroupActivityInfoService {

    @Resource
    private GbGroupActivityInfoMapper mapper;

    @Resource
    private GbGroupActivityGoodsMapper goodsMapper;

    @Resource
    private GbOrgMessageInfoService messageService;

    @Resource
    private GbOrgPointInfoService pointService;


    public List<GbGroupActivityInfo> getAdminGroupList(int page, int pageSize) {

        LambdaQueryWrapper<GbGroupActivityInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.orderByDesc(GbGroupActivityInfo::getGroupId);
        queryWrapper.last("limit " + (page - 1) * pageSize + "," + pageSize);
        List<GbGroupActivityInfo> result = mapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }


    public Long getAdminGroupCount() {

        LambdaQueryWrapper<GbGroupActivityInfo> queryWrapper = Wrappers.lambdaQuery();
        return mapper.selectCount(queryWrapper);
    }


    public GbGroupActivityInfo getGroupInfo(Long groupId) {

        return mapper.selectById(groupId);
    }


    public List<GbGoodsInfo> getGroupGoodsList(Long groupId) {

        return goodsMapper.getGroupGoodsList(groupId);
    }


    public List<GbGroupActivityGoods> getGroupActivityGoodsList(Long groupId) {

        LambdaQueryWrapper<GbGroupActivityGoods> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbGroupActivityGoods::getGroupId, groupId);
        queryWrapper.orderByAsc(GbGroupActivityGoods::getGoodsId);
        List<GbGroupActivityGoods> results = goodsMapper.selectList(queryWrapper);
        return results == null ? new ArrayList<>() : results;
    }


    public List<GbGroupActivityInfo> getMiniGroupActivityList(Long leaderId, Long catId, int page, int pageSize) {

        LambdaQueryWrapper<GbGroupActivityInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(
                GbGroupActivityInfo::getGroupId,
                GbGroupActivityInfo::getLeaderId,
                GbGroupActivityInfo::getGroupName,
                GbGroupActivityInfo::getGroupImg,
                GbGroupActivityInfo::getGroupImg2,
                GbGroupActivityInfo::getGroupImg3,
                GbGroupActivityInfo::getGroupPrice,
                GbGroupActivityInfo::getGroupPrice2,
                GbGroupActivityInfo::getMarketPrice,
                //GbGroupActivityInfo::getGoodsId,
                GbGroupActivityInfo::getOrderTotal,
                GbGroupActivityInfo::getVirtualOrder);
        if (leaderId > 0) queryWrapper.eq(GbGroupActivityInfo::getLeaderId, leaderId);
        if (catId > 0) queryWrapper.eq(GbGroupActivityInfo::getCatId, catId);
        queryWrapper.eq(GbGroupActivityInfo::getIsClose, 0); // 未关闭
        queryWrapper.eq(GbGroupActivityInfo::getIsCheck, 1); // 已审核
        queryWrapper.orderByDesc(GbGroupActivityInfo::getGroupId);
        queryWrapper.last("limit " + (page - 1) * pageSize + "," + pageSize);
        List<GbGroupActivityInfo> result = mapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }


    public Long getMiniGroupActivityCount(Long leaderId, Long catId) {

        LambdaQueryWrapper<GbGroupActivityInfo> queryWrapper = Wrappers.lambdaQuery();
        if (leaderId > 0) queryWrapper.eq(GbGroupActivityInfo::getLeaderId, leaderId);
        if (catId > 0) queryWrapper.eq(GbGroupActivityInfo::getCatId, catId);
        queryWrapper.eq(GbGroupActivityInfo::getIsClose, 0);
        queryWrapper.eq(GbGroupActivityInfo::getIsCheck, 1);
        return mapper.selectCount(queryWrapper);
    }


    public GbGroupActivityInfo getMiniGroupActivityInfo(Long groupId) {

        LambdaQueryWrapper<GbGroupActivityInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(
                GbGroupActivityInfo::getGroupId,
                GbGroupActivityInfo::getLeaderId,
                GbGroupActivityInfo::getGroupName,
                GbGroupActivityInfo::getGroupImg,
                GbGroupActivityInfo::getGroupImg2,
                GbGroupActivityInfo::getGroupImg3,
                GbGroupActivityInfo::getGroupPrice,
                GbGroupActivityInfo::getGroupPrice2,
                GbGroupActivityInfo::getMarketPrice,
                GbGroupActivityInfo::getGroupInfo,
                //GbGroupActivityInfo::getGoodsId,
                //GbGroupActivityInfo::getGoodsType,
                GbGroupActivityInfo::getPickupStyle,
                GbGroupActivityInfo::getOrderTotal,
                GbGroupActivityInfo::getVirtualOrder,
                GbGroupActivityInfo::getIsClose);
        //queryWrapper.eq(GbGroupActivityInfo::getIsClose, 0);
        queryWrapper.eq(GbGroupActivityInfo::getIsCheck, 1);
        queryWrapper.eq(GbGroupActivityInfo::getGroupId, groupId);
        queryWrapper.last("limit 0,1");
        return mapper.selectOne(queryWrapper);
    }


    public Boolean addGroupOrderNumber(Long groupId) {

        LambdaUpdateWrapper<GbGroupActivityInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.setSql("order_total = order_total + {0}", 1);
        updateWrapper.eq(GbGroupActivityInfo::getGroupId, groupId);
        int flag = mapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }


    public List<GbGroupActivityInfo> getMiniLeaderGroupList(int flag, Long leaderId, Long catId, String activityName, int status, int page, int pageSize) {
        //(1、团长团查询 2 用户端查询)
        LambdaQueryWrapper<GbGroupActivityInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbGroupActivityInfo::getLeaderId, leaderId);
        Integer nowTime = TimeUtils.getTimeStamp();
        if (flag == 2) {
            queryWrapper.le(GbGroupActivityInfo::getStartTime, nowTime).ge(GbGroupActivityInfo::getEndTime, nowTime);
            queryWrapper.eq(GbGroupActivityInfo::getIsClose, (byte) 0);
        } else {
            //status 状态：0 全部  1 活动中 2 未开始 3 已结束 \\ 开团时间 start_time / end_time
            if (status == 1) {
                queryWrapper.le(GbGroupActivityInfo::getStartTime, nowTime).ge(GbGroupActivityInfo::getEndTime, nowTime);
            }
            if (status == 2) {
                queryWrapper.ge(GbGroupActivityInfo::getStartTime, nowTime);
            }
            if (status == 3) {
                queryWrapper.le(GbGroupActivityInfo::getEndTime, nowTime);
            }
        }
        if (StringUtil.isNotEmpty(activityName)) {
            // 团购名称模糊搜索
            queryWrapper.like(GbGroupActivityInfo::getGroupName, activityName);
        }
        int cid = Optional.ofNullable(catId).orElse(0L).intValue();
        if (cid != 0) {
            queryWrapper.eq(GbGroupActivityInfo::getCatId, catId);
        }
        queryWrapper.orderByDesc(GbGroupActivityInfo::getGroupId);
        queryWrapper.last("limit " + (page - 1) * pageSize + "," + pageSize);
        List<GbGroupActivityInfo> result = mapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }


    public Long getMiniLeaderGroupCount(Long leaderId, Long catId, String activityName, int status) {
        // 筛选条件与 getMiniLeaderGroupList 保持一致, 保证分页总页数正确
        LambdaQueryWrapper<GbGroupActivityInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbGroupActivityInfo::getLeaderId, leaderId);
        Integer nowTime = TimeUtils.getTimeStamp();
        if (status == 1) {
            queryWrapper.eq(GbGroupActivityInfo::getIsClose, (byte) 0);
            queryWrapper.le(GbGroupActivityInfo::getStartTime, nowTime).ge(GbGroupActivityInfo::getEndTime, nowTime);
        }
        if (status == 2) {
            queryWrapper.eq(GbGroupActivityInfo::getIsClose, (byte) 0);
            queryWrapper.ge(GbGroupActivityInfo::getStartTime, nowTime);
        }
        if (status == 3) {
            queryWrapper.le(GbGroupActivityInfo::getEndTime, nowTime);
        }
        if (StringUtil.isNotEmpty(activityName)) {
            queryWrapper.like(GbGroupActivityInfo::getGroupName, activityName);
        }
        int cid = Optional.ofNullable(catId).orElse(0L).intValue();
        if (cid != 0) {
            queryWrapper.eq(GbGroupActivityInfo::getCatId, catId);
        }
        return mapper.selectCount(queryWrapper);
    }


    public Long addMiniLeaderGroupInfo(Long leaderId, GbGroupActivityInfo info) {

        GbGroupActivityInfo data = new GbGroupActivityInfo();
        data.setCatId(info.getCatId());
        data.setLeaderId(leaderId);
        data.setIsolationId(0);
        data.setPickupStyle(info.getPickupStyle());
        data.setGroupName(info.getGroupName());
        data.setGroupImg(info.getGroupImg());
        data.setGroupImg2(info.getGroupImg2());
        data.setGroupImg3(info.getGroupImg3());
        data.setGroupPrice(info.getGroupPrice());
        data.setGroupPrice2(info.getGroupPrice2());
        data.setMarketPrice(info.getMarketPrice());
        // 开团/结束时间以提交为准, 未传时默认 0
        data.setStartTime(Optional.ofNullable(info.getStartTime()).orElse(0));
        data.setEndTime(Optional.ofNullable(info.getEndTime()).orElse(0));
        data.setGroupInfo(info.getGroupInfo());
        data.setOrderTotal(0);
        data.setVirtualOrder(info.getVirtualOrder());
        data.setIsClose(info.getIsClose());
        data.setSortOrder(65535);
        data.setStaffId(info.getStaffId());
        data.setStaffName(info.getStaffName());
        data.setIsCheck((byte) 1);
        data.setCheckRemark("");
        // 团购标签
        data.setTagId(info.getTagId() == null ? 0L : info.getTagId());
        data.setTagName(info.getTagName() == null ? "" : info.getTagName());
        // 自提点id,0未选择
        data.setPointId(info.getPointId() == null ? 0L : info.getPointId());
        data.setAddTime(TimeUtils.getTimeStamp());
        mapper.insert(data);
        Long groupId = data.getGroupId();
        if (info.getLists().size() > 0) {
            for (GbGroupActivityGoods item : info.getLists()) {
                item.setGroupId(groupId);
            }
            goodsMapper.insert(info.getLists());
        }
        byte type = 2;
        String oper = "添加了";
        String content = info.getStaffName() + " " + oper + " " + info.getGroupName() + " 的团购活动。";
        if (data.getGroupId() > 0) messageService.addMiniLeaderMessageInfo(leaderId, info.getStaffId(), type, content);
        return data.getGroupId();
    }


    public Boolean editMiniLeaderGroupInfo(Long leaderId, GbGroupActivityInfo info) {
        LambdaUpdateWrapper<GbGroupActivityInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.set(GbGroupActivityInfo::getCatId, info.getCatId());
        updateWrapper.set(GbGroupActivityInfo::getGroupName, info.getGroupName());
        updateWrapper.set(GbGroupActivityInfo::getGroupImg, info.getGroupImg());
        updateWrapper.set(GbGroupActivityInfo::getGroupImg2, info.getGroupImg2());
        updateWrapper.set(GbGroupActivityInfo::getGroupImg3, info.getGroupImg3());
        updateWrapper.set(GbGroupActivityInfo::getGroupPrice, info.getGroupPrice());
        updateWrapper.set(GbGroupActivityInfo::getGroupPrice2, info.getGroupPrice2());
        updateWrapper.set(GbGroupActivityInfo::getMarketPrice, info.getMarketPrice());
        updateWrapper.set(GbGroupActivityInfo::getGroupInfo, info.getGroupInfo());
        updateWrapper.set(GbGroupActivityInfo::getVirtualOrder, info.getVirtualOrder());
        // 提货方式/开团结束时间同步更新
        updateWrapper.set(GbGroupActivityInfo::getPickupStyle, info.getPickupStyle());
        updateWrapper.set(GbGroupActivityInfo::getStartTime, info.getStartTime());
        updateWrapper.set(GbGroupActivityInfo::getEndTime, info.getEndTime());
        updateWrapper.set(GbGroupActivityInfo::getStaffName, info.getStaffName());
        // 团购标签
        updateWrapper.set(GbGroupActivityInfo::getTagId, info.getTagId() == null ? 0L : info.getTagId());
        updateWrapper.set(GbGroupActivityInfo::getTagName, info.getTagName() == null ? "" : info.getTagName());
        // 自提点id,0未选择
        updateWrapper.set(GbGroupActivityInfo::getPointId, info.getPointId() == null ? 0L : info.getPointId());
        updateWrapper.eq(GbGroupActivityInfo::getGroupId, info.getGroupId());
        updateWrapper.eq(GbGroupActivityInfo::getLeaderId, leaderId);
        int flag = mapper.update(updateWrapper);
        Long groupId = info.getGroupId();
        goodsMapper.deleteGroupGoodsList(groupId);
        goodsMapper.insert(info.getLists());
        Byte type = 2;
        String oper = "修改了";
        String content = info.getStaffName() + " " + oper + " " + info.getGroupName() + " 的团购活动。";
        if (flag > 0) messageService.addMiniLeaderMessageInfo(leaderId, info.getStaffId(), type, content);
        return flag > 0 ? true : false;
    }


    public Boolean closeMiniLeaderGroupInfo(Long leaderId, Long groupId, Integer status, Long staffId, String staffName, String groupName) {
        LambdaUpdateWrapper<GbGroupActivityInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.set(GbGroupActivityInfo::getIsClose, status);
        updateWrapper.eq(GbGroupActivityInfo::getGroupId, groupId);
        updateWrapper.eq(GbGroupActivityInfo::getLeaderId, leaderId);
        int flag = mapper.update(updateWrapper);
        Byte type = 2;
        String oper = "修改了";
        if (status == 1) {
            oper = "关闭了";
        } else {
            oper = "打开了";
        }
        String content = staffName + " " + oper + " " + groupName + " 的团购活动。";
        if (flag > 0) {
            messageService.addMiniLeaderMessageInfo(leaderId, staffId, type, content);
        }

        return flag > 0 ? true : false;
    }

    public Boolean isGoodsGrouping(Long leaderId, Long goodsId) {

        GbGroupActivityGoods result = goodsMapper.isGroupGoodsOnline(leaderId, goodsId);
        return result != null && result.getGroupId() > 0 ? true : false;
    }

    @Override
    public List<GbGroupActivityInfo> getMemberGroupActivityList(Long leaderId, Double longitude, Double latitude, int page, int pageSize) {

        // 请求参数归一
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 10;

        // 1. 已绑定团长(leaderId > 0): 直接查询该团长的在线团购活动, SQL 分页
        //    置顶活动优先(sort_order 升序), 置顶级别相同时新活动在前(按活动id倒序)
        if (leaderId != null && leaderId > 0) {
            LambdaQueryWrapper<GbGroupActivityInfo> queryWrapper = onlineWrapper();
            queryWrapper.eq(GbGroupActivityInfo::getLeaderId, leaderId);
            queryWrapper.orderByAsc(GbGroupActivityInfo::getSortOrder);
            queryWrapper.orderByDesc(GbGroupActivityInfo::getGroupId);
            List<GbGroupActivityInfo> result = mapper.selectList(queryWrapper.last("limit " + (page - 1) * pageSize + "," + pageSize));
            return result == null ? new ArrayList<>() : result;
        }

        // 2. 未绑定团长(新用户, leaderId=0): 按定位经纬度过滤出离自提点 20km 内的在线活动
        //    距离计算需要经纬度, 缺失时无法定位, 直接返回空列表
        if (longitude == null || latitude == null) {
            return new ArrayList<>();
        }
        // 2.1 第一阶段: 窄表扫描仅取 groupId/pointId 两列, 距离过滤与分页定位在此进行,
        //     避免把全量在线活动的大字段(图片/详情等)加载进内存; 未选自提点的活动无法计算距离, SQL 端直接排除
        LambdaQueryWrapper<GbGroupActivityInfo> lightWrapper = onlineWrapper();
        lightWrapper.select(GbGroupActivityInfo::getGroupId, GbGroupActivityInfo::getPointId);
        lightWrapper.isNotNull(GbGroupActivityInfo::getPointId).gt(GbGroupActivityInfo::getPointId, 0);
        lightWrapper.orderByDesc(GbGroupActivityInfo::getGroupId);
        List<GbGroupActivityInfo> lightList = mapper.selectList(lightWrapper);
        if (lightList == null || lightList.isEmpty()) {
            return new ArrayList<>();
        }
        // 收集绑定的自提点id, 批量查询自提点坐标
        Set<Long> pointIdSet = new HashSet<>();
        for (GbGroupActivityInfo item : lightList) {
            pointIdSet.add(item.getPointId());
        }
        List<GbOrgPointInfo> pointList = pointService.getPointListByIds(pointIdSet);
        Map<Long, GbOrgPointInfo> pointMap = new HashMap<>();
        if (pointList != null) {
            for (GbOrgPointInfo point : pointList) {
                pointMap.put(point.getPointId(), point);
            }
        }
        // 过滤出距离 20km 内的活动id(lightList 按活动id倒序, 遍历顺序即最终展示顺序)
        List<Long> nearIds = new ArrayList<>();
        for (GbGroupActivityInfo item : lightList) {
            GbOrgPointInfo point = pointMap.get(item.getPointId());
            if (point == null || point.getLongitude() == null || point.getLatitude() == null) {
                continue;
            }
            //自提点设置的自提范围,单位：公里
            double allowScope = point.getPointScope();
            double distance = distanceKm(longitude, latitude, point.getLongitude(), point.getLatitude());
            if (distance < allowScope) {
                nearIds.add(item.getGroupId());
            }
        }
        if (nearIds.isEmpty()) {
            return new ArrayList<>();
        }
        // 2.2 距离过滤需全量数据, 分页只能在过滤后进行; 内存定位当页id, 再按 id 集合查完整数据
        int fromIndex = Math.min((page - 1) * pageSize, nearIds.size());
        int toIndex = Math.min(fromIndex + pageSize, nearIds.size());
        List<Long> pageIds = nearIds.subList(fromIndex, toIndex);
        LambdaQueryWrapper<GbGroupActivityInfo> fullWrapper = Wrappers.lambdaQuery();
        fullWrapper.in(GbGroupActivityInfo::getGroupId, pageIds);
        fullWrapper.orderByDesc(GbGroupActivityInfo::getGroupId);
        List<GbGroupActivityInfo> result = mapper.selectList(fullWrapper);
        return result == null ? new ArrayList<>() : result;
    }

    // 在线团购活动条件: 未下线且当前时间处于开团时间窗内(已开团未结束); 排序由调用方按业务指定
    private LambdaQueryWrapper<GbGroupActivityInfo> onlineWrapper() {

        LambdaQueryWrapper<GbGroupActivityInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbGroupActivityInfo::getIsClose, (byte) 0);
        Integer nowTime = TimeUtils.getTimeStamp();
        queryWrapper.le(GbGroupActivityInfo::getStartTime, nowTime);
        queryWrapper.ge(GbGroupActivityInfo::getEndTime, nowTime);
        return queryWrapper;
    }

    // 球面距离计算(haversine), 单位: 公里
    private double distanceKm(double lon1, double lat1, double lon2, double lat2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(radLat1) * Math.cos(radLat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }


}
