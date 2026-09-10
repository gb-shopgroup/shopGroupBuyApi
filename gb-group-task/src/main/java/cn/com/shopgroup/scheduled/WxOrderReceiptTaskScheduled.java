package cn.com.shopgroup.scheduled;

import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.common.wxmini.WxMiniAccessTokenHelper;
import cn.com.shopgroup.common.wxmini.WxMiniProgramHelper;
import cn.com.shopgroup.order.model.GbOrderBusinessInfo;
import cn.com.shopgroup.service.BusinessOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

// 微信订单收货状态同步任务
@Component
@Slf4j
public class WxOrderReceiptTaskScheduled {

    @Resource
    private BusinessOrderService service;

    @Resource
    private WxMiniAccessTokenHelper helper;

    // 每次执行任务限制数量
    private int limit = 200;

    // 只查询近48小时内的订单
    private int hours = 48;

    // 微信订单收货状态同步: 全天每隔15分钟执行一次
    // 微信订单状态: (1)待发货 (2)已发货 (3)确认收货 (4)交易完成 (5)已退款 (6)资金待结算
    @Scheduled(cron = "0 */15 * * * ?")
    public void execute() {

        // 记录日志
        log.info("微信订单收货状态同步任务执行==开始==：" + TimeUtils.getNowTime());

        // 查询近48小时内已调用微信发货(wx_shipment=1)且用户未确认收货(click_confirm_flag=0)的订单
        int startTime = TimeUtils.getTimeStamp() - hours * 60 * 60;
        List<GbOrderBusinessInfo> dataList = service.getUnConfirmReceiptBusinessOrderList(startTime, limit);
        if (dataList == null || dataList.isEmpty()) {
            log.info("微信订单收货状态同步任务执行==结束==：无待处理订单");
            return;
        }

        // 统一获取AccessToken
        String accessToken = helper.getAccessToken(false);
        if (accessToken == null || accessToken.length() == 0) {
            log.warn("微信订单收货状态同步任务执行==错误==：获取AccessToken失败");
            return;
        }

        // 同步收货数量
        int receiptCount = 0;

        // 同步发货状态数量
        int sendCount = 0;

        // 同步退款状态数量
        int refundCount = 0;

        // 循环查询微信订单收货状态
        for (GbOrderBusinessInfo item : dataList) {

            String orderNo = item.getOrderNo();
            String transactionId = item.getTransactionId();
            if (transactionId == null || transactionId.length() == 0) continue;

            int orderState;
            try {
                // 查询微信订单发货状态
                orderState = WxMiniProgramHelper.getWxOrder(accessToken, transactionId);
            } catch (Exception e) {
                log.error("微信订单收货状态查询异常: orderNo = " + orderNo, e);
                continue;
            }

            // accessToken失效, 刷新后本轮结束
            if (orderState == -1) {
                helper.removeAccessToken();
                break;
            }

            // 0 表示查询失败, 1 表示待发货, 均跳过
            if (orderState <= 1) continue;

            // 同步微信状态到收款账户订单(gb_order_business_info)
            // 微信状态5=已退款: 同步退款状态(comm_status=5 已退款)
            // 微信状态>=2(2已发货/3确认收货/4交易完成/6资金待结算): 同步发货状态(is_send=1/comm_status=1 已发货)
            boolean syncFlag;
            if (orderState == 5) {
                syncFlag = service.syncBusinessOrderRefundStatus(orderNo);
                if (syncFlag) refundCount++;
            } else {
                syncFlag = service.syncBusinessOrderSendStatus(orderNo);
                if (syncFlag) sendCount++;
            }

            // 微信返回收货状态大于1(2=已发货及以后), 同步标记订单已确认收货
            boolean flag = service.updateBusinessOrderReceiptStatus(orderNo);
            if (flag) receiptCount++;
            log.info("微信订单收货状态同步: orderNo = " + orderNo + ", orderState = " + orderState + ", totalSync = " + syncFlag + ", receiptSync = " + flag);
        }

        // 记录日志
        log.info("微信订单收货状态同步任务执行==结束==：处理订单数=" + dataList.size() + ", 同步发货数=" + sendCount + ", 同步退款数=" + refundCount + ", 同步收货数=" + receiptCount + "：" + TimeUtils.getNowTime());
    }

}
