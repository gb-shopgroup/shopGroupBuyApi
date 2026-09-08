package cn.com.shopgroup.goods.service;

import cn.com.shopgroup.goods.model.GbGoodsInfo;
import cn.com.shopgroup.goods.model.GbGroupActivityGoods;
import cn.com.shopgroup.goods.model.GbGroupActivityInfo;

import java.util.List;

public interface GbGroupActivityInfoService {

    List<GbGroupActivityInfo> getAdminGroupList(int page, int pageSize);

    Long getAdminGroupCount();

    GbGroupActivityInfo getGroupInfo(Long groupId);

    List<GbGoodsInfo> getGroupGoodsList(Long groupId);

    List<GbGroupActivityGoods> getGroupActivityGoodsList(Long groupId);

    List<GbGroupActivityInfo> getMiniGroupActivityList(Long leaderId, Long catId, int page, int pageSize);

    Long getMiniGroupActivityCount(Long leaderId, Long catId);

    GbGroupActivityInfo getMiniGroupActivityInfo(Long groupId);

    Boolean addGroupOrderNumber(Long groupId);

    List<GbGroupActivityInfo> getMiniLeaderGroupList(int flag, Long leaderId, Long catId, String activityName, int status, int page, int pageSize);

    Long getMiniLeaderGroupCount(Long leaderId, Long catId, String activityName, int status);

    Long addMiniLeaderGroupInfo(Long leaderId, GbGroupActivityInfo info);

    Boolean editMiniLeaderGroupInfo(Long leaderId, GbGroupActivityInfo info);

    Boolean closeMiniLeaderGroupInfo(Long leaderId, Long groupId, Integer status, Long staffId,
                                     String staffName, String groupName);

    Boolean isGoodsGrouping(Long leaderId, Long goodsId);

    List<GbGroupActivityInfo> getMemberGroupActivityList(Long leaderId, Double longitude, Double latitude, int page, int pageSize);
}
