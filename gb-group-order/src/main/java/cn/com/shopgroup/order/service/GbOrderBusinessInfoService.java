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

    Boolean updateBusinessOrderSendStatus(String orderNo);

    Boolean updateBusinessOrderCheckStatus(String orderNo);

}