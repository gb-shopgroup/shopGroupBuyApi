package cn.com.shopgroup.user.service.impl;

import cn.com.shopgroup.user.mapper.GbSysConfigInfoMapper;
import cn.com.shopgroup.user.service.GbSysConfigInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class GbSysConfigInfoServiceImpl implements GbSysConfigInfoService {

    @Resource
    private GbSysConfigInfoMapper mapper;

}
