package cn.com.shopgroup.scheduled;

import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.service.TaskGoodsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

// 到期团购活动自动下线任务
@Component
@Slf4j
public class GroupActivityCloseTaskScheduled {

    @Resource
    private TaskGoodsService service;

    // 每次执行任务限制数量
    private int limit = 100;

    // 到期团购活动自动下线: 全天每隔30分钟执行一次,
    // 在线(is_close=0)且 当前时间>=结束时间(end_time) 的团购活动, 置为下线(is_close=1)
    @Scheduled(cron = "0 */30 * * * ?")
    public void execute() {

        // 执行开始
        log.info("到期团购活动自动下线任务==开始==" + TimeUtils.getNowTime());

        // 下线到期活动: 查询在线活动(最多100条), 当前时间>=活动结束时间的置为下线
        int closeCount = service.closeExpiredGroupActivity(TimeUtils.getTimeStamp(), limit);

        // 执行结束
        log.info("到期团购活动自动下线任务==结束==, 本次下线活动数=" + closeCount + "==" + TimeUtils.getNowTime());
    }

}
