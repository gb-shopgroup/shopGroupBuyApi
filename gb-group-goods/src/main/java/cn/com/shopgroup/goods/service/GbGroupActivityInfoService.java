package cn.com.shopgroup.goods.service;

import cn.com.shopgroup.goods.http.response.group.GroupActGoodsResponse;
import cn.com.shopgroup.goods.model.GbGoodsInfo;
import cn.com.shopgroup.goods.model.GbGroupActivityGoods;
import cn.com.shopgroup.goods.model.GbGroupActivityInfo;

import java.util.List;
import java.util.Map;

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

    List<GbGroupActivityInfo> getMemberGroupActivityList(Long leaderId, Double longitude, Double latitude,
                                                         String groupName,Long catId,int page, int pageSize);

    /**
     * 批量查询多个团购活动的商品列表(价格取团购商品表冗余的团购价, 库存/单位取自商品表), 按团购id分组返回
     * 一次批量查询完成, 避免团购列表在循环内做 N+1 查询
     *
     * @param groupIds 团购id集合
     * @return key=团购id, value=该团购的商品列表(无商品的团购不会出现在map中)
     */
    Map<Long, List<GroupActGoodsResponse>> getGroupGoodsResponseMap(List<Long> groupIds);
}
