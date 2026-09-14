package cn.com.shopgroup.scheduled;

import cn.com.shopgroup.common.cache.RedisConstant;
import cn.com.shopgroup.common.cache.RedisHelper;
import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.service.TaskOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

// 超时未支付订单自动取消任务(Redis延迟队列驱动):
// 下单时订单号以"下单时间+15分钟"为score写入Redis ZSet(RedisOrderPayDelayQueueKey),
// 本任务每隔5秒取出并移除到期的订单号, 对仍未支付(pay_time=0且status=0)的订单执行
// 抢占式取消(status 0->6)并回补商品/SKU库存; 已支付的订单不命中CAS更新, 天然幂等。
// 说明: OrderReturnStockTaskScheduled(每15分钟DB兜底扫描)保留, 作为Redis数据丢失时的兜底。
@Component
@Slf4j
public class OrderPayDelayQueueTaskScheduled {

    @Resource
    private TaskOrderService service;

    @Resource
    private RedisHelper redisHelper;

    // 每次最多处理的到期订单数
    private int limit = 200;

    // 延迟队列消费: 每隔5秒扫描一次到期的未支付订单
    @Scheduled(fixedDelay = 5000)
    public void execute() {

        try {
            // 取出并移除到期的订单号(score <= 当前时间戳)
            double nowScore = (double) TimeUtils.getTimeStamp();
            List<Object> orderNoList = redisHelper.popDelayQueueItems(RedisConstant.RedisOrderPayDelayQueueKey, nowScore, limit);
            if (orderNoList == null || orderNoList.isEmpty()) {
                return;
            }

            log.info("延迟队列取消超时未支付订单==开始==, 到期订单数=" + orderNoList.size());
            int cancelCount = 0;
            for (Object item : orderNoList) {
                if (item == null) {
                    continue;
                }
                try {
                    String orderNo = String.valueOf(item);
                    if (service.cancelUnpaidOrderByOrderNoAndReturnStock(orderNo)) {
                        cancelCount++;
                    }
                } catch (Exception e) {
                    // 单个订单处理失败不影响其他订单
                    log.error("延迟队列取消订单异常: orderNo={}", item, e);
                }
            }
            if (cancelCount > 0) {
                log.info("延迟队列取消超时未支付订单==结束==, 本次取消订单数=" + cancelCount + "==" + TimeUtils.getNowTime());
            }
        } catch (Exception e) {
            // 任务本身异常只记录日志, 等待下一轮重试
            log.error("延迟队列取消超时未支付订单任务异常", e);
        }
    }

}
