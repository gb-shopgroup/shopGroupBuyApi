package cn.com.shopgroup.goods.controller;

import cn.com.shopgroup.common.cache.RedisConstant;
import cn.com.shopgroup.common.cache.RedisHelper;
import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.goods.http.response.group.GroupGoodsResponse;
import cn.com.shopgroup.goods.model.GbGoodsInfo;
import cn.com.shopgroup.goods.model.GbGoodsPackageInfo;
import cn.com.shopgroup.goods.model.GbGoodsSkuInfo;
import cn.com.shopgroup.goods.model.GbGoodsSpecInfo;
import cn.com.shopgroup.goods.service.GbGoodsInfoService;
import cn.com.shopgroup.goods.service.GbGoodsPackageInfoService;
import cn.com.shopgroup.goods.service.GbGoodsSkuInfoService;
import cn.com.shopgroup.goods.service.GbGoodsSpecInfoService;
import cn.com.shopgroup.goods.service.GbGroupActivityInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

//用户端
@RestController
@RequestMapping("/goods")
@Slf4j
public class GroupGoodsController {

    @Resource
    private GbGoodsSkuInfoService skuService;

    @Resource
    private GbGoodsPackageInfoService packageService;

    @Resource
    private GbGoodsSpecInfoService specService;

    @Resource
    private GbGroupActivityInfoService groupService;

    @Resource
    private RedisHelper redisHelper;

    @Resource
    private GbGoodsInfoService goodsInfoService;

    // 团购商品列表(包装, 规格, sku)
    @GetMapping("/group/goods/list")
    public JsonResult goodsList(@RequestParam("lid") Long leaderId, @RequestParam("groupId") Long groupId) {
        log.info("goods/group/goods/lis leaderId:{},groupId:{}",leaderId,groupId);
        // 从缓存里面读取
        String key = RedisConstant.RedisGroupGoodsListKey + groupId;
        if (redisHelper.hasKey(key) == false) {

            // 待返回的数据格式
            List<GroupGoodsResponse> responsesList = new ArrayList<>();

            // 先查询商品列表
            List<GbGoodsInfo> goodsList = groupService.getGroupGoodsList(groupId);

            // 循环查询库存, 包装, 规格, sku
            for (GbGoodsInfo info : goodsList) {

                // 商品Id
                Long goodsId = info.getGoodsId();
                // 再查询包装列表
                List<GbGoodsPackageInfo> packList = packageService.getMiniGoodsPackageList(leaderId, goodsId);
                // 再查询规格列表
                List<GbGoodsSpecInfo> specList = specService.getMiniGoodsSpecList(goodsId);
                // 最后查询sku列表
                List<GbGoodsSkuInfo> skuList = skuService.getMiniGoodsSkuList(goodsId);
                // 合并所有数据到缓存中
                GroupGoodsResponse response = new GroupGoodsResponse(info, packList, specList, skuList);

                // 添加到列表中
                responsesList.add(response);
            }

            // 放到缓存里面
            redisHelper.setCacheObject(key, responsesList, RedisConstant.RedisGroupGoodsListExpired, TimeUnit.SECONDS);
        }

        // 从缓存里面读取
        List<GroupGoodsResponse> responsesList = redisHelper.getCacheObject(key);

        // 返回数据
        return JsonResult.success(responsesList);
    }

    // 查询商品库存, 后期增加缓存
    @GetMapping("/group/goods/stock")
    public JsonResult goodsStock(@RequestParam("gid") String goodsIds) {

        // 商品id列表
        List<Long> goodsIdList = Arrays.stream(goodsIds.split(",")).map(Long::parseLong).collect(Collectors.toList());
        Map<Long, Integer> stock = goodsInfoService.getGoodsStock(goodsIdList);
        return JsonResult.success(stock);
    }

}
