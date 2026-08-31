package cn.com.shopgroup.user.service.impl;

import cn.com.shopgroup.user.mapper.GbMemberAddressInfoMapper;
import cn.com.shopgroup.user.model.GbMemberAddressInfo;
import cn.com.shopgroup.user.service.GbMemberAddressInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class GbMemberAddressInfoServiceImpl implements GbMemberAddressInfoService {

    @Resource
    private GbMemberAddressInfoMapper mapper;


    public GbMemberAddressInfo getMemberAddressInfo(int addressId) {

        return mapper.selectById(addressId);
    }


    public List<GbMemberAddressInfo> getMemberAddressList(Long memberId) {

        LambdaQueryWrapper<GbMemberAddressInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(
                GbMemberAddressInfo::getAddressId,
                GbMemberAddressInfo::getTrueName,
                GbMemberAddressInfo::getTelephone,
                GbMemberAddressInfo::getProvince,
                GbMemberAddressInfo::getCity,
                GbMemberAddressInfo::getDistrict,
                GbMemberAddressInfo::getAddress);
        queryWrapper.eq(GbMemberAddressInfo::getMemberId, memberId);
        queryWrapper.orderByDesc(GbMemberAddressInfo::getAddressId);
        queryWrapper.last("limit 0,10");
        List<GbMemberAddressInfo> result = mapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }


    public Long addMemberAddress(GbMemberAddressInfo data) {

        mapper.insert(data);
        return data.getAddressId();
    }


}
