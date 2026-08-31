package cn.com.shopgroup.user.service.impl;

import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.user.mapper.GbMemberBlackListMapper;
import cn.com.shopgroup.user.model.GbMemberBlackList;
import cn.com.shopgroup.user.service.GbMemberBlackListService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class GbMemberBlackListServiceImpl implements GbMemberBlackListService {

    @Resource
    private GbMemberBlackListMapper mapper;


    public Boolean addMemberBlack(Long leaderId, GbMemberBlackList info){

        GbMemberBlackList data = new GbMemberBlackList();
        data.setLeaderId(leaderId);
        data.setMemberId(info.getMemberId());
        data.setMobile(info.getMobile());
        data.setNickName(info.getNickName());
        data.setAvatar(info.getAvatar());
        data.setExpireTime(0);
        data.setStaffId(info.getStaffId());
        data.setStaffName(info.getStaffName());
        data.setAddTime(TimeUtils.getTimeStamp());
        mapper.insert(data);
        return true;
    }


    public Boolean removeMemberBlack(Long leaderId, Long memberId){

        LambdaQueryWrapper<GbMemberBlackList> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbMemberBlackList::getLeaderId, leaderId);
        queryWrapper.eq(GbMemberBlackList::getMemberId, memberId);
        mapper.delete(queryWrapper);
        return true;
    }



    public Boolean getMemberBlackById(Long leaderId, Long memberId){

        LambdaQueryWrapper<GbMemberBlackList> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbMemberBlackList::getLeaderId, leaderId);
        queryWrapper.eq(GbMemberBlackList::getMemberId, memberId);
        queryWrapper.orderByDesc(GbMemberBlackList::getMemberId);
        queryWrapper.last("limit 0, 1");
        GbMemberBlackList res = mapper.selectOne(queryWrapper);
        return res != null ? true : false;
    }


    public GbMemberBlackList getMemberBlackByMobile(Long leaderId, String mobile){

        LambdaQueryWrapper<GbMemberBlackList> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbMemberBlackList::getLeaderId, leaderId);
        queryWrapper.eq(GbMemberBlackList::getMobile, mobile);
        queryWrapper.orderByDesc(GbMemberBlackList::getMemberId);
        queryWrapper.last("limit 0, 1");
        return mapper.selectOne(queryWrapper);
    }


    public List<GbMemberBlackList> getMemberBlackList(Long leaderId, int page, int pageSize){

        LambdaQueryWrapper<GbMemberBlackList> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbMemberBlackList::getLeaderId, leaderId);
        queryWrapper.orderByDesc(GbMemberBlackList::getAddTime);
        queryWrapper.last("limit " + (page - 1) * pageSize + "," + pageSize);
        List<GbMemberBlackList> results = mapper.selectList(queryWrapper);
        return results == null ? new ArrayList<>() : results;
    }


    public Long getMemberBlackCount(Long leaderId){

        LambdaQueryWrapper<GbMemberBlackList> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbMemberBlackList::getLeaderId, leaderId);
        queryWrapper.orderByDesc(GbMemberBlackList::getAddTime);
        return mapper.selectCount(queryWrapper);
    }


}
