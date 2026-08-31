package cn.com.shopgroup.goods.service.impl;

import cn.com.shopgroup.goods.mapper.GbGroupSalesSetMapper;
import cn.com.shopgroup.goods.service.GbGroupSalesSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class GbGroupSalesSetServiceImpl implements GbGroupSalesSetService {

    @Resource
    private GbGroupSalesSetMapper mapper;

}
