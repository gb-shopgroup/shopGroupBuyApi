package cn.com.shopgroup.goods.service.impl;

import cn.com.shopgroup.goods.mapper.GbGroupCollectionActivityMapper;
import cn.com.shopgroup.goods.mapper.GbGroupCollectionInfoMapper;
import cn.com.shopgroup.goods.service.GbGroupCollectionInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class GbGroupCollectionInfoServiceImpl implements GbGroupCollectionInfoService {

    @Resource
    private GbGroupCollectionInfoMapper mapper;

    @Resource
    private GbGroupCollectionActivityMapper activityMapper;

}
