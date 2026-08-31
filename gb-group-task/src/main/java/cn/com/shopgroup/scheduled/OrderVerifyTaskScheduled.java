package cn.com.shopgroup.scheduled;

import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.service.TaskOrderService;
import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

// 订单自动完成定时任务
@Component
@Slf4j
public class OrderVerifyTaskScheduled {

    @Resource
    private TaskOrderService service;

    // 每天凌晨2点自动收货, 前提已经分账且店员核销的订单们, 时间锁定在7天以内
    @Scheduled(cron = "0 0 2 * * ?")
    public void execute() {

        // 开始
        log.info("自动完成收货任务==开始==" + TimeUtils.getNowTime());

        // 当前时间(结束时间)
        int endTime = TimeUtils.getTimeStamp();

        // 7天时间(开始时间)
        int startTime = endTime - 600000;

        // 先查询订单, 定时任务需要"已经分账核销但用户未主动收货的订单"自动收货掉
        List<Map<String, String>> orderNos = service.getUnReceiptOrderIds(startTime, endTime);

        // 在修改订单
        if (CollectionUtil.isNotEmpty(orderNos)) {
            for (Map<String, String> item : orderNos) {

                // 系统替用户自动收货
                String orderNo = item.get("orderNo");
                service.receiptOrder(orderNo, endTime);
                log.info("自动完成收货任务====" + orderNo);
            }
        }

        // 结束
        log.info("自动完成收货任务==结束==" + TimeUtils.getNowTime());
    }


}
