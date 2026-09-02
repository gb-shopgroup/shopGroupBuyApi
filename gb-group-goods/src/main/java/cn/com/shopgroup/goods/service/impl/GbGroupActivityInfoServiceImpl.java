package cn.com.shopgroup.goods.service.impl;

import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.goods.mapper.GbGroupActivityGoodsMapper;
import cn.com.shopgroup.goods.mapper.GbGroupActivityInfoMapper;
import cn.com.shopgroup.goods.model.GbGoodsInfo;
import cn.com.shopgroup.goods.model.GbGroupActivityGoods;
import cn.com.shopgroup.goods.model.GbGroupActivityInfo;
import cn.com.shopgroup.goods.service.GbGroupActivityInfoService;
import cn.com.shopgroup.user.service.GbOrgMessageInfoService;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class GbGroupActivityInfoServiceImpl implements GbGroupActivityInfoService {

    @Resource
    private GbGroupActivityInfoMapper mapper;

    @Resource
    private GbGroupActivityGoodsMapper goodsMapper;

    @Resource
    private GbOrgMessageInfoService messageService;


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


    public List<GbGroupActivityInfo> getMiniLeaderGroupList(int flag, Long leaderId, Long catId,String activityName, int status, int page, int pageSize) {
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
        if(cid != 0 ){
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
        data.setIsClose((byte) 1);
        data.setSortOrder(65535);
        data.setStaffId(info.getStaffId());
        data.setStaffName(info.getStaffName());
        data.setIsCheck((byte) 1);
        data.setCheckRemark("");
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


}
