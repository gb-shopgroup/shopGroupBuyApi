package cn.com.shopgroup.order.service;

import cn.com.shopgroup.order.model.GbOrderGoodsRefundRecord;

import java.util.List;

// 订单退款记录信息表
public interface GbOrderGoodsRefundRecordService {

    // 根据订单号查询退款记录
    List<GbOrderGoodsRefundRecord> getRefundRecordListByOrderNo(String orderNo);

    // 新增退款记录
    int addRefundRecord(GbOrderGoodsRefundRecord record);

}
