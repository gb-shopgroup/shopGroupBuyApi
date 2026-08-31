package cn.com.shopgroup.goods.service.impl;

import cn.com.shopgroup.goods.mapper.GbGoodsRestockInfoMapper;
import cn.com.shopgroup.goods.service.GbGoodsRestockInfoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class GbGoodsRestockInfoServiceImpl implements GbGoodsRestockInfoService {

    @Resource
    private GbGoodsRestockInfoMapper mapper;

}
