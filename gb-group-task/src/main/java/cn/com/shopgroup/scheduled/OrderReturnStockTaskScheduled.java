package cn.com.shopgroup.scheduled;

import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.service.TaskOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

// 超时未支付订单自动取消并恢复库存任务
@Component
@Slf4j
public class OrderReturnStockTaskScheduled {

    @Resource
    private TaskOrderService service;

    // 每次执行任务限制数量
    private int limit = 200;

    // 未支付订单超时(下单30分钟未支付)自动取消并恢复库存: 全天每隔5分钟执行一次
    @Scheduled(cron = "0 */5 * * * ?")
    public void execute() {

        // 执行开始
        log.info("超时未支付订单自动取消并恢复库存任务==开始==" + TimeUtils.getNowTime());

        // 处理下单时间早于(当前时间-30分钟)的待支付订单: 状态改为已取消 + 恢复商品/SKU库存
        int time = TimeUtils.getTimeStamp() - 30 * 60;
        int cancelCount = service.cancelTimeoutUnpaidOrderAndReturnStock(time, limit);

        // 执行结束
        log.info("超时未支付订单自动取消并恢复库存任务==结束==, 本次取消订单数=" + cancelCount + "==" + TimeUtils.getNowTime());
    }

}
