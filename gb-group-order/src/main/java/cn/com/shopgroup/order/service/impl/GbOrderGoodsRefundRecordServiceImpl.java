package cn.com.shopgroup.order.service.impl;

import cn.com.shopgroup.order.mapper.GbOrderGoodsRefundRecordMapper;
import cn.com.shopgroup.order.model.GbOrderGoodsRefundRecord;
import cn.com.shopgroup.order.service.GbOrderGoodsRefundRecordService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class GbOrderGoodsRefundRecordServiceImpl implements GbOrderGoodsRefundRecordService {

    @Resource
    private GbOrderGoodsRefundRecordMapper mapper;

    @Override
    public List<GbOrderGoodsRefundRecord> getRefundRecordListByOrderNo(String orderNo) {

        LambdaQueryWrapper<GbOrderGoodsRefundRecord> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrderGoodsRefundRecord::getOrderNo, orderNo);
        queryWrapper.orderByAsc(GbOrderGoodsRefundRecord::getId);
        List<GbOrderGoodsRefundRecord> result = mapper.selectList(queryWrapper);
        return result == null ? new ArrayList<>() : result;
    }

    @Override
    public int addRefundRecord(GbOrderGoodsRefundRecord record) {

        // 添加时间
        if (record.getAddTime() == null) {
            record.setAddTime((int) (System.currentTimeMillis() / 1000));
        }
        return mapper.insert(record);
    }
}
