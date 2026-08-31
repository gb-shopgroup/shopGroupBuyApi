package cn.com.shopgroup.user.service;

import cn.com.shopgroup.user.model.GbOrgBusinessInfo;

import java.util.List;

public interface GbOrgBusinessInfoService {

   Long addAdminBusinessInfo(Long leaderId, String busName, String code);


   GbOrgBusinessInfo getAdminBusinessInfoByCode(String code);


   List<GbOrgBusinessInfo> getAdminBusinessList(Long leaderId, int page, int pageSize);


   Long getAdminBusinessCount();


   GbOrgBusinessInfo getBusinessInfo(Long busId);


   List<GbOrgBusinessInfo> getMiniBusinessList(Long leaderId);


   List<GbOrgBusinessInfo> getMiniLeaderBusinessList(Long leaderId);


   Boolean isMiniLeaderAccountExist(String account);


   Long addMiniLeaderBusiness(Long leaderId, GbOrgBusinessInfo info);


   Boolean editMiniLeaderBusiness(Long leaderId, GbOrgBusinessInfo data);


   Boolean closeMiniLeaderBusiness(Long leaderId, Long busId, Integer status);


   Boolean increaseMiniLeaderBusinessBalance(Long leaderId, Long busId, Integer fee);


   Boolean reduceMiniLeaderBusinessBalance(Long leaderId, Long busId, Integer fee);


}
