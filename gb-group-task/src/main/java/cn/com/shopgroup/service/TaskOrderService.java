package cn.com.shopgroup.service;

import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.goods.service.GbGoodsInfoService;
import cn.com.shopgroup.goods.service.GbGoodsSkuInfoService;
import cn.com.shopgroup.order.constants.OrderStatusEnum;
import cn.com.shopgroup.order.mapper.GbOrderGoodsInfoMapper;
import cn.com.shopgroup.order.mapper.GbOrderInfoMapper;
import cn.com.shopgroup.order.model.GbOrderGoodsInfo;
import cn.com.shopgroup.order.model.GbOrderInfo;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private GbOrderGoodsInfoMapper goodsMapper;

    @Resource
    private GbGoodsInfoService goodsService;

    @Resource
    private GbGoodsSkuInfoService skuService;

    // 查询待系统自动完成收货的订单, 定时任务需要"已分账核销但核销满7天仍未完成收货"的订单自动收货掉
    // verifyEndTime: 核销时间截止点(当前时间-7天), 核销时间早于该值的未完成订单才会被查询出来
    public List<Map<String, String>> getUnReceiptOrderIds(int verifyEndTime){

        return mapper.getUnReceiptOrderIds(verifyEndTime);
    }

    /**
     * 系统自动完成收货(一个订单一次事务):
     * 把"已分账且已核销、但7天内未主动确认收货"的订单补成真实完成态, 与店员整单核销/用户主动收货后的数据口径保持一致:
     * 1) 订单主表: 待收货(1)/部分收货(2) -> 已收货(3), 补齐收货时间/确认收货标记/更新时间(条件更新防误伤退款/售后/取消单)
     * 2) 订单商品表: 把未收足的商品行收货数量补足为"可收货数量"(购买数-已申请退款/退货退款占坑数), 避免退款部分被重复核销
     */
    @Transactional(rollbackFor = Exception.class)
    public void receiptOrder(String orderNo, Integer receiptTime){

        // 1. 抢占式更新订单主状态: 仅"待收货/部分收货"(未完成态)的订单才会被置为已收货,
        //    由 status in (1,2) + 置3 承担防重(处理过/全退/售后中的单不命中), 不再要求 receipt_time=0,
        //    避免"已主动确认收货(2且receipt_time>0)但商品未核销完"的订单永久无法自动完成
        LambdaUpdateWrapper<GbOrderInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.eq(GbOrderInfo::getOrderNo, orderNo);
        updateWrapper.in(GbOrderInfo::getStatus, OrderStatusEnum.PREPAID.getCode(), OrderStatusEnum.PART_RECEIVED.getCode());
        updateWrapper.set(GbOrderInfo::getStatus, OrderStatusEnum.RECEIVED.getCode());
        updateWrapper.set(GbOrderInfo::getReceiptTime, receiptTime);
        updateWrapper.set(GbOrderInfo::getClickConfirmFlag, 1);
        updateWrapper.set(GbOrderInfo::getUpdateTime, receiptTime);
        int flag = mapper.update(null, updateWrapper);
        if (flag <= 0) {
            // 订单已退款/售后/取消或已被处理过, 跳过(不重复回补商品行)
            log.info("自动完成收货跳过(订单状态不允许或已处理): orderNo={}", orderNo);
            return;
        }

        // 2. 同步订单商品: 未收足的商品行收货数量补足为可收货数量(购买数-退款/退货退款占坑数, 下限0、上限购买数)
        LambdaUpdateWrapper<GbOrderGoodsInfo> goodsWrapper = Wrappers.lambdaUpdate();
        goodsWrapper.eq(GbOrderGoodsInfo::getOrderNo, orderNo);
        goodsWrapper.setSql("receipt_num = LEAST(goods_num, GREATEST(goods_num - IFNULL(refund_num, 0) - IFNULL(refund_goods_num, 0), 0))");
        goodsWrapper.apply("goods_num - IFNULL(refund_num, 0) - IFNULL(refund_goods_num, 0) > receipt_num");
        goodsWrapper.set(GbOrderGoodsInfo::getUpdateTime, receiptTime);
        goodsMapper.update(null, goodsWrapper);
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
