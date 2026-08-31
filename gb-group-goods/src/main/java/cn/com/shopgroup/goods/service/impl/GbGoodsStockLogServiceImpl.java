package cn.com.shopgroup.goods.service.impl;

import cn.com.shopgroup.goods.mapper.GbGoodsStockLogMapper;
import cn.com.shopgroup.goods.service.GbGoodsStockLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class GbGoodsStockLogServiceImpl implements GbGoodsStockLogService {

    @Resource
    private GbGoodsStockLogMapper mapper;

}
