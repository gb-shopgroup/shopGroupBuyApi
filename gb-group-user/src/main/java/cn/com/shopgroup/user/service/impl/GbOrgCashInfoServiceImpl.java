package cn.com.shopgroup.user.service.impl;

import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.user.mapper.GbOrgCashInfoMapper;
import cn.com.shopgroup.user.model.GbOrgCashInfo;
import cn.com.shopgroup.user.service.GbOrgCashInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class GbOrgCashInfoServiceImpl implements GbOrgCashInfoService {

    @Resource
    private GbOrgCashInfoMapper mapper;


    public List<GbOrgCashInfo> getMiniLeaderCashList(Long leaderId, int page, int pageSize) {

        LambdaQueryWrapper<GbOrgCashInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrgCashInfo::getLeaderId, leaderId);
        queryWrapper.orderByDesc(GbOrgCashInfo::getCashId);
        queryWrapper.last("limit " + (page - 1) * pageSize + "," + pageSize);
        List<GbOrgCashInfo> result = mapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }


    public Long getMiniLeaderCashCount(Long leaderId) {

        LambdaQueryWrapper<GbOrgCashInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrgCashInfo::getLeaderId, leaderId);
        return mapper.selectCount(queryWrapper);
    }


    public Long addMiniLeaderCash(Long leaderId, GbOrgCashInfo info) {

        GbOrgCashInfo data = new GbOrgCashInfo();

        data.setBusId(info.getBusId());

        data.setLeaderId(leaderId);

        data.setCashFee(info.getCashFee());

        data.setCashBank(info.getCashBank());

        data.setCashBankNo(info.getCashBankNo());

        data.setTrueName(info.getTrueName());

        data.setCashStatus((byte) 1);

        data.setCashNo("");

        data.setCashTime(0);

        data.setAddTime(TimeUtils.getTimeStamp());


        mapper.insert(data);


        return data.getCashId();
    }


    public Boolean editMiniLeaderCash(Long leaderId, Long cashId, String cashNo) {

        LambdaUpdateWrapper<GbOrgCashInfo> updateWrapper = Wrappers.lambdaUpdate();


        updateWrapper.eq(GbOrgCashInfo::getCashId, cashId);

        updateWrapper.eq(GbOrgCashInfo::getLeaderId, leaderId);


        updateWrapper.set(GbOrgCashInfo::getCashStatus, (byte) 2);

        updateWrapper.set(GbOrgCashInfo::getCashNo, cashNo);

        updateWrapper.set(GbOrgCashInfo::getCashTime, TimeUtils.getTimeStamp());


        int flag = mapper.update(updateWrapper);


        return flag > 0 ? true : false;
    }


}
