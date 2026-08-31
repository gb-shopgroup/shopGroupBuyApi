package cn.com.shopgroup.goods.service;

import cn.com.shopgroup.goods.model.GbGoodsPackageInfo;

import java.util.List;

public interface GbGoodsPackageInfoService {

    GbGoodsPackageInfo getGoodsPackageInfo(Long packId);


    List<GbGoodsPackageInfo> getMiniGoodsPackageList(Long leaderId, Long goodsId);


    List<GbGoodsPackageInfo> getMiniLeaderGoodsPackageList(Long leaderId, Long goodsId);


    void updateMiniLeaderGoodsPackageList(Long leaderId, List<GbGoodsPackageInfo> packageList);


    Long addMiniLeaderGoodsPackageList(Long leaderId, GbGoodsPackageInfo info);


    Boolean editMiniLeaderGoodsPackageList(Long leaderId, GbGoodsPackageInfo info);


    Boolean closeMiniLeaderGoodsPackageList(int leaderId, int packId, int status);


    Boolean removeMiniLeaderGoodsPackageList(Long leaderId, Long packId);


}
