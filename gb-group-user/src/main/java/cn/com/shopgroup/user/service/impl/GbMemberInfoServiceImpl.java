package cn.com.shopgroup.user.service.impl;

import cn.com.shopgroup.user.mapper.GbMemberInfoMapper;
import cn.com.shopgroup.user.model.GbMemberInfo;
import cn.com.shopgroup.user.service.GbMemberInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class GbMemberInfoServiceImpl implements GbMemberInfoService {

    @Resource
    private GbMemberInfoMapper mapper;


    public List<GbMemberInfo> getAdminMemberList(int page, int pageSize) {

        LambdaQueryWrapper<GbMemberInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.orderByDesc(GbMemberInfo::getMemberId);
        queryWrapper.last("limit " + (page - 1) * pageSize + "," + pageSize);
        List<GbMemberInfo> result = mapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }


    public Long getAdminMemberCount() {

        LambdaQueryWrapper<GbMemberInfo> queryWrapper = Wrappers.lambdaQuery();
        return mapper.selectCount(queryWrapper);
    }


    public GbMemberInfo getMemberInfo(Long memberId) {

        return mapper.selectById(memberId);
    }


    public GbMemberInfo getMemberInfoByMobile(String mobile) {

        LambdaQueryWrapper<GbMemberInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbMemberInfo::getMobile, mobile);
        queryWrapper.orderByDesc(GbMemberInfo::getMemberId);
        queryWrapper.last("limit 0,1");
        return mapper.selectOne(queryWrapper);
    }


    public Long addMiniMember(GbMemberInfo data) {

        mapper.insert(data);
        return data.getMemberId();
    }


    public GbMemberInfo getMiniMemberById(Long memberId) {

        LambdaQueryWrapper<GbMemberInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(
                GbMemberInfo::getMemberId,
                GbMemberInfo::getNickname,
                GbMemberInfo::getAvatar,
                GbMemberInfo::getMobile,
                GbMemberInfo::getOpenid);
        //queryWrapper.eq(GbMemberInfo::getIsClose, 0);
        queryWrapper.eq(GbMemberInfo::getMemberId, memberId);
        queryWrapper.orderByDesc(GbMemberInfo::getMemberId);
        queryWrapper.last("limit 0, 1");
        return mapper.selectOne(queryWrapper);
    }


    public GbMemberInfo getMiniMemberByOpenId(String openId) {

        LambdaQueryWrapper<GbMemberInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(
                GbMemberInfo::getMemberId,
                GbMemberInfo::getNickname,
                GbMemberInfo::getAvatar,
                GbMemberInfo::getMobile,
                GbMemberInfo::getOpenid);
        //queryWrapper.eq(GbMemberInfo::getIsClose, 0);
        queryWrapper.eq(GbMemberInfo::getOpenid, openId);
        queryWrapper.orderByDesc(GbMemberInfo::getMemberId);
        queryWrapper.last("limit 0, 1");
        return mapper.selectOne(queryWrapper);
    }


    public Boolean updateMemberErcode(Long memberId, String ercode) {

        LambdaUpdateWrapper<GbMemberInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.set(GbMemberInfo::getErcode, ercode);
        updateWrapper.eq(GbMemberInfo::getMemberId, memberId);
        int flag = mapper.update(updateWrapper);
        return flag > 0 ? true : false;
    }


}
