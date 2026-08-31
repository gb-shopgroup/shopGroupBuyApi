package cn.com.shopgroup.goods.service.impl;

import cn.com.shopgroup.goods.mapper.GbGroupSalesInfoMapper;
import cn.com.shopgroup.goods.mapper.GbGroupSalesPriceMapper;
import cn.com.shopgroup.goods.service.GbGroupSalesInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class GbGroupSalesInfoServiceImpl implements GbGroupSalesInfoService {

    @Resource
    private GbGroupSalesInfoMapper mapper;

    @Resource
    private GbGroupSalesPriceMapper priceMapper;

}
