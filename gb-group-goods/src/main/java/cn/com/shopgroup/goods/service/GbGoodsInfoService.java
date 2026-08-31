package cn.com.shopgroup.goods.service;

import cn.com.shopgroup.goods.model.GbGoodsInfo;

import java.util.List;
import java.util.Map;

public interface GbGoodsInfoService {

    List<GbGoodsInfo> getAdminGoodsList(int page, int pageSize);


    Long getAdminGoodsCount();


    GbGoodsInfo getGoodsInfo(Long goodsId);


    List<GbGoodsInfo> getGoodsStockList(List<Long> goodsIds);


    List<String> getMiniGoodsImgList(Long goodsId, Integer size);


    Integer getMiniGoodsStock(Long goodsId);


    List<GbGoodsInfo> getMiniLeaderOnlineGoodsList(Long leaderId);


    List<GbGoodsInfo> getMiniLeaderGoodsList(Long leaderId, Long catId, int page, int pageSize);


    Long getMiniLeaderGoodsCount(Long leaderId, Long catId);


    Long addMiniLeaderGoodsInfo(Long leaderId, GbGoodsInfo info, List<String> imgList);


    Boolean editMiniLeaderGoodsInfo(Long leaderId, GbGoodsInfo info, List<String> imgList);


    Boolean closeMiniLeaderGoodsInfo(Long leaderId, Long goodsId, Integer status);


    Boolean reduceGoodsStock(Long goodsId, Integer goodsNum);


    Boolean increaseGoodsStock(Long goodsId, int goodsNum);


    Map<Long, Integer> getGoodsStock(List<Long> goodsIdList);
}
