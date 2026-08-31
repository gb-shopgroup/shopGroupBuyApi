package cn.com.shopgroup.user.service.impl;

import cn.com.shopgroup.user.mapper.GbRegionAreaInfoMapper;
import cn.com.shopgroup.user.service.GbRegionAreaInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class GbRegionAreaInfoServiceImpl implements GbRegionAreaInfoService {

    @Resource
    private GbRegionAreaInfoMapper mapper;

}
