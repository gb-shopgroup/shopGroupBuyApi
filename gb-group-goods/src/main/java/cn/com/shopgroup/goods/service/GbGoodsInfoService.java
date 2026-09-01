package cn.com.shopgroup.goods.service;

import cn.com.shopgroup.goods.model.GbGoodsInfo;

import java.util.List;
import java.util.Map;

public interface GbGoodsInfoService {

    List<GbGoodsInfo> getAdminGoodsList(int page, int pageSize);


    Long getAdminGoodsCount();


    GbGoodsInfo getGoodsInfo(Long goodsId);


    /**
     * 按商品id批量查询商品信息(全字段), 避免循环内 N+1 查询
     */
    List<GbGoodsInfo> getGoodsInfoList(List<Long> goodsIds);


    List<GbGoodsInfo> getGoodsStockList(List<Long> goodsIds);


    List<String> getMiniGoodsImgList(Long goodsId, Integer size);


    Integer getMiniGoodsStock(Long goodsId);


    List<GbGoodsInfo> getMiniLeaderOnlineGoodsList(Long leaderId);


    List<GbGoodsInfo> getMiniLeaderGoodsList(Long leaderId, Long catId, int page, int pageSize);


    Long getMiniLeaderGoodsCount(Long leaderId, Long catId);


    /**
     * 分页查询团长下的所有商品, 支持商品名称关键字模糊搜索
     *
     * @param keyword 商品名称关键字, 可为空
     */
    List<GbGoodsInfo> getMiniLeaderGoodsList(Long leaderId, Long catId, String keyword, int page, int pageSize);


    /**
     * 统计团长下的所有商品数量, 支持商品名称关键字模糊搜索
     *
     * @param keyword 商品名称关键字, 可为空
     */
    Long getMiniLeaderGoodsCount(Long leaderId, Long catId, String keyword);


    Long addMiniLeaderGoodsInfo(Long leaderId, GbGoodsInfo info, List<String> imgList);


    Boolean editMiniLeaderGoodsInfo(Long leaderId, GbGoodsInfo info, List<String> imgList);


    Boolean closeMiniLeaderGoodsInfo(Long leaderId, Long goodsId, Integer status);


    Boolean reduceGoodsStock(Long goodsId, Integer goodsNum);


    Boolean increaseGoodsStock(Long goodsId, int goodsNum);


    Map<Long, Integer> getGoodsStock(List<Long> goodsIdList);
}
