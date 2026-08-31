package cn.com.shopgroup.user.service;

import cn.com.shopgroup.user.model.GbOrgCashInfo;

import java.util.List;

public interface GbOrgCashInfoService {

   List<GbOrgCashInfo> getMiniLeaderCashList(Long leaderId, int page, int pageSize) ;


   Long getMiniLeaderCashCount(Long leaderId) ;


   Long addMiniLeaderCash(Long leaderId, GbOrgCashInfo info) ;


   Boolean editMiniLeaderCash(Long leaderId, Long cashId, String cashNo) ;


}
