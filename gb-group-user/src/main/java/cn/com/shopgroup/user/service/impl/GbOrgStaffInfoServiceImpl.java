package cn.com.shopgroup.user.service.impl;

import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.user.mapper.GbOrgPointStaffMapper;
import cn.com.shopgroup.user.mapper.GbOrgStaffInfoMapper;
import cn.com.shopgroup.user.model.GbOrgPointInfo;
import cn.com.shopgroup.user.model.GbOrgPointStaff;
import cn.com.shopgroup.user.model.GbOrgStaffInfo;
import cn.com.shopgroup.user.service.GbOrgStaffInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class GbOrgStaffInfoServiceImpl implements GbOrgStaffInfoService {

    @Resource
    private GbOrgStaffInfoMapper mapper;

    @Resource
    private GbOrgPointStaffMapper pointStaffMapper;


    public Long addAdminLeaderStaffInfo(Long leaderId, String name, String mobile, String nickName, String avatar, String openid) {

        GbOrgStaffInfo info = new GbOrgStaffInfo();

        info.setLeaderId(leaderId);

        info.setStaffName(name);

        info.setMobile(mobile);

        info.setNickname(nickName);

        info.setAvatar(avatar);

        info.setOpenid(openid);

        info.setRemark("");

        info.setAuthList("");

        info.setIsClose((byte) 0);

        info.setAddTime(TimeUtils.getTimeStamp());

        mapper.insert(info);

        return info.getStaffId();
    }


    public GbOrgStaffInfo getAdminLeaderStaffInfoByMobile(String mobile) {

        LambdaQueryWrapper<GbOrgStaffInfo> queryWrapper = Wrappers.lambdaQuery();
        //queryWrapper.select(GbOrgStaffInfo::getStaffId);
        queryWrapper.eq(GbOrgStaffInfo::getMobile, mobile);
        queryWrapper.orderByDesc(GbOrgStaffInfo::getStaffId);
        queryWrapper.last("limit 0,1");
        return mapper.selectOne(queryWrapper);
    }


    public GbOrgStaffInfo getStaffInfo(Long id) {

        return mapper.selectById(id);
    }


    public GbOrgStaffInfo getMiniStaffInfo(String openid) {

        LambdaQueryWrapper<GbOrgStaffInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(
                GbOrgStaffInfo::getStaffId,
                GbOrgStaffInfo::getLeaderId,
                GbOrgStaffInfo::getStaffName,
                GbOrgStaffInfo::getAuthList);
        queryWrapper.eq(GbOrgStaffInfo::getIsClose, 0);
        queryWrapper.eq(GbOrgStaffInfo::getOpenid, openid);
        queryWrapper.orderByDesc(GbOrgStaffInfo::getStaffId);
        queryWrapper.last("limit 0,1");
        return mapper.selectOne(queryWrapper);
    }


    public List<GbOrgPointInfo> getMiniStaffPointList(Long staffId) {

        return pointStaffMapper.getStaffPointList(staffId);
    }


    public List<GbOrgStaffInfo> getMiniLeaderStaffList(Long leaderId) {


        LambdaQueryWrapper<GbOrgStaffInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrgStaffInfo::getLeaderId, leaderId);
        queryWrapper.orderByAsc(GbOrgStaffInfo::getStaffId);
        queryWrapper.last("limit 0, 20");
        List<GbOrgStaffInfo> result = mapper.selectList(queryWrapper);


        for (GbOrgStaffInfo item : result) {
            List<GbOrgPointInfo> temp = pointStaffMapper.getStaffPointList(item.getStaffId());
            if (temp == null) temp = new ArrayList<>();
            item.setPointList(temp);
        }


        return result == null ? new ArrayList<>() : result;
    }


    public Long addMiniLeaderStaff(Long leaderId, GbOrgStaffInfo info, List<Long> pointList) {

        GbOrgStaffInfo data = new GbOrgStaffInfo();

        data.setLeaderId(leaderId);

        data.setStaffName(info.getStaffName());

        data.setMobile(info.getMobile());

        data.setNickname(info.getNickname());

        data.setAvatar(info.getAvatar());

        data.setOpenid(info.getOpenid());

        data.setRemark(info.getRemark());

        data.setAuthList(info.getAuthList());

        data.setIsClose((byte) 0);

        data.setAddTime(TimeUtils.getTimeStamp());


        mapper.insert(data);
        Long staffId = data.getStaffId();


        if (pointList.size() > 0) {
            List<GbOrgPointStaff> dataList = new ArrayList<>();
            for (Long pid : pointList) {
                GbOrgPointStaff temp = new GbOrgPointStaff();
                temp.setStaffId(staffId);
                temp.setPointId(pid);
                dataList.add(temp);
            }
            if (dataList.size() > 0) pointStaffMapper.insert(dataList);
        }


        return staffId;
    }


    public Boolean editMiniLeaderStaff(Long leaderId, GbOrgStaffInfo info, List<Long> pointList) {

        LambdaUpdateWrapper<GbOrgStaffInfo> updateWrapper = Wrappers.lambdaUpdate();

        updateWrapper.set(GbOrgStaffInfo::getStaffName, info.getStaffName());


        if (info.getMobile() != null && info.getMobile().length() > 0) {

            updateWrapper.set(GbOrgStaffInfo::getMobile, info.getMobile());

            updateWrapper.set(GbOrgStaffInfo::getNickname, info.getNickname());

            updateWrapper.set(GbOrgStaffInfo::getAvatar, info.getAvatar());

            updateWrapper.set(GbOrgStaffInfo::getOpenid, info.getOpenid());
        }


        updateWrapper.set(GbOrgStaffInfo::getRemark, info.getRemark());

        updateWrapper.set(GbOrgStaffInfo::getAuthList, info.getAuthList());

        updateWrapper.eq(GbOrgStaffInfo::getStaffId, info.getStaffId());
        updateWrapper.eq(GbOrgStaffInfo::getLeaderId, leaderId);
        int flag = mapper.update(updateWrapper);


        Long staffId = info.getStaffId();
        LambdaQueryWrapper<GbOrgPointStaff> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrgPointStaff::getStaffId, staffId);
        pointStaffMapper.delete(queryWrapper);
        if (pointList.size() > 0) {
            List<GbOrgPointStaff> dataList = new ArrayList<>();
            for (Long pid : pointList) {
                GbOrgPointStaff temp = new GbOrgPointStaff();
                temp.setStaffId(staffId);
                temp.setPointId(pid);
                dataList.add(temp);
            }
            if (dataList.size() > 0) pointStaffMapper.insert(dataList);
        }


        return flag > 0 ? true : false;
    }


    public Boolean closeMiniLeaderStaff(Long leaderId, Long staffId, Integer status) {

        LambdaUpdateWrapper<GbOrgStaffInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.set(GbOrgStaffInfo::getIsClose, status);
        updateWrapper.eq(GbOrgStaffInfo::getStaffId, staffId);
        updateWrapper.eq(GbOrgStaffInfo::getLeaderId, leaderId);
        int flag = mapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }

    public int removeStaff(Long leaderId, Long staffId) {
        return mapper.removeStaff(leaderId,staffId);
    }


}
