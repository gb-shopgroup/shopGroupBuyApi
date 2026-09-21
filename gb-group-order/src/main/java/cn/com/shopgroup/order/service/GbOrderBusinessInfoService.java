package cn.com.shopgroup.order.service;

import cn.com.shopgroup.order.model.GbOrderBusinessInfo;
import java.util.List;

public interface GbOrderBusinessInfoService {

    List<GbOrderBusinessInfo> getAdminOrderBusinessList(int page, int pageSize);

    long getAdminOrderBusinessCount();

    GbOrderBusinessInfo getOrderBusinessInfo(String orderNo);

    List<GbOrderBusinessInfo> getMiniLeaderOrderBusinessList(Long leaderId, Long busId, int page, int pageSize);

    Long getMiniLeaderOrderBusinessCount(int leaderId, int busId);

    Integer addMiniLeaderOrderBusiness(GbOrderBusinessInfo info);

    int editMiniLeaderOrderBusinessRefundStatus(String orderNo);

    // 退款成功后按剩余金额重算分账金额: 仅更新未分账(is_divide=0)订单的分账五字段, 已分账订单不覆写
    int editOrderBusinessDivideFee(GbOrderBusinessInfo info);

    Boolean updateBusinessOrderSendStatus(String orderNo);

    Boolean updateBusinessOrderCheckStatus(String orderNo);

}