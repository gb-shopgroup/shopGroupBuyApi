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

    // 每天凌晨2点自动完成收货: 前提是已经分账且店员已核销的订单, 核销完成满7天仍未主动确认收货的, 系统自动完成
    @Scheduled(cron = "0 0 2 * * ?")
    public void execute() {

        // 开始
        log.info("自动完成收货任务==开始==" + TimeUtils.getNowTime());

        // 当前时间
        int nowTime = TimeUtils.getTimeStamp();

        // 核销完成时间截止点(核销时间早于当前时间-7天的才进入自动完成范围)
        int verifyEndTime = nowTime - 7 * 24 * 60 * 60;

        // 先查询订单, 定时任务需要"已经分账核销但用户未主动收货的订单"自动收货掉
        List<Map<String, String>> orderNos = service.getUnReceiptOrderIds(verifyEndTime);

        // 在修改订单
        if (CollectionUtil.isNotEmpty(orderNos)) {
            for (Map<String, String> item : orderNos) {

                // 系统替用户自动收货(以当前时间为完成时间)
                String orderNo = item.get("orderNo");
                service.receiptOrder(orderNo, nowTime);
                log.info("自动完成收货任务====" + orderNo);
            }
        }

        // 结束
        log.info("自动完成收货任务==结束==" + TimeUtils.getNowTime());
    }


}
