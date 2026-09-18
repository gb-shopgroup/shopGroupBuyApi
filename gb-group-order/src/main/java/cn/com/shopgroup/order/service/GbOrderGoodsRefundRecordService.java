package cn.com.shopgroup.order.service;

import cn.com.shopgroup.order.model.GbOrderGoodsRefundRecord;

import java.util.List;

// 订单退款记录信息表
public interface GbOrderGoodsRefundRecordService {

    // 根据订单号查询退款记录
    List<GbOrderGoodsRefundRecord> getRefundRecordListByOrderNo(String orderNo);

    // 新增退款记录
    int addRefundRecord(GbOrderGoodsRefundRecord record);

    // 统计订单"已同意"(已向易宝发起退款)的累计退款金额(单位:分);
    // 用于审核同意发起退款时防超退校验: 已同意累计 + 本次 <= 订单实付金额
    int getAgreedRefundCentByOrderNo(String orderNo);

    // 该易宝退款单号是否已存在"系统退款成功"落库记录;
    // 作为退款回调的DB幂等基准(按退款单号判重, 支持一单多次部分退款, 兼容Redis幂等键过期后易宝重发旧通知)
    boolean existsSuccessRefundRecord(String refundNo);

}
