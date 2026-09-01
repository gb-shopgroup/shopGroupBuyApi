package cn.com.shopgroup.scheduled;

import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.goods.service.GbGoodsInfoService;
import cn.com.shopgroup.goods.service.GbGoodsSkuInfoService;
import cn.com.shopgroup.order.model.GbOrderGoodsInfo;
import cn.com.shopgroup.service.TaskOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
@Slf4j
public class OrderReturnStockTaskScheduled {

    @Resource
    private TaskOrderService service;

    @Resource
    private GbGoodsInfoService goodsService;

    @Resource
    private GbGoodsSkuInfoService skuService;

    // 每次执行任务限制数量
    private int limit = 200;

    // 未支付订单恢复库存任务: 每天 8:00 ~ 20:00 之间每小时 15分、45分的时候执行
    @Scheduled(cron = "0 15,45 8-19 * * ?")
    public void execute() {

        // 执行开始
        log.info("未支付订单恢复库存任务==开始==" + TimeUtils.getNowTime());

        // 查询半个小时之前下的未支付的订单商品
        int time = TimeUtils.getTimeStamp() - 30 * 60;
        List<GbOrderGoodsInfo> resutls = service.getUnPayOrderGoodsList(time, limit);

        // 循环恢复库存(总库存 + SKU库存)
        for (GbOrderGoodsInfo item : resutls) {

            String orderNo = item.getOrderNo();
            Long goodsId = item.getGoodsId();
            int goodsNum = item.getGoodsNum();
            int packNum = item.getPackNum();
            goodsService.increaseGoodsStock(goodsId, goodsNum * packNum);
            // 回补SKU库存(下单时按skuId同步扣减)
            if (item.getSkuId() != null && item.getSkuId() > 0) {
                skuService.increaseGoodsStock(item.getSkuId(), goodsNum * packNum);
            }

            // 记录恢复订单商品库存
            log.info("恢复订单号=" + orderNo + ", 商品ID=" + goodsId + "的库存数量：" + goodsNum * packNum);
        }

        // 执行结束
        log.info("未支付订单恢复库存任务==结束==" + TimeUtils.getNowTime());
    }


}
