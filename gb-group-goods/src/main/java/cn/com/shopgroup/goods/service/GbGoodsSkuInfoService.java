package cn.com.shopgroup.goods.service;

import cn.com.shopgroup.goods.model.GbGoodsSkuInfo;

import java.util.List;

public interface GbGoodsSkuInfoService {

   GbGoodsSkuInfo getGoodsSkuInfo(Long skuId);


   // 批量查询SKU信息(按skuId集合), 用于订单商品规格名称回填等场景
   List<GbGoodsSkuInfo> getGoodsSkuInfoList(List<Long> skuIds);


   List<GbGoodsSkuInfo> getMiniGoodsSkuList(Long goodsId);


   GbGoodsSkuInfo getMiniGoodsSkuStockPrice(Long skuId);


   List<GbGoodsSkuInfo> getMiniLeaderGoodsSkuList(Long goodsId);


   Integer saveMiniLeaderGoodsSku(Long leaderId, Long goodsId, List<GbGoodsSkuInfo> lists);


   Boolean reduceGoodsStock(Long skuId, Integer goodsNum);


   Boolean increaseGoodsStock(Long skuId, Integer goodsNum);


   // 直接覆盖SKU库存(绝对值, 非增减): 团长端修改库存使用, num<0 视为非法
   Boolean updateSkuGoodsNum(Long skuId, Integer goodsNum);



}
