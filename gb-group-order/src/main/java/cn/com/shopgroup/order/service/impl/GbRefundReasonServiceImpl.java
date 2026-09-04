package cn.com.shopgroup.order.service.impl;

import cn.com.shopgroup.order.mapper.GbRefundReasonMapper;
import cn.com.shopgroup.order.model.GbRefundReason;
import cn.com.shopgroup.order.service.GbRefundReasonService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class GbRefundReasonServiceImpl implements GbRefundReasonService {

    @Resource
    private GbRefundReasonMapper mapper;

    @Override
    public List<GbRefundReason> getEnabledReasonList() {

        LambdaQueryWrapper<GbRefundReason> queryWrapper = Wrappers.lambdaQuery();
        // 只返回启用中的原因, 按排序值升序, 排序相同按id升序
        queryWrapper.eq(GbRefundReason::getStatus, 1);
        queryWrapper.orderByAsc(GbRefundReason::getSort);
        queryWrapper.orderByAsc(GbRefundReason::getId);
        List<GbRefundReason> result = mapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }
}
