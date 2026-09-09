package cn.com.shopgroup.user.service;

import cn.com.shopgroup.user.model.GbOrgShopInfo;

public interface GbOrgShopInfoService {


    void addAdminLeaderShop(Long leaderId, String shopName);


    GbOrgShopInfo getMiniLeaderShop(Long leaderId);


    Boolean updateMiniLeaderShop(Long leaderId, Long shopId,String name, String mobile, String banner);


    GbOrgShopInfo getInfoByLeaderAndShopId(Long leaderId, Long shopId);

    Boolean updateShopInfo(GbOrgShopInfo shopInfo);

    GbOrgShopInfo getByShopId(Long shopId);

    boolean addShopInfo(GbOrgShopInfo shopInfo);
}
