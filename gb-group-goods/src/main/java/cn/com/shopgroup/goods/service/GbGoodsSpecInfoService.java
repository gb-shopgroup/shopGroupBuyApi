package cn.com.shopgroup.goods.service;

import cn.com.shopgroup.goods.model.GbGoodsSpecInfo;
import cn.com.shopgroup.goods.model.GbGoodsSpecValue;

import java.util.List;
import java.util.Map;

public interface GbGoodsSpecInfoService {

    GbGoodsSpecInfo getGoodsSpecInfo(Long specId);

    List<GbGoodsSpecInfo> getMiniGoodsSpecList(Long goodsId);

    List<GbGoodsSpecInfo> getMiniLeaderGoodsSpecList(Long goodsId);

    /**
     * 查询单个商品关联的规格(包含规格值), 通过规格值表 goods_id 关联
     */
    List<GbGoodsSpecInfo> getGoodsSpecListByGoodsId(Long goodsId);

    /**
     * 批量查询多个商品关联的规格(包含规格值), 返回 key=商品id, value=该商品的规格列表
     */
    Map<Long, List<GbGoodsSpecInfo>> getGoodsSpecListByGoodsIds(List<Long> goodsIds);

    Long addMiniLeaderGoodsSpec(GbGoodsSpecInfo info);

    Boolean editMiniLeaderGoodsSpec(GbGoodsSpecInfo info);

    Boolean closeMiniLeaderGoodsSpec(Long specId, int status);

    Boolean removeMiniLeaderGoodsSpec(Long specId);

    Boolean updateMiniLeaderGoodsSpecVal(Long leaderId, Long goodsId, List<GbGoodsSpecInfo> lists);

    List<GbGoodsSpecInfo> getLeaderGoodsSpecList(Long leaderId);

    int updateGoodsIdByIds(Long leaderId, Long goodsId, List<Long> specInfoIds);
}
