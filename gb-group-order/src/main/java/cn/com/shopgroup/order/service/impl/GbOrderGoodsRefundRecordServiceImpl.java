package cn.com.shopgroup.order.service.impl;

import cn.com.shopgroup.order.mapper.GbOrderGoodsRefundRecordMapper;
import cn.com.shopgroup.order.model.GbOrderGoodsRefundRecord;
import cn.com.shopgroup.order.service.GbOrderGoodsRefundRecordService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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

    // 统计订单已同意(is_agree=1, 即已向易宝发起退款)的累计退款金额(单位:分)
    @Override
    public int getAgreedRefundCentByOrderNo(String orderNo) {

        if (orderNo == null || orderNo.length() == 0) {
            return 0;
        }
        QueryWrapper<GbOrderGoodsRefundRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("IFNULL(SUM(refund_amount), 0) AS total");
        queryWrapper.eq("order_no", orderNo);
        queryWrapper.eq("is_agree", 1);
        List<Object> objs = mapper.selectObjs(queryWrapper);
        if (objs == null || objs.isEmpty() || objs.get(0) == null) {
            return 0;
        }
        // SUM 返回类型依驱动版本可能为 BigDecimal/Long/Integer
        return Integer.parseInt(String.valueOf(objs.get(0)));
    }

    // 该易宝退款单号是否已存在"系统退款成功"落库记录(退款回调DB幂等基准)
    @Override
    public boolean existsSuccessRefundRecord(String refundNo) {

        if (refundNo == null || refundNo.length() == 0) {
            return false;
        }
        LambdaQueryWrapper<GbOrderGoodsRefundRecord> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(GbOrderGoodsRefundRecord::getRefundNo, refundNo);
        queryWrapper.eq(GbOrderGoodsRefundRecord::getIsAgree, 1);
        queryWrapper.eq(GbOrderGoodsRefundRecord::getOperateName, "系统退款成功");
        Long count = mapper.selectCount(queryWrapper);
        return count != null && count > 0;
    }
}
