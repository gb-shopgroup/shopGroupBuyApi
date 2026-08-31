package cn.com.shopgroup.order.service;

import cn.com.shopgroup.order.model.OrderTransactionLog;

import java.util.List;

// 订单交易流水表
public interface OrderTransactionLogService {

    // 根据订单号查询交易流水列表
    List<OrderTransactionLog> getListByOrderNo(String orderNo);

    // 根据交易流水号查询流水
    OrderTransactionLog getByTransactionNo(String transactionNo);

    // 新增交易流水
    int addTransactionLog(OrderTransactionLog log);

}
