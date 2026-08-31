package cn.com.shopgroup.user.service.impl;

import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.user.mapper.GbOrgMessageInfoMapper;
import cn.com.shopgroup.user.model.GbOrgMessageInfo;
import cn.com.shopgroup.user.service.GbOrgMessageInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class GbOrgMessageInfoServiceImpl implements GbOrgMessageInfoService {

    @Resource
    private GbOrgMessageInfoMapper mapper;


    public List<GbOrgMessageInfo> getMiniLeaderMessageList(Long leaderId, Long staffId, Integer msgType, int page, int pageSize) {

        LambdaQueryWrapper<GbOrgMessageInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrgMessageInfo::getLeaderId, leaderId);
        if (staffId > 0) queryWrapper.eq(GbOrgMessageInfo::getStaffId, staffId);
        if (msgType > 0) queryWrapper.eq(GbOrgMessageInfo::getMsgType, msgType);
        queryWrapper.orderByDesc(GbOrgMessageInfo::getMsgId);
        queryWrapper.last("limit " + (page - 1) * pageSize + "," + pageSize);
        List<GbOrgMessageInfo> result = mapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }


    public Long getMiniLeaderMessageCount(Long leaderId, Long staffId, Integer msgType) {

        LambdaQueryWrapper<GbOrgMessageInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrgMessageInfo::getLeaderId, leaderId);
        if (staffId > 0) queryWrapper.eq(GbOrgMessageInfo::getStaffId, staffId);
        if (msgType > 0) queryWrapper.eq(GbOrgMessageInfo::getMsgType, msgType);
        return mapper.selectCount(queryWrapper);
    }


    public Long getMiniLeaderMessageUnReadCount(Long leaderId, Long staffId, Integer msgType) {

        LambdaQueryWrapper<GbOrgMessageInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrgMessageInfo::getLeaderId, leaderId);
        queryWrapper.eq(GbOrgMessageInfo::getIsRead, 0);
        if (staffId > 0) queryWrapper.eq(GbOrgMessageInfo::getStaffId, staffId);
        if (msgType > 0) queryWrapper.eq(GbOrgMessageInfo::getMsgType, msgType);
        return mapper.selectCount(queryWrapper);
    }


    public Long addMiniLeaderMessageInfo(Long leaderId, Long staffId, Byte type, String msg) {

        GbOrgMessageInfo data = new GbOrgMessageInfo();

        data.setLeaderId(leaderId);

        data.setStaffId(staffId);

        data.setMsgType(type);

        data.setMsgContent(msg);

        data.setIsRead((byte) 0);

        data.setAddTime(TimeUtils.getTimeStamp());

        mapper.insert(data);

        return data.getMsgId();
    }


    public Boolean readMiniLeaderMessageInfo(Long leaderId, Long staffId, Long msgId) {

        LambdaUpdateWrapper<GbOrgMessageInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.set(GbOrgMessageInfo::getIsRead, 1);
        updateWrapper.eq(GbOrgMessageInfo::getIsRead, 0);
        updateWrapper.eq(GbOrgMessageInfo::getLeaderId, leaderId);
        updateWrapper.eq(GbOrgMessageInfo::getStaffId, staffId);
        if (msgId > 0) updateWrapper.eq(GbOrgMessageInfo::getMsgId, msgId);
        int flag = mapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }


}
