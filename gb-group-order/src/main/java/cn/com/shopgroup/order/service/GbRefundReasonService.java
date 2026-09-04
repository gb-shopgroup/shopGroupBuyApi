package cn.com.shopgroup.order.service;

import cn.com.shopgroup.order.model.GbRefundReason;

import java.util.List;

// 退款原因配置表
public interface GbRefundReasonService {

    // 查询启用中的退款原因列表(用户退款时"选择退款原因"下拉)
    List<GbRefundReason> getEnabledReasonList();

}
