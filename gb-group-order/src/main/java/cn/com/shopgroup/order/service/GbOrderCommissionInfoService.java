package cn.com.shopgroup.order.service;

import cn.com.shopgroup.order.mapper.GbOrderCommissionInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GbOrderCommissionInfoService {

    @Autowired
    private GbOrderCommissionInfoMapper mapper;

}
