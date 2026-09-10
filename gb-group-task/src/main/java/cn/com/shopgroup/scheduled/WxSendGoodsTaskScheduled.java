package cn.com.shopgroup.scheduled;

import cn.com.shopgroup.common.cache.RedisHelper;
import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.common.wxmini.WxMiniAccessTokenHelper;
import cn.com.shopgroup.common.wxmini.WxMiniProgramHelper;
import cn.com.shopgroup.order.model.GbOrderBusinessInfo;
import cn.com.shopgroup.order.service.GbOrderInfoService;
import cn.com.shopgroup.service.BusinessOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
@Slf4j
public class WxSendGoodsTaskScheduled {
    @Resource
    private BusinessOrderService service;

    @Resource
    private GbOrderInfoService orderInfoService;

    @Resource
    private RedisHelper redisHelper;

    @Resource
    private WxMiniAccessTokenHelper helper;

    // 每次执行任务限制数量
    private int limit = 200;

    // 订单自动微信发货: 每天 6:00 ~ 19:30 整点、半点执行，20点不执行
   // @Scheduled(cron = "0 0,30 6-19 * * ?")
    public void execute() {

        // 记录日志
        log.info("订单自动微信发货任务执行==开始==：" + TimeUtils.getNowTime());

        // 统一获取AccessToken
        String accessToken = helper.getAccessToken(false);
        if (accessToken == null || accessToken.length() == 0) {
            log.warn("订单自动微信发货任务执行==错误==：获取AccessToken失败");
            return;
        }

        // 查询未发货的订单列表, 每次 limit 条记录
        List<GbOrderBusinessInfo> dataList = service.getUnSendBusinessOrderList(limit);

        // 当前时间
        int nowTime = TimeUtils.getTimeStamp();
        int cha = 15 * 60; // 15分钟的时间间隔

        // 循环发货
        for (GbOrderBusinessInfo item : dataList) {

            // 下单时间判断
            int addTime = item.getAddTime();
            if (nowTime - addTime <= cha) continue;

            // 订单发货
            String transactionId = item.getTransactionId();
            String goodsName = item.getGroupName();
            String openid = item.getOpenid();
            int isSuccess = WxMiniProgramHelper.uploadShippingInfo(accessToken, transactionId, goodsName, openid);
            if (isSuccess == 1) {
                // 更新发货标识
                service.updateBusinessOrderSendStatus(item.getOrderNo());
                // 发货成功后同步订单的微信发货标识(wx_shipment:0=未调用,1=已调用)
                orderInfoService.updateWxShipment(item.getOrderNo());
                log.info("自动微信发货成功: orderId = " + item.getOrderNo() + ", orderSn = " + item.getOrderSn());
            } else {
                // 考虑 accessToken 失效问题
                if (isSuccess == -1) {
                    helper.removeAccessToken();
                    break;
                }
                // 记录发货失败
                log.error("自动微信发货失败: orderId = " + item.getOrderNo() + ", orderSn = " + item.getOrderSn());
            }
        }

        // 记录日志
        log.info("订单自动微信发货任务执行==结束==：" + TimeUtils.getNowTime());
        //log.error("订单自动微信发货任务执行：" + TimeUtils.getNowTime());
        //if(dataList.size() > 0){
        //    int id = dataList.get(0).getOrderId();
        //    int id2 = dataList.get(dataList.size()-1).getOrderId();
        //    log.error("订单自动微信发货任务执行：数量 = " + dataList.size() + ", 第一个订单id = " + id + ", 最后一个订单id=" + id2);
        //}
    }


}
