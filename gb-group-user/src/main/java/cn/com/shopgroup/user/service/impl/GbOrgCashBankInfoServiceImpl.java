package cn.com.shopgroup.user.service.impl;

import cn.com.shopgroup.user.mapper.GbOrgCashBankInfoMapper;
import cn.com.shopgroup.user.model.GbOrgCashBankInfo;
import cn.com.shopgroup.user.service.GbOrgCashBankInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class GbOrgCashBankInfoServiceImpl implements GbOrgCashBankInfoService {

    @Resource
    private GbOrgCashBankInfoMapper mapper;


    public List<GbOrgCashBankInfo> getBankList(){

        LambdaQueryWrapper<GbOrgCashBankInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.select(GbOrgCashBankInfo::getBankId,GbOrgCashBankInfo::getBankName);
        queryWrapper.eq(GbOrgCashBankInfo::getIsClose, 0);
        queryWrapper.orderByAsc(GbOrgCashBankInfo::getBankId);
        List<GbOrgCashBankInfo> lists = mapper.selectList(queryWrapper);
        return lists == null ? new ArrayList<>() : lists;
    }

}
