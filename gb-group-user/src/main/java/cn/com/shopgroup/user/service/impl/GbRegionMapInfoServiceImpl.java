package cn.com.shopgroup.user.service.impl;

import cn.com.shopgroup.user.mapper.GbRegionMapInfoMapper;
import cn.com.shopgroup.user.service.GbRegionMapInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class GbRegionMapInfoServiceImpl implements GbRegionMapInfoService {

    @Resource
    private GbRegionMapInfoMapper mapper;

}
