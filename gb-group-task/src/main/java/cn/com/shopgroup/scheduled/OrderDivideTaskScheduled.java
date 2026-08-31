package cn.com.shopgroup.scheduled;

import cn.com.shopgroup.common.cache.RedisConstant;
import cn.com.shopgroup.common.cache.RedisHelper;
import cn.com.shopgroup.common.utils.MoneyUtil;
import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.common.yeepay.YeepayUtils;
import cn.com.shopgroup.order.model.GbOrderBusinessInfo;
import cn.com.shopgroup.service.BusinessOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class OrderDivideTaskScheduled {

    @Resource
    private BusinessOrderService service;

    @Resource
    private RedisHelper redisHelper;


    // 每次执行最少 200 条记录
    private final int limit = 200;
    private final double taskNum = 8.0;


    // 自动订单分账: 每天 20:00 ~ 23:30 整点、半点执行，24点不执行
    // 一共 8 次 执行次数, 每次执行最少 200 条记录
    @Scheduled(cron = "0 0,30 20-23 * * ?")
    public void execute() {

        // 从redis中获取页码page
        int pageSize = limit;
        Boolean isExist = redisHelper.hasKey(RedisConstant.RedisLeaderOrderDividePageKey);
        long page = redisHelper.increment(RedisConstant.RedisLeaderOrderDividePageKey);
        if (isExist == false) {

            // 设置页码有效期5小时
            redisHelper.expire(RedisConstant.RedisLeaderOrderDividePageKey, RedisConstant.RedisLeaderOrderDividePageExpired, TimeUnit.SECONDS);

            // 查询总数量并缓存起来5小时
            long total = service.getUnDivideBusinessOrderCount();
            redisHelper.setCacheObject(RedisConstant.RedisLeaderOrderDivideTotalKey, total, RedisConstant.RedisLeaderOrderDividePageExpired, TimeUnit.SECONDS);

            // 如果一共800条记录, 执行8次定时任务, 则每次任务至少执行100条记录
            pageSize = (int) Math.ceil(total / taskNum);
            // 每次执行最少 200 条记录
            pageSize = pageSize > limit ? pageSize : limit;

            // 分账任务开始
            log.info("待分账的订单任务执行开始：====================== " + TimeUtils.getNowTime() + " ======================");
            log.info("待分账的订单任务执行开始：总数量 = " + total);
        }

        // 查询待分账的订单
        List<GbOrderBusinessInfo> dataList = service.getUnDivideBusinessOrderList((int) page, pageSize);

        // 循环分账
        for (GbOrderBusinessInfo item : dataList) {
            String orderNo = item.getOrderNo();
            String merchantNo = item.getMerchantNo();
            // 平台服务费分账
            String remark = "平台服务费";
            double amount = MoneyUtil.centToYuan(item.getServiceFee());

            // 商户自己分账
            String remark2 = "用户支付商品订单费用";
            double amount2 = MoneyUtil.centToYuan(item.getBusFee());

            // 开始分账
            Map<String, String> res = YeepayUtils.divide(orderNo, merchantNo, amount, remark, amount2, remark2);

            // 分账结果
            if (Integer.parseInt(res.get("success")) == 0) {
                String uniqueDivideNo = res.containsKey("uniqueDivideNo") ? res.get("uniqueDivideNo") : "";
                log.warn("订单分账失败：订单号 = " + orderNo + " , 易宝分账流水号 = " + uniqueDivideNo + " 原因：" + res.get("data"));
            } else {
                String status = res.get("data");
                String uniqueDivideNo = res.get("uniqueDivideNo");
                service.updateBusinessOrderDivideStatus(orderNo, status, uniqueDivideNo);
                log.info("订单分账成功：订单号 = " + orderNo);
            }
        }

        // 记录日志
        log.info("待分账的订单任务执行结束：page=" + page + ", pageSize=" + pageSize + ", 时间=" + TimeUtils.getNowTime());
        if (dataList.size() > 0) {
            String firstOrderNo = dataList.get(0).getOrderNo();
            String lastOrderNo = dataList.get(dataList.size() - 1).getOrderNo();
            log.info("待分账的订单任务执行结束：数量 = " + dataList.size() + ", 第一个订单号 = " + firstOrderNo + ", 最后一个订单号=" + lastOrderNo);
        }
    }


}
