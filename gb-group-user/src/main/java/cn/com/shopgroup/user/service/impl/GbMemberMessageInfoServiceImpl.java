package cn.com.shopgroup.user.service.impl;

import cn.com.shopgroup.user.mapper.GbMemberMessageInfoMapper;
import cn.com.shopgroup.user.service.GbMemberMessageInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class GbMemberMessageInfoServiceImpl implements GbMemberMessageInfoService {

    @Resource
    private GbMemberMessageInfoMapper mapper;

}
