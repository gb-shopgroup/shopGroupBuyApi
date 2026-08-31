package cn.com.shopgroup.user.service.impl;

import cn.com.shopgroup.user.mapper.GbOrgCashBankNoMapper;
import cn.com.shopgroup.user.model.GbOrgCashBankNo;
import cn.com.shopgroup.user.service.GbOrgCashBankNoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class GbOrgCashBankNoServiceImpl implements GbOrgCashBankNoService {

    @Resource
    private GbOrgCashBankNoMapper mapper;


    public Long addMiniLeaderBankNo(Long leaderId, String bankNo, String bankName, String trueName){

        GbOrgCashBankNo data = new GbOrgCashBankNo();
        data.setLeaderId(leaderId);
        data.setBankNo(bankNo);
        data.setBankName(bankName);
        data.setTrueName(trueName);
        mapper.insert(data);
        return data.getBanknoId();
    }


    public Boolean isMiniLeaderBankNoExist(Long leaderId, String bankNo){

        LambdaQueryWrapper<GbOrgCashBankNo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrgCashBankNo::getLeaderId, leaderId);
        queryWrapper.eq(GbOrgCashBankNo::getBankNo, bankNo);
        queryWrapper.orderByDesc(GbOrgCashBankNo::getBanknoId);
        queryWrapper.last("limit 0, 1");
        GbOrgCashBankNo res = mapper.selectOne(queryWrapper);
        return res == null ? false : true;
    }


    public List<GbOrgCashBankNo> getMiniLeaderBankNoList(Long leaderId){

        LambdaQueryWrapper<GbOrgCashBankNo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrgCashBankNo::getLeaderId, leaderId);
        queryWrapper.orderByDesc(GbOrgCashBankNo::getBanknoId);
        List<GbOrgCashBankNo> result = mapper.selectList(queryWrapper);
        return  result == null ? new ArrayList<>() : result;
    }


}
