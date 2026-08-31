package cn.com.shopgroup.order.service.impl;

import cn.com.shopgroup.order.mapper.OrderTransactionLogMapper;
import cn.com.shopgroup.order.model.OrderTransactionLog;
import cn.com.shopgroup.order.service.OrderTransactionLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderTransactionLogServiceImpl implements OrderTransactionLogService {

    @Resource
    private OrderTransactionLogMapper mapper;

    @Override
    public List<OrderTransactionLog> getListByOrderNo(String orderNo) {

        LambdaQueryWrapper<OrderTransactionLog> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(OrderTransactionLog::getOrderNo, orderNo);
        queryWrapper.orderByDesc(OrderTransactionLog::getId);
        List<OrderTransactionLog> result = mapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }

    @Override
    public OrderTransactionLog getByTransactionNo(String transactionNo) {

        LambdaQueryWrapper<OrderTransactionLog> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(OrderTransactionLog::getTransactionNo, transactionNo);
        return mapper.selectOne(queryWrapper);
    }

    @Override
    public int addTransactionLog(OrderTransactionLog log) {

        // 添加时间
        if (log.getAddTime() == null) {
            log.setAddTime((int) (System.currentTimeMillis() / 1000));
        }
        return mapper.insert(log);
    }

}
