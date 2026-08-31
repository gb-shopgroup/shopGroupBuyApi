package cn.com.shopgroup.user.service.impl;

import cn.com.shopgroup.user.mapper.GbSysUserInfoMapper;
import cn.com.shopgroup.user.model.GbSysUserInfo;
import cn.com.shopgroup.user.service.GbSysUserInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class GbSysUserInfoServiceImpl implements GbSysUserInfoService {

    @Resource
    private GbSysUserInfoMapper mapper;


    public GbSysUserInfo getUserByUsername(String username){

        LambdaQueryWrapper<GbSysUserInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbSysUserInfo:: getUserName, username);
        queryWrapper.orderByDesc(GbSysUserInfo::getUserId);
        queryWrapper.last("limit 0,1");
        return mapper.selectOne(queryWrapper);
    }

}
