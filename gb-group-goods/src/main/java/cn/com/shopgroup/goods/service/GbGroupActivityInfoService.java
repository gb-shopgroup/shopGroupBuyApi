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

    /**
     * 查询团购的在线商品列表(is_close=0, 保留团购商品表冗余的团购价/市场价/商品名/主图)。
     * 已关闭(is_close=1)的商品不展示, 用于团购详情/跟团记录/分享海报等展示场景。
     */
    List<GbGroupActivityGoods> getGroupActivityOnlineGoodsList(Long groupId);

    List<GbGroupActivityInfo> getMiniGroupActivityList(Long leaderId, Long catId, int page, int pageSize);

    Long getMiniGroupActivityCount(Long leaderId, Long catId);

    GbGroupActivityInfo getMiniGroupActivityInfo(Long groupId);

    Boolean addGroupOrderNumber(Long groupId);

    /**
     * 团购订单数完全回退(订单已全额退款完成, 不再参与团购跟团Redis+DB 累加):
     * 仅当 gb_group_activity_info.order_total > 0 时才减, 防止数字 / 商品写库异常导致的负数
     */
    Boolean decreaseGroupOrderNumber(Long groupId);

    List<GbGroupActivityInfo> getMiniLeaderGroupList(int flag, Long leaderId, Long catId, String activityName, int status, int page, int pageSize);

    Long getMiniLeaderGroupCount(Long leaderId, Long catId, String activityName, int status);

    Long addMiniLeaderGroupInfo(Long leaderId, GbGroupActivityInfo info);

    Boolean editMiniLeaderGroupInfo(Long leaderId, GbGroupActivityInfo info);

    Boolean closeMiniLeaderGroupInfo(Long leaderId, Long groupId, Integer status, Long staffId,
                                     String staffName, String groupName);

    Boolean isGoodsGrouping(Long leaderId, Long goodsId);

    /**
     * 查询团长 id 下的所有团购活动（不过滤状态/时间, 按添加时间倒序, 主要用于团长端活动下拉选择）。
     *
     * @param leaderId 团长 id, 为空或<=0 时返回空列表
     * @return 该团长下的全部团购活动列表
     */
    List<GbGroupActivityInfo> listAllByLeaderId(Long leaderId);

    List<GbGroupActivityInfo> getMemberGroupActivityList(Long leaderId, Double longitude, Double latitude,
                                                         String groupName,Long catId,int page, int pageSize);

    /**
     * 批量查询多个团购活动的商品列表(价格取团购商品表冗余的团购价, 库存/单位取自商品表), 按团购id分组返回,
     * 一次批量查询完成, 避免团购列表在循环内做 N+1 查询
     *
     * @param groupIds        团购id集合
     * @param onlineGoodsOnly true=仅返回在线商品(is_close=0, C端用户查询活动列表/详情使用);
     *                        false=返回全部状态商品(团长端查询使用)
     * @return key=团购id, value=该团购的商品列表(无商品的团购不会出现在map中)
     */
    Map<Long, List<GroupActGoodsResponse>> getGroupGoodsResponseMap(List<Long> groupIds, boolean onlineGoodsOnly);


    /**
     * 查询商品参与的所有团购活动id(不过滤活动/商品状态), 用于商品上下架后清缓存等场景
     */
    List<Long> getGroupIdsByGoodsId(Long goodsId);
}
