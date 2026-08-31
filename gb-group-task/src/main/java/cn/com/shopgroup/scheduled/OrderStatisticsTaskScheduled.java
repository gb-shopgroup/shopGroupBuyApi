package cn.com.shopgroup.scheduled;

import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.order.model.GbReportBusinessInfo;
import cn.com.shopgroup.order.service.GbReportBusinessInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class OrderStatisticsTaskScheduled {
    @Resource
    private GbReportBusinessInfoService service;

    //todo 分布式？
    // 每天凌晨1点汇总昨天的分账信息
    @Scheduled(cron = "0 0 1 * * ?")
    public void execute() {

        // 分账汇总结束
        log.info("分账汇总==开始==：" + TimeUtils.getNowTime());

        // 开始时间(昨天凌晨)
        long startTime = this.getYesterdayZeroSecond();

        // 结束时间(今天凌晨)
        long endTime = this.getTodayZeroSecond();

        // 昨天日期
        String yesterday = this.getYesterdayDate();

        // 统计分账收入
        List<GbReportBusinessInfo> results = service.sumReportBusinessList(startTime, endTime);

        // 补充日期
        for (GbReportBusinessInfo item : results) {

            item.setStartTime((int) startTime);
            item.setEndTime((int) endTime);
            item.setReportName(yesterday);

            // 记录一下
            //log.error("分账汇总：busId = " + item.getBusId() + ", busFee = " + MoneyUtil.centToYuan(item.getBusFee()));
        }

        // 添加到数据库
        if (results.size() > 0) {
            service.addMiniLeaderReportBusiness(results);
        }

        // 分账汇总结束
        log.info("分账汇总==结束==：" + TimeUtils.getNowTime());
    }

    // 今天零点秒级时间戳
    private long getTodayZeroSecond() {

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis() / 1000;
    }

    // 昨天零点秒级时间戳
    private long getYesterdayZeroSecond() {

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis() / 1000;
    }

    // 获取昨天的日期格式
    private String getYesterdayDate() {

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date date = calendar.getTime();
        SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");
        return SDF.format(date);
    }


}
