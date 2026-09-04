package cn.com.shopgroup.service;

import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.goods.service.GbGoodsInfoService;
import cn.com.shopgroup.goods.service.GbGoodsSkuInfoService;
import cn.com.shopgroup.order.constants.OrderStatusEnum;
import cn.com.shopgroup.order.mapper.GbOrderInfoMapper;
import cn.com.shopgroup.order.model.GbOrderGoodsInfo;
import cn.com.shopgroup.order.model.GbOrderInfo;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TaskOrderService {

    @Resource
    private GbOrderInfoMapper mapper;

    @Resource
    private GbGoodsInfoService goodsService;

    @Resource
    private GbGoodsSkuInfoService skuService;

    // 查询未收货订单id, 定时任务需要"已经分账核销但用户未主动收货的订单"自动收货掉
    public List<Map<String, String>> getUnReceiptOrderIds(int startTime, int endTime){

        return mapper.getUnReceiptOrderIds(startTime, endTime);
    }

    // 修改订单收货时间, 系统替用户收货
    public void receiptOrder(String orderNo, Integer receiptTime){

        LambdaUpdateWrapper<GbOrderInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(GbOrderInfo::getOrderNo, orderNo);
        updateWrapper.set(GbOrderInfo::getReceiptTime, receiptTime);
        mapper.update(null, updateWrapper);
    }

    // 查询未支付的订单, 用于返回商品库存
    public List<GbOrderGoodsInfo> getUnPayOrderGoodsList(int time, int limit){

        return mapper.getUnPayOrderGoodsList(time, limit);
    }

    /**
     * 超时未支付订单自动取消并恢复库存:
     * 下单超过指定时间(30分钟)仍未支付(pay_time=0)且仍为待支付(status=0)的订单,
     * 先抢占式把订单状态改为已取消(6), 抢单成功(0->6 恰好更新1行)后才回补该订单商品库存(商品总库存 + SKU库存),
     * 避免重复执行/并发触发导致同一订单被重复回补库存
     *
     * @param time  截止时间戳(下单时间小于该值即视为超时)
     * @param limit 本次最多处理的订单商品行数
     * @return 实际取消的订单数
     */
    public int cancelTimeoutUnpaidOrderAndReturnStock(int time, int limit){

        // 查询超时未支付且仍为待支付的订单商品(仅 is_stock=1 开启库存管理的商品行, 与下单扣减口径一致)
        List<GbOrderGoodsInfo> goodsList = mapper.getUnPayOrderGoodsList(time, limit);
        if (goodsList == null || goodsList.isEmpty()) {
            return 0;
        }

        // 按订单号分组, 保证同一订单的库存只回补一次
        Map<String, List<GbOrderGoodsInfo>> orderMap = goodsList.stream()
                .collect(Collectors.groupingBy(GbOrderGoodsInfo::getOrderNo, LinkedHashMap::new, Collectors.toList()));

        int now = TimeUtils.getTimeStamp();
        int cancelCount = 0;
        for (Map.Entry<String, List<GbOrderGoodsInfo>> entry : orderMap.entrySet()) {
            String orderNo = entry.getKey();
            // 抢占式取消: 仅当订单仍为待支付且未支付时(status 0->6 更新成功)才视为取消成功
            if (cancelUnpaidOrder(orderNo, now) > 0) {
                // 恢复商品总库存 + SKU库存
                for (GbOrderGoodsInfo item : entry.getValue()) {
                    int packNum = item.getPackNum() == null || item.getPackNum() == 0 ? 1 : item.getPackNum();
                    int stockNum = item.getGoodsNum() * packNum;
                    goodsService.increaseGoodsStock(item.getGoodsId(), stockNum);
                    // 回补SKU库存(下单时按skuId同步扣减)
                    if (item.getSkuId() != null && item.getSkuId() > 0) {
                        skuService.increaseGoodsStock(item.getSkuId(), stockNum);
                    }
                    log.info("超时未支付订单取消并恢复库存: orderNo={}, goodsId={}, skuId={}, stockNum={}", orderNo, item.getGoodsId(), item.getSkuId(), stockNum);
                }
                cancelCount++;
                log.info("超时未支付订单已取消: orderNo={}, 恢复商品行数={}", orderNo, entry.getValue().size());
            }
        }
        return cancelCount;
    }

    // 将未支付订单状态改为已取消(CAS式更新, 只更新"待支付(status=0)且未支付(pay_time=0)"的订单)
    public int cancelUnpaidOrder(String orderNo, Integer updateTime) {

        LambdaUpdateWrapper<GbOrderInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(GbOrderInfo::getOrderNo, orderNo);
        updateWrapper.eq(GbOrderInfo::getStatus, OrderStatusEnum.UNPAID.getCode());
        updateWrapper.eq(GbOrderInfo::getPayTime, 0);
        updateWrapper.set(GbOrderInfo::getStatus, OrderStatusEnum.CANCELED.getCode());
        updateWrapper.set(GbOrderInfo::getUpdateTime, updateTime);
        return mapper.update(null, updateWrapper);
    }

}
