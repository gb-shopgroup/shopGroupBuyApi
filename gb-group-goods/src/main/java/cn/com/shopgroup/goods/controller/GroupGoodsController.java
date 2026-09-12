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
        log.info("goods/group/goods/list leaderId:{},groupId:{}", leaderId, groupId);
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

//    // 用户查询所有在线的团购活动列表[新用户未绑定团长时leaderId=0]: 仅返回未下线(isClose=0)且当前时间处于开团时间窗内(已开团未结束)的在线活动; leaderId>0按团长过滤(排序值sortOrder升序置顶优先, 同级按活动id倒序, 分页在SQL层完成); leaderId=0时需传经纬度(longitude/latitude, 缺失返回空列表), 仅统计已绑定自提点(pointId>0)的活动并过滤出绑定自提点与定位点球面距离小于20km者, 按活动id倒序(先距离过滤再分页); page默认1, pageSize默认10最大20
//    @PostMapping("/member/groupActivity/list")
//    public JsonResult getGroupActiveList(@Validated @RequestBody MemberGroupListRequest request) {
//        log.info("用户-查询所有在线的团购活动列表,request:{}", JSON.toJSONString(request));
//        // 从请求获取团长id
//        Long leaderId = request.getLeaderId();
//        Double longitude = request.getLongitude();
//        Double latitude = request.getLatitude();
//        // 请求参数矫正
//        int page = Optional.ofNullable(request.getPage()).orElse(1);
//        int pageSize = Optional.ofNullable(request.getPageSize())
//                .map(size -> Math.min(size, 20))
//                .orElse(10);
//        List<GbGroupActivityInfo> result = groupService.getMemberGroupActivityList(leaderId, longitude, latitude, page, pageSize);
//        List<GroupActResponse> data = GroupActResponse.getGroupResponseList(result);
//        return JsonResult.success(data);
//    }

}
