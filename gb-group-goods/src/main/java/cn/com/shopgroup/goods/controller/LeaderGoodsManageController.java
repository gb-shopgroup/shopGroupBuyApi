package cn.com.shopgroup.goods.controller;

import cn.com.shopgroup.common.exception.BusinessException;
import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.goods.exception.GoodsErrorCodeEnum;
import cn.com.shopgroup.goods.http.request.leader.LeaderAddGoodsRequest;
import cn.com.shopgroup.goods.http.request.leader.LeaderAddSpecRequest;
import cn.com.shopgroup.goods.http.request.leader.LeaderAddSpecValRequest;
import cn.com.shopgroup.goods.http.request.leader.LeaderGoodsRequest;
import cn.com.shopgroup.goods.http.request.leader.LeaderSkuRequest;
import cn.com.shopgroup.goods.http.response.GoodsCategoryResponse;
import cn.com.shopgroup.goods.http.response.leader.GoodsDetailResponse;
import cn.com.shopgroup.goods.http.response.leader.LeaderGoodsResponse;
import cn.com.shopgroup.goods.http.response.leader.LeaderSkuResponse;
import cn.com.shopgroup.goods.http.response.leader.LeaderSpecResponse;
import cn.com.shopgroup.goods.model.GbGoodsCategoryInfo;
import cn.com.shopgroup.goods.model.GbGoodsInfo;
import cn.com.shopgroup.goods.model.GbGoodsSkuInfo;
import cn.com.shopgroup.goods.model.GbGoodsSpecInfo;
import cn.com.shopgroup.goods.model.GbGoodsSpecValue;
import cn.com.shopgroup.goods.service.GbGoodsCategoryInfoService;
import cn.com.shopgroup.goods.service.GbGoodsInfoService;
import cn.com.shopgroup.goods.service.GbGoodsSkuInfoService;
import cn.com.shopgroup.goods.service.GbGoodsSpecInfoService;
import cn.com.shopgroup.goods.service.GbGoodsSpecValueService;
import cn.com.shopgroup.goods.service.GbGroupActivityInfoService;
import cn.com.shopgroup.goods.utils.SkuGenerateUtils;
import cn.com.shopgroup.user.utils.RequestParamsUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

//团长端-商品管理
@RestController
@RequestMapping("/goods")
@Slf4j
public class LeaderGoodsManageController {

    @Resource
    private GbGoodsInfoService goodsService;

    @Resource
    private GbGoodsSpecInfoService specService;

    @Resource
    private GbGoodsSpecValueService specValueService;

    @Resource
    private GbGoodsSkuInfoService skuService;

    @Resource
    private GbGoodsCategoryInfoService categoryService;

    @Resource
    private GbGroupActivityInfoService groupActivityInfoService;

    /******************************************************************************************/

    // 商品分类列表
    @GetMapping("/get/goods/cat")
    public JsonResult goodsCat() {
        List<GbGoodsCategoryInfo> result = categoryService.getGoodsCategoryList();
        if (CollectionUtils.isEmpty(result)) {
            return JsonResult.success();
        }
        return JsonResult.success(GoodsCategoryResponse.getGoodsCategoryResponseList(result));
    }

    // 查询所有审核通过且未关闭的商品, 添加团购时使用
    @GetMapping("/leader/goods/online")
    public JsonResult online() {
        Long leaderId = getLeaderId();
        List<GbGoodsInfo> result = goodsService.getMiniLeaderOnlineGoodsList(leaderId);
        List<LeaderGoodsResponse> data = LeaderGoodsResponse.getGoodsResponseList(result);
        // 商品规格信息
        fillGoodsSpecList(data);
        // 分类名称
        fillGoodsCategoryName(data);
        return JsonResult.success(data);
    }

    // 分页查询团长下的所有商品(团长控制台-商品管理), 支持分类+商品名称关键字筛选
    @GetMapping("/leader/goods/list")
    public JsonResult goodsList(@RequestParam("cat") Long catId,
                                @RequestParam(value = "keyword", required = false) String keyword,
                                @RequestParam("page") int page, @RequestParam("pageSize") int pageSize) {
        log.info("goods/leader/goods/list catId:{},keyword:{}",catId,keyword);
        Long leaderId = getLeaderId();
        // 请求参数矫正
        if (page == 0) page = 1;
        if (pageSize == 0) pageSize = 10;
        if (pageSize > 20) pageSize = 20;

        // 查询数据库
        List<GbGoodsInfo> result = goodsService.getMiniLeaderGoodsList(leaderId, catId, keyword, page, pageSize);
        List<LeaderGoodsResponse> data = LeaderGoodsResponse.getGoodsResponseList(result);
        // 商品规格信息
        fillGoodsSpecList(data);
        // 分类名称
        fillGoodsCategoryName(data);
        return JsonResult.success(data);
    }

    // 查询团长下的所有商品数量, 支持分类+商品名称关键字筛选
    @GetMapping("/leader/goods/count")
    public JsonResult goodsCount(@RequestParam("cat") Long catId,
                                 @RequestParam(value = "keyword", required = false) String keyword) {
        Long leaderId = getLeaderId();
        Long total = goodsService.getMiniLeaderGoodsCount(leaderId, catId, keyword);
        return JsonResult.success(total);
    }

    // 商品信息查询: 商品基本信息 + 分类名 + 图片 + 规格(含规格值) + SKU
    @GetMapping("/leader/goods/info")
    public JsonResult goodsInfo(@RequestParam("id") Long goodsId) {
        log.info("查询商品:/goods/leader/goods/info, goodsId:{}", goodsId);
        // 查询商品
        GbGoodsInfo goodsInfo = goodsService.getGoodsInfo(goodsId);
        if (ObjectUtils.isEmpty(goodsInfo)) {
            return JsonResult.success("商品id=" + goodsId + "未查询到商品");
        }

        GoodsDetailResponse detail = new GoodsDetailResponse();
        LeaderGoodsResponse response = new LeaderGoodsResponse(goodsInfo);

        // 分类名称
        response.setCatName(getCategoryName(goodsInfo.getCatId()));

        // 商品图片
        List<String> imgs = goodsService.getMiniGoodsImgList(goodsId, 3);
        if (imgs != null && imgs.size() > 0) {
            response.setImg(imgs.get(0));
            if (imgs.size() > 1) response.setImg2(imgs.get(1));
            if (imgs.size() > 2) response.setImg3(imgs.get(2));
        }

        // 商品规格信息(包含规格值)
        List<GbGoodsSpecInfo> specList = specService.getGoodsSpecListByGoodsId(goodsId);
        response.setSpecList(LeaderSpecResponse.getSpecResponseList(specList));

        // 商品SKU信息
        List<GbGoodsSkuInfo> skuList = skuService.getMiniGoodsSkuList(goodsId);
        detail.setGoods(response);
        detail.setSkuList(LeaderSkuResponse.getSkuResponseList(skuList));

        return JsonResult.success(detail);
    }

    // 添加商品
    @PostMapping("/leader/goods/addGoods")
    public JsonResult addGoods(@Validated @RequestBody LeaderAddGoodsRequest request) {
        log.info("添加商品:/goods/leader/goods/addGoods request:{}", request);
        Long leaderId = getLeaderId();

        // 1. 商品基本信息 -> gb_goods_info
        GbGoodsInfo data = new GbGoodsInfo();
        data.setCatId(request.getCatId());
        data.setGoodsType(request.getType());
        data.setGoodsName(request.getName());
        // 价格
        data.setCostPrice(Optional.ofNullable(request.getCostPrice()).orElse(0D));
        data.setSalesPrice(Optional.ofNullable(request.getSalePrice()).orElse(0D));
        data.setMarketPrice(Optional.ofNullable(request.getMarketPrice()).orElse(0D));
        // 库存: 启用库存时使用提交库存, 否则默认 10000(产品要求)
        data.setIsStock(request.getIsStock());
        int stockNum = 10000;
        if (request.getIsStock() != null && request.getIsStock() == 1
                && request.getStockNum() != null && request.getStockNum() > 0) {
            stockNum = request.getStockNum();
        }
        data.setGoodsNum(stockNum);
        // 限购
        data.setIsLimit(request.getIsLimit());
        data.setLimitNum(request.getLimitNum());
        data.setGoodsUnit(request.getUnit());
        // 商品介绍
        data.setGoodsInfo(request.getGoodsInfo());

        // 2. 商品图片 -> gb_goods_image_info(过滤空图片, 主图必填)
        List<String> imgList = new ArrayList<>();
        if (request.getImg() != null && request.getImg().trim().length() > 0) imgList.add(request.getImg());
        if (request.getImg2() != null && request.getImg2().trim().length() > 0) imgList.add(request.getImg2());
        if (request.getImg3() != null && request.getImg3().trim().length() > 0) imgList.add(request.getImg3());

        // 3. 写入商品表
        Long goodsId = goodsService.addMiniLeaderGoodsInfo(leaderId, data, imgList);

        // 4. 商品规格 + 规格值 -> gb_goods_spec_info / gb_goods_spec_value
        saveGoodsSpec(leaderId, goodsId, request.getAddSpecList());

        // 5. 商品SKU -> gb_goods_sku_info(随商品保存一并写入; 未提交SKU时按规格自动生成)
        saveGoodsSku(leaderId, goodsId, request.getSkuList(), true);

        // 返回
        return JsonResult.success("添加成功", goodsId);
    }

    // 修改商品, 如果该商品正在团购中, 则不允许修改
    @PostMapping("/leader/goods/edit")
    public JsonResult editGoods(@Validated @RequestBody LeaderGoodsRequest request) {
        log.info("修改商品:/goods/leader/goods/edit request:{}", request);
        Long leaderId = getLeaderId();

        // 如果该商品正在团购中, 则不允许修改
        if (groupActivityInfoService.isGoodsGrouping(leaderId, request.getId())) {
            throw new BusinessException(GoodsErrorCodeEnum.GOODS_GROUPING);
        }

        // 1. 商品基本信息 -> gb_goods_info
        GbGoodsInfo data = new GbGoodsInfo();
        data.setGoodsId(request.getId());
        data.setCatId(request.getCatId());
        data.setGoodsType(request.getType());
        data.setGoodsName(request.getName());
        data.setCostPrice(Optional.ofNullable(request.getCostPrice()).orElse(0D));
        data.setSalesPrice(Optional.ofNullable(request.getPrice()).orElse(0D));
        data.setMarketPrice(Optional.ofNullable(request.getPrice2()).orElse(0D));
        data.setIsStock(request.getIsStock());
        data.setGoodsNum(request.getStockNum());
        // 限购
        data.setIsLimit(request.getIsLimit());
        data.setLimitNum(request.getLimitNum());
        data.setGoodsUnit(request.getUnit());
        // 商品介绍
        data.setGoodsInfo(request.getGoodsInfo());

        // 2. 商品图片 -> gb_goods_image_info(过滤空图片, 主图必填)
        List<String> imgList = new ArrayList<>();
        if (request.getImg() != null && request.getImg().trim().length() > 0) imgList.add(request.getImg());
        if (request.getImg2() != null && request.getImg2().trim().length() > 0) imgList.add(request.getImg2());
        if (request.getImg3() != null && request.getImg3().trim().length() > 0) imgList.add(request.getImg3());

        // 3. 修改商品表
        boolean flag = goodsService.editMiniLeaderGoodsInfo(leaderId, data, imgList);
        if (!flag) {
            throw new BusinessException(GoodsErrorCodeEnum.UPDATE_FAILED);
        }

        // 4. 商品规格重建 -> gb_goods_spec_info / gb_goods_spec_value
        // 提交了规格则先删除该商品原有规格再按提交内容重建, 未提交则保留原有规格
        rebuildGoodsSpec(leaderId, request.getId(), request.getAddSpecList());

        // 5. 商品SKU重建 -> gb_goods_sku_info
        // - 提交了SKU: 先删除该商品原有SKU再按提交内容重建
        // - 未提交SKU但修改了规格: 按新规格自动生成
        // - 未提交SKU也未修改规格: 保留原有SKU(不覆盖已有的SKU价格/库存)
        if (!CollectionUtils.isEmpty(request.getSkuList()) || !CollectionUtils.isEmpty(request.getAddSpecList())) {
            saveGoodsSku(leaderId, request.getId(), request.getSkuList(), CollectionUtils.isEmpty(request.getSkuList()));
        }

        return JsonResult.success();
    }

    // 关闭商品(上下架), 如果该商品正在团购中, 则不允许操作
    @GetMapping("/leader/goods/close")
    public JsonResult closeGoods(@RequestParam("id") Long goodsId) {
        Long leaderId = getLeaderId();

        // 查询旧状态
        GbGoodsInfo goodsInfo = goodsService.getGoodsInfo(goodsId);
        if (ObjectUtils.isEmpty(goodsInfo)) {
            throw new BusinessException(GoodsErrorCodeEnum.GOODS_NOT_EXIST);
        }
        // 如果该商品正在团购中, 则不允许操作
        if (groupActivityInfoService.isGoodsGrouping(leaderId, goodsId)) {
            throw new BusinessException(GoodsErrorCodeEnum.GOODS_GROUPING);
        }
        // 切换上下架状态
        int status = goodsInfo.getIsClose() == 0 ? 1 : 0;
        goodsService.closeMiniLeaderGoodsInfo(leaderId, goodsId, status);
        return JsonResult.success();
    }

    /******************************************************************************************/

    // 根据规格罗列所有SKU, 包括已经存在的sku信息
    @GetMapping("/leader/goods/sku/spec")
    public JsonResult getGoodsSkuSpec(@RequestParam("id") Long goodsId) {
        Long leaderId = getLeaderId();

        // 查询所有规格和规格值
        List<GbGoodsSpecInfo> specInfoList = specService.getMiniGoodsSpecList(goodsId);

        // 查询已经存在的sku信息, 转map, key=skuIds
        List<GbGoodsSkuInfo> skuList = skuService.getMiniGoodsSkuList(goodsId);
        Map<String, GbGoodsSkuInfo> skuMap = new HashMap<>();
        for (GbGoodsSkuInfo item : skuList) {
            skuMap.put(item.getSkuIds(), item);
        }

        // 将规格值组合生成SKU列表(笛卡尔积), 合并已存在SKU信息
        List<GbGoodsSkuInfo> result = SkuGenerateUtils.generateSkuList(specInfoList);
        List<LeaderSkuResponse> data = new ArrayList<>();
        for (GbGoodsSkuInfo item : result) {
            LeaderSkuResponse temp = new LeaderSkuResponse();
            temp.setIds(item.getSkuIds());
            temp.setGid(goodsId);
            temp.setNames(item.getSkuNames());
            GbGoodsSkuInfo exist = skuMap.get(item.getSkuIds());
            if (exist != null) {
                temp.setId(exist.getSkuId());
                temp.setPrice(exist.getSalesPrice());
                temp.setPrice2(exist.getMarketPrice());
                temp.setNum(exist.getGoodsNum());
                temp.setImg(exist.getGoodsImg());
            } else {
                temp.setId(0L);
                temp.setPrice(0D);
                temp.setPrice2(0D);
                temp.setNum(0);
                temp.setImg("");
            }
            data.add(temp);
        }

        return JsonResult.success(data);
    }

    // 批量保存商品SKU(先删除后添加), 如果该商品正在团购中, 则不允许修改
    // 注: 前端不再单独调用此接口, SKU已随添加/修改商品接口(addGoods/edit)一并处理, 此处保留兼容
    @PostMapping("/leader/goods/sku/save")
    public JsonResult saveGoodsSku(@Validated @RequestBody List<LeaderSkuRequest> requestList) {
        Long leaderId = getLeaderId();

        // 商品不存在
        if (requestList == null || requestList.isEmpty()) {
            throw new BusinessException(GoodsErrorCodeEnum.SKU_REQUIRED);
        }
        Long goodsId = requestList.get(0).getGid();
        if (goodsId == null || goodsId == 0) {
            throw new BusinessException(GoodsErrorCodeEnum.GOODS_NOT_EXIST);
        }

        // 如果该商品正在团购中, 则不允许修改
        if (groupActivityInfoService.isGoodsGrouping(leaderId, goodsId)) {
            throw new BusinessException(GoodsErrorCodeEnum.GOODS_GROUPING);
        }

        // 先删除后添加(未提交SKU时自动生成, 保持原行为)
        saveGoodsSku(leaderId, goodsId, requestList, true);
        return JsonResult.success();
    }

    /******************************************************************************************/

    /**
     * 批量保存商品SKU(先删除后添加)
     * <p>
     * 1. 前端提交了skuList -> 按提交内容写入gb_goods_sku_info
     * 2. 前端未提交skuList 且 autoGenerateWhenEmpty=true -> 后台根据商品规格自动生成SKU(笛卡尔积),
     * 价格/库存/单位/图片取商品表默认值; 无规格商品生成1条默认SKU
     * 3. 前端未提交skuList 且 autoGenerateWhenEmpty=false -> 保留原有SKU, 不覆盖已有的SKU价格/库存
     */
    private void saveGoodsSku(Long leaderId, Long goodsId, List<LeaderSkuRequest> requestList, boolean autoGenerateWhenEmpty) {
        if (goodsId == null || goodsId == 0) {
            return;
        }

        // 商品基础信息(用于SKU默认价格/库存/单位/图片)
        GbGoodsInfo goodsInfo = goodsService.getGoodsInfo(goodsId);
        Double costPrice = goodsInfo == null ? 0D : goodsInfo.getCostPrice();
        Double salesPrice = goodsInfo == null ? 0D : goodsInfo.getSalesPrice();
        Double marketPrice = goodsInfo == null ? 0D : goodsInfo.getMarketPrice();
        Integer goodsNum = goodsInfo == null ? 0 : goodsInfo.getGoodsNum();
        String goodsUnit = goodsInfo == null ? "" : goodsInfo.getGoodsUnit();
        String goodsImg = goodsInfo == null ? "" : goodsInfo.getGoodsImg();

        List<GbGoodsSkuInfo> dataList = new ArrayList<>();
        int nowTime = TimeUtils.getTimeStamp();

        // 1. 前端提交了SKU -> 按提交内容入库
        if (!CollectionUtils.isEmpty(requestList)) {
            for (LeaderSkuRequest request : requestList) {
                GbGoodsSkuInfo temp = new GbGoodsSkuInfo();
                temp.setSkuId(request.getId());
                temp.setGoodsId(goodsId);
                temp.setLeaderId(leaderId);
                temp.setSkuIds(request.getIds());
                temp.setSkuNames(request.getNames());
                temp.setCostPrice(costPrice);
                temp.setSalesPrice(request.getPrice());
                temp.setMarketPrice(request.getPrice2());
                temp.setGoodsNum(request.getNum());
                temp.setGoodsImg(request.getImg());
                temp.setGoodsUnit(goodsUnit);
                temp.setIsClose((byte) 0);
                temp.setAddTime(nowTime);
                dataList.add(temp);
            }
        } else if (autoGenerateWhenEmpty) {
            // 2. 前端未提交SKU 且允许自动生成 -> 根据商品规格自动生成(笛卡尔积)
            List<GbGoodsSpecInfo> specList = specService.getMiniGoodsSpecList(goodsId);
            List<GbGoodsSkuInfo> generateList = SkuGenerateUtils.generateSkuList(specList);
            if (CollectionUtils.isEmpty(generateList)) {
                // 无规格商品: 生成1条默认SKU
                GbGoodsSkuInfo defaultSku = new GbGoodsSkuInfo();
                defaultSku.setSkuIds("");
                defaultSku.setSkuNames(goodsInfo == null ? "" : goodsInfo.getGoodsName());
                generateList.add(defaultSku);
            }
            for (GbGoodsSkuInfo item : generateList) {
                GbGoodsSkuInfo temp = new GbGoodsSkuInfo();
                temp.setGoodsId(goodsId);
                temp.setLeaderId(leaderId);
                temp.setSkuIds(item.getSkuIds());
                temp.setSkuNames(item.getSkuNames());
                temp.setCostPrice(costPrice);
                temp.setSalesPrice(salesPrice);
                temp.setMarketPrice(marketPrice);
                temp.setGoodsNum(goodsNum);
                temp.setGoodsImg(goodsImg);
                temp.setGoodsUnit(goodsUnit);
                temp.setIsClose((byte) 0);
                temp.setAddTime(nowTime);
                dataList.add(temp);
            }
        } else {
            // 3. 前端未提交SKU 且不允许自动生成 -> 保留原有SKU
            return;
        }

        // 先删除后添加
        skuService.saveMiniLeaderGoodsSku(leaderId, goodsId, dataList);
    }

    /**
     * 从请求头中获取团长id
     */
    private Long getLeaderId() {
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            throw new BusinessException(GoodsErrorCodeEnum.LEADER_NOT_EXIST);
        }
        return leaderId;
    }

    /**
     * 保存商品规格和规格值(添加商品时使用)
     * <p>
     * 支持两种模式:
     * - 规格携带 specId: 关联已有的规格模板(goods_id 更新到规格/规格值表)
     * - 规格未携带 specId: 为商品新建专属规格和规格值(goods_id 直接写入)
     */
    private void saveGoodsSpec(Long leaderId, Long goodsId, List<LeaderAddSpecRequest> specList) {
        if (CollectionUtils.isEmpty(specList)) {
            return;
        }
        int nowTime = TimeUtils.getTimeStamp();
        // 需要关联已有规格模板的id集合
        List<Long> existSpecIds = new ArrayList<>();
        List<Long> existValIds = new ArrayList<>();

        for (LeaderAddSpecRequest spec : specList) {
            List<LeaderAddSpecValRequest> valList = spec.getSpecValLists();

            // 1. 关联已有的规格模板
            if (spec.getSpecId() != null && spec.getSpecId() > 0) {
                existSpecIds.add(spec.getSpecId());
                if (!CollectionUtils.isEmpty(valList)) {
                    for (LeaderAddSpecValRequest val : valList) {
                        if (val.getValId() != null && val.getValId() > 0) {
                            existValIds.add(val.getValId());
                        }
                    }
                }
                continue;
            }

            // 2. 新建商品专属规格 -> gb_goods_spec_info
            GbGoodsSpecInfo specInfo = new GbGoodsSpecInfo();
            specInfo.setGoodsId(goodsId);
            specInfo.setLeaderId(leaderId);
            specInfo.setSpecName(spec.getName());
            specInfo.setIsPrice(spec.getPrice() == null ? (byte) 0 : spec.getPrice());
            specInfo.setIsStock(spec.getStock() == null ? (byte) 0 : spec.getStock());
            specInfo.setSortOrder(255);
            specInfo.setIsClose((byte) 0);
            specInfo.setAddTime(nowTime);
            Long specId = specService.addMiniLeaderGoodsSpec(specInfo);

            // 3. 新建规格值 -> gb_goods_spec_value
            if (!CollectionUtils.isEmpty(valList)) {
                for (LeaderAddSpecValRequest val : valList) {
                    GbGoodsSpecValue specVal = new GbGoodsSpecValue();
                    specVal.setGoodsId(goodsId);
                    specVal.setLeaderId(leaderId);
                    specVal.setSpecId(specId);
                    specVal.setSpecVal(val.getVal());
                    specVal.setIsClose((byte) 0);
                    specVal.setAddTime(nowTime);
                    specValueService.addGoodsSpecVal(specVal);
                }
            }
        }

        // 4. 关联已有规格模板到当前商品
        if (!existSpecIds.isEmpty()) {
            specService.updateGoodsIdByIds(leaderId, goodsId, existSpecIds);
        }
        if (!existValIds.isEmpty()) {
            specValueService.updateGoodsIdByIds(leaderId, goodsId, existValIds);
        }
    }

    /**
     * 重建商品规格和规格值(修改商品时使用)
     * <p>
     * 提交了规格则先删除该商品原有的规格和规格值, 再按提交内容重建;
     * 未提交规格则保持原有规格不变。
     */
    private void rebuildGoodsSpec(Long leaderId, Long goodsId, List<LeaderAddSpecRequest> specList) {
        if (CollectionUtils.isEmpty(specList)) {
            return;
        }
        // 1. 删除该商品原有的规格和规格值
        List<GbGoodsSpecInfo> oldSpecList = specService.getMiniGoodsSpecList(goodsId);
        if (!CollectionUtils.isEmpty(oldSpecList)) {
            for (GbGoodsSpecInfo oldSpec : oldSpecList) {
                specValueService.removeMiniLeaderGoodsSpecValList(oldSpec.getSpecId());
                specService.removeMiniLeaderGoodsSpec(oldSpec.getSpecId());
            }
        }
        // 2. 按提交内容重建
        saveGoodsSpec(leaderId, goodsId, specList);
    }

    /**
     * 批量填充商品列表的规格信息(包含规格值), 避免循环内 N+1 查询
     */
    private void fillGoodsSpecList(List<LeaderGoodsResponse> data) {
        if (CollectionUtils.isEmpty(data)) {
            return;
        }
        List<Long> goodsIds = new ArrayList<>();
        for (LeaderGoodsResponse item : data) {
            goodsIds.add(item.getId());
        }
        Map<Long, List<GbGoodsSpecInfo>> specMap = specService.getGoodsSpecListByGoodsIds(goodsIds);
        for (LeaderGoodsResponse item : data) {
            List<GbGoodsSpecInfo> specList = specMap.get(item.getId());
            if (CollectionUtils.isEmpty(specList)) {
                item.setSpecList(new ArrayList<>());
            } else {
                item.setSpecList(LeaderSpecResponse.getSpecResponseList(specList));
            }
        }
    }

    /**
     * 批量填充商品列表的分类名称, 避免循环内 N+1 查询
     */
    private void fillGoodsCategoryName(List<LeaderGoodsResponse> data) {
        if (CollectionUtils.isEmpty(data)) {
            return;
        }
        Map<Long, String> catNameMap = categoryService.getGoodsCategoryNameMap();
        for (LeaderGoodsResponse item : data) {
            item.setCatName(catNameMap.get(item.getCat()));
        }
    }

    /**
     * 根据分类id查询分类名称
     */
    private String getCategoryName(Long catId) {
        if (catId == null) {
            return "";
        }
        String name = categoryService.getGoodsCategoryNameMap().get(catId);
        return name == null ? "" : name;
    }

}
