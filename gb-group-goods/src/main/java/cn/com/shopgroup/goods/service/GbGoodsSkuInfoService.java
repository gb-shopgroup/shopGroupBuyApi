package cn.com.shopgroup.goods.service;

import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.goods.model.GbGoodsSkuInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.executor.BatchResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public interface GbGoodsSkuInfoService {

   GbGoodsSkuInfo getGoodsSkuInfo(Long skuId);


   List<GbGoodsSkuInfo> getMiniGoodsSkuList(Long goodsId);


   GbGoodsSkuInfo getMiniGoodsSkuStockPrice(Long skuId);


   List<GbGoodsSkuInfo> getMiniLeaderGoodsSkuList(Long goodsId);


   Integer saveMiniLeaderGoodsSku(Long leaderId, Long goodsId, List<GbGoodsSkuInfo> lists);


   Boolean reduceGoodsStock(Long skuId, Integer goodsNum);


   Boolean increaseGoodsStock(Long skuId, Integer goodsNum);



}
