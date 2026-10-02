package cn.com.shopgroup.order.service.impl;

import cn.com.shopgroup.order.mapper.GbLeaderMerchantMonthlyAmountMapper;
import cn.com.shopgroup.order.model.GbLeaderMerchantMonthlyAmount;
import cn.com.shopgroup.order.service.GbLeaderMerchantMonthlyAmountService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class GbLeaderMerchantMonthlyAmountServiceImpl implements GbLeaderMerchantMonthlyAmountService {

    @Resource
    private GbLeaderMerchantMonthlyAmountMapper mapper;

    @Override
    public GbLeaderMerchantMonthlyAmount getByMerchantAndYearMonth(Long leaderId, String merchantNo, String yearMonth) {

        LambdaQueryWrapper<GbLeaderMerchantMonthlyAmount> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbLeaderMerchantMonthlyAmount::getLeaderId, leaderId);
        queryWrapper.eq(GbLeaderMerchantMonthlyAmount::getMerchantNo, merchantNo);
        queryWrapper.eq(GbLeaderMerchantMonthlyAmount::getYearMonth, yearMonth);
        return mapper.selectOne(queryWrapper);
    }

    @Override
    public List<GbLeaderMerchantMonthlyAmount> listByLeaderAndYearMonth(Long leaderId, String yearMonth) {

        LambdaQueryWrapper<GbLeaderMerchantMonthlyAmount> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbLeaderMerchantMonthlyAmount::getLeaderId, leaderId);
        queryWrapper.eq(GbLeaderMerchantMonthlyAmount::getYearMonth, yearMonth);
        queryWrapper.orderByDesc(GbLeaderMerchantMonthlyAmount::getTotalAmount);
        List<GbLeaderMerchantMonthlyAmount> result = mapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }

    @Override
    public int add(GbLeaderMerchantMonthlyAmount info) {
        return mapper.insert(info);
    }

    @Override
    public int edit(GbLeaderMerchantMonthlyAmount info) {
        return mapper.updateById(info);
    }

    @Override
    public int addTotalAmount(Long leaderId, Long busId, String merchantNo, String yearMonth, BigDecimal totalAmount) {
        return mapper.upsertTotalAmount(leaderId, busId, merchantNo, yearMonth, totalAmount);
    }

}
