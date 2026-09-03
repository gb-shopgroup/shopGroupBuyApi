package cn.com.shopgroup.user.service;

import cn.com.shopgroup.user.model.GbOrgLeaderInfo;

import java.util.List;

public interface GbOrgLeaderInfoService {

    List<GbOrgLeaderInfo> getAdminLeaderList(String mobile, int page, int pageSize);


    Long getAdminLeaderCount();


    Long addAdminLeaderInfo(String name, String mobile, String nickName,
                            String avatar, String openid, byte commission, Integer cashType);


    List<GbOrgLeaderInfo> getAdminLeaderSelectList();


    GbOrgLeaderInfo getLeaderInfo(Long leaderId);


    GbOrgLeaderInfo getMiniLeaderInfo(String openid);


}
