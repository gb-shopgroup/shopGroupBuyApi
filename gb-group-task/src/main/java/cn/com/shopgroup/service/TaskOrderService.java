package cn.com.shopgroup.service;

import cn.com.shopgroup.order.mapper.GbOrderInfoMapper;
import cn.com.shopgroup.order.model.GbOrderGoodsInfo;
import cn.com.shopgroup.order.model.GbOrderInfo;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class TaskOrderService {

    @Resource
    private GbOrderInfoMapper mapper;

    // 查询未收货订单id, 定时任务需要"已经分账核销但用户未主动收货的订单"自动收货掉
    public List<Map<String, String>> getUnReceiptOrderIds(int startTime, int endTime){

        return mapper.getUnReceiptOrderIds(startTime, endTime);
    }

    // 修改订单收货时间, 系统替用户收货
    public void receiptOrder(String orderNo, Integer receiptTime){

        LambdaUpdateWrapper<GbOrderInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(GbOrderInfo::getOrderNo, orderNo);
        updateWrapper.set(GbOrderInfo::getReceiptTime, receiptTime);
        mapper.update(updateWrapper);
    }

    // 查询未支付的订单, 用于返回商品库存
    public List<GbOrderGoodsInfo> getUnPayOrderGoodsList(int time, int limit){

        return mapper.getUnPayOrderGoodsList(time, limit);
    }


}
