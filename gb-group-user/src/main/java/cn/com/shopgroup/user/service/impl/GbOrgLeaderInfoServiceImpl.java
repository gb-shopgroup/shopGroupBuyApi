package cn.com.shopgroup.user.service.impl;

import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.user.mapper.GbOrgLeaderInfoMapper;
import cn.com.shopgroup.user.model.GbOrgLeaderInfo;
import cn.com.shopgroup.user.service.GbOrgLeaderInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class GbOrgLeaderInfoServiceImpl implements GbOrgLeaderInfoService {

    @Resource
    private GbOrgLeaderInfoMapper mapper;


    public List<GbOrgLeaderInfo> getAdminLeaderList(String mobile, int page, int pageSize){

        LambdaQueryWrapper<GbOrgLeaderInfo> queryWrapper = Wrappers.lambdaQuery();
        if(mobile != null && mobile.length() > 0) queryWrapper.eq(GbOrgLeaderInfo::getMobile, mobile);
        queryWrapper.orderByDesc(GbOrgLeaderInfo::getLeaderId);
        queryWrapper.last("limit " + (page - 1) * pageSize + "," + pageSize);
        List<GbOrgLeaderInfo> result = mapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }


    public Long getAdminLeaderCount(){

        LambdaQueryWrapper<GbOrgLeaderInfo> queryWrapper = Wrappers.lambdaQuery();
        return mapper.selectCount(queryWrapper);
    }


    public Long addAdminLeaderInfo(String name, String mobile, String nickName, String avatar, String openid, byte commission){

        GbOrgLeaderInfo info = new GbOrgLeaderInfo();

        info.setRegionId(0L);

        info.setRegionName("");
    
        info.setLeaderType((byte)2);

        info.setIsolationId(0);

        info.setLeaderName(name);

        info.setMobile(mobile);

        info.setNickname(nickName);

        info.setAvatar(avatar);

        info.setOpenid(openid);

        info.setStartTime(0);

        info.setEndTime(0);

        info.setLeaderBalance(0D);

        info.setCommission(commission);

        info.setNumLimit(0);

        info.setRemark("");

        info.setIsClose((byte)0);

        info.setAddTime(TimeUtils.getTimeStamp());

        mapper.insert(info);

        return info.getLeaderId();
    }


    public List<GbOrgLeaderInfo> getAdminLeaderSelectList(){

        LambdaQueryWrapper<GbOrgLeaderInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(GbOrgLeaderInfo::getLeaderId, GbOrgLeaderInfo::getLeaderName);
        queryWrapper.orderByDesc(GbOrgLeaderInfo::getLeaderId);
        List<GbOrgLeaderInfo> result = mapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }


    public GbOrgLeaderInfo getLeaderInfo(Long leaderId){

        return mapper.selectById(leaderId);
    }


    public GbOrgLeaderInfo getMiniLeaderInfo(String openid){

        LambdaQueryWrapper<GbOrgLeaderInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(
                GbOrgLeaderInfo::getLeaderId,
                GbOrgLeaderInfo::getLeaderName);
        queryWrapper.eq(GbOrgLeaderInfo::getIsClose, 0);
        queryWrapper.eq(GbOrgLeaderInfo::getOpenid, openid);
        queryWrapper.orderByDesc(GbOrgLeaderInfo::getLeaderId);
        queryWrapper.last("limit 0,1");
        return mapper.selectOne(queryWrapper);
    }


}
