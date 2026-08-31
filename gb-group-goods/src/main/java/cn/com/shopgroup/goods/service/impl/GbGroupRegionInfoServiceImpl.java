package cn.com.shopgroup.goods.service.impl;

import cn.com.shopgroup.goods.mapper.GbGroupRegionInfoMapper;
import cn.com.shopgroup.goods.service.GbGroupRegionInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class GbGroupRegionInfoServiceImpl implements GbGroupRegionInfoService {

    @Resource
    private GbGroupRegionInfoMapper mapper;

}
