package cn.com.shopgroup.order.service.impl;

import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.order.constants.PaymentStatusEnum;
import cn.com.shopgroup.order.model.GbOrderGoodsRefundRecord;
import cn.com.shopgroup.order.model.GbOrderInfo;
import cn.com.shopgroup.order.model.OrderTransactionLog;
import cn.com.shopgroup.order.service.GbOrderGoodsRefundRecordService;
import cn.com.shopgroup.order.service.GbOrderInfoService;
import cn.com.shopgroup.order.service.OrderTransactionLogService;
import cn.com.shopgroup.order.service.RefundConfirmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 退款回调成功核心落库(事务化), 详见 {@link RefundConfirmService}
 */
@Slf4j
@Service
public class RefundConfirmServiceImpl implements RefundConfirmService {

    // 退款成功记录的操作人标识, 与 GbOrderGoodsRefundRecordServiceImpl#existsSuccessRefundRecord 的幂等判重条件保持一致
    private static final String SYSTEM_REFUND_SUCCESS_OPERATOR = "系统退款成功";

    @Resource
    private GbOrderGoodsRefundRecordService refundRecordService;

    @Resource
    private GbOrderInfoService orderInfoService;

    @Resource
    private OrderTransactionLogService transactionLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmRefundSuccess(GbOrderInfo orderInfo, String refundNo, int refundCent, double amountYuan) {

        String orderNo = orderInfo.getOrderNo();
        // 1. 幂等判重: 该易宝退款单号已落过"系统退款成功"记录则跳过
        //    (按退款单号判重, 支持一单多次部分退款各自独立确认, 兼容Redis幂等键过期后易宝重发旧通知)
        if (refundRecordService.existsSuccessRefundRecord(refundNo)) {
            log.info("退款确认幂等: 订单{}退款单{}已确认过, 跳过重复落库", orderNo, refundNo);
            return false;
        }
        // 2. 落"系统退款成功"退款记录(带退款单号与实际金额, 供与易宝逐笔对账及幂等判重)
        GbOrderGoodsRefundRecord refundRecord = new GbOrderGoodsRefundRecord();
        refundRecord.setOrderNo(orderNo);
        refundRecord.setRefundNo(refundNo);
        refundRecord.setRefundAmount(refundCent);
        refundRecord.setRefundGoodsMsg("已退款" + amountYuan + "元");
        refundRecord.setOperateId(0L);
        refundRecord.setOperateName(SYSTEM_REFUND_SUCCESS_OPERATOR);
        refundRecord.setIsAgree(1);
        refundRecord.setActionReason("退款回调确认");
        refundRecord.setAddTime(TimeUtils.getTimeStamp());
        refundRecordService.addRefundRecord(refundRecord);
        // 3. 订单主表退款金额原子累加(申请/审核阶段不维护, 只在资金最终成功时按实际金额累加一次)
        if (refundCent > 0) {
            orderInfoService.addMiniOrderRefundFee(orderNo, refundCent);
        } else {
            log.warn("退款确认: 订单{}退款单{}回调金额为空或为0, 未累加订单退款金额, 请人工核对", orderNo, refundNo);
        }
        // 4. 订单表补全退款流水号(存最近一笔, 供展示/人工核对)与退款时间
        orderInfoService.updateOrderRefundInfo(orderNo, refundNo, TimeUtils.getTimeStamp());
        // 5. 退款成功交易流水(流水号直接使用易宝退款单号, 便于与易宝对账)
        OrderTransactionLog transactionLog = new OrderTransactionLog();
        transactionLog.setOrderNo(orderNo);
        transactionLog.setTransactionNo(refundNo);
        transactionLog.setRefundAmount(amountYuan);
        transactionLog.setPayAmount(amountYuan);
        transactionLog.setPayStatus(PaymentStatusEnum.REFUNDED.getCode());
        transactionLog.setPayMethod("yeePay");
        transactionLog.setOperatorName(SYSTEM_REFUND_SUCCESS_OPERATOR);
        transactionLog.setAddTime(TimeUtils.getTimeStamp());
        transactionLog.setRemark("退款回调成功,退款单号:" + refundNo);
        transactionLogService.addTransactionLog(transactionLog);
        log.info("退款确认落库完成: 订单{}, 退款单{}, 实际退款{}分", orderNo, refundNo, refundCent);
        return true;
    }
}