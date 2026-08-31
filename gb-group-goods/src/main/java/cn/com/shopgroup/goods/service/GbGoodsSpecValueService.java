package cn.com.shopgroup.goods.service;

import cn.com.shopgroup.goods.model.GbGoodsSpecValue;

import java.util.List;

/**
 * @version: 1.0.0
 */
public interface GbGoodsSpecValueService {
    Long addGoodsSpecVal(GbGoodsSpecValue specValue);

    int editGoodsSpecVal(GbGoodsSpecValue data);

    int removeMiniLeaderGoodsSpecVal(Long valId);

    Boolean removeMiniLeaderGoodsSpecValList(Long specId);

    List<GbGoodsSpecValue> getMiniLeaderGoodsSpecValList(Long specId);

    GbGoodsSpecValue getGoodsSpecValInfo(Long valId);

    int updateGoodsIdByIds(Long leaderId, Long goodsId, List<Long> specInfoIds);
}
