package cn.com.shopgroup.goods.controller;

import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.goods.http.request.leader.AddSpecRequest;
import cn.com.shopgroup.goods.http.request.leader.LeaderAddGoodsRequest;
import cn.com.shopgroup.goods.http.request.leader.LeaderAddSpecRequest;
import cn.com.shopgroup.goods.http.request.leader.LeaderAddSpecValRequest;
import cn.com.shopgroup.goods.http.request.leader.LeaderGoodsRequest;
import cn.com.shopgroup.goods.http.request.leader.LeaderGoodsStockRequest;
import cn.com.shopgroup.goods.http.request.leader.LeaderPackageListRequest;
import cn.com.shopgroup.goods.http.request.leader.LeaderPackageRequest;
import cn.com.shopgroup.goods.http.request.leader.LeaderSkuRequest;
import cn.com.shopgroup.goods.http.request.leader.SpecListRequest;
import cn.com.shopgroup.goods.http.request.leader.SpecRequest;
import cn.com.shopgroup.goods.http.request.leader.SpecValRequest;
import cn.com.shopgroup.goods.http.response.GoodsCategoryResponse;
import cn.com.shopgroup.goods.http.response.leader.LeaderGoodsResponse;
import cn.com.shopgroup.goods.http.response.leader.LeaderSkuResponse;
import cn.com.shopgroup.goods.http.response.leader.LeaderSpecResponse;
import cn.com.shopgroup.goods.http.response.leader.SpecValResponse;
import cn.com.shopgroup.goods.http.response.leader.leaderPackageResponse;
import cn.com.shopgroup.goods.model.GbGoodsCategoryInfo;
import cn.com.shopgroup.goods.model.GbGoodsInfo;
import cn.com.shopgroup.goods.model.GbGoodsPackageInfo;
import cn.com.shopgroup.goods.model.GbGoodsSkuInfo;
import cn.com.shopgroup.goods.model.GbGoodsSpecInfo;
import cn.com.shopgroup.goods.model.GbGoodsSpecValue;
import cn.com.shopgroup.goods.service.GbGoodsCategoryInfoService;
import cn.com.shopgroup.goods.service.GbGoodsInfoService;
import cn.com.shopgroup.goods.service.GbGoodsPackageInfoService;
import cn.com.shopgroup.goods.service.GbGoodsSkuInfoService;
import cn.com.shopgroup.goods.service.GbGoodsSpecInfoService;
import cn.com.shopgroup.goods.service.GbGoodsSpecValueService;
import cn.com.shopgroup.goods.service.GbGroupActivityInfoService;
import cn.com.shopgroup.goods.utils.SkuGenerateUtils;
import cn.com.shopgroup.user.model.GbOrgStaffInfo;
import cn.com.shopgroup.user.service.GbOrgMessageInfoService;
import cn.com.shopgroup.user.service.GbOrgStaffInfoService;
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

@RestController
@RequestMapping("/goods")
@Slf4j
public class LeaderGoodsController {

    @Resource
    private GbGoodsInfoService goodsService;

    @Resource
    private GbGoodsPackageInfoService packageService;

    @Resource
    private GbGoodsSpecInfoService goodsSpecInfoService;

    @Resource
    private GbGoodsSpecValueService specValueService;

    @Resource
    private GbGoodsSkuInfoService skuService;

    @Resource
    private GbGroupActivityInfoService groupActivityInfoService;

    @Resource
    private GbOrgMessageInfoService messageService;

    @Resource
    private GbOrgStaffInfoService staffService;

    @Resource
    private GbGoodsCategoryInfoService goodsCategoryInfoService;


    // 查询所有审核通过的商品, 且没有关闭的商品, 添加团购的时候使用
    @GetMapping("/leader/goods/online")
    public JsonResult online() {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) return JsonResult.fail("lid不存在");

        // 查询数据库
        List<GbGoodsInfo> result = goodsService.getMiniLeaderOnlineGoodsList(leaderId);
        List<LeaderGoodsResponse> data = LeaderGoodsResponse.getGoodsResponseList(result);
        // 商品规格信息
        fillGoodsSpecList(data);
        return JsonResult.success(data);
    }

    // 分页查询团长下的所有商品或（团长控制台首页-商品查询）
    @GetMapping("/leader/goods/list")
    public JsonResult goodsList(@RequestParam("cat") Long catId, @RequestParam("page") int page, @RequestParam("pageSize") int pageSize) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) return JsonResult.fail("lid不存在");

        // 请求参数矫正
        if (page == 0) page = 1;
        if (pageSize == 0) pageSize = 10;
        if (pageSize > 100) pageSize = 100;

        // 查询数据库
        List<GbGoodsInfo> result = goodsService.getMiniLeaderGoodsList(leaderId, catId, page, pageSize);
        List<LeaderGoodsResponse> data = LeaderGoodsResponse.getGoodsResponseList(result);
        // 商品规格信息
        fillGoodsSpecList(data);
        return JsonResult.success(data);
    }

    // 查询团长下的所有商品数量
    @GetMapping("/leader/goods/count")
    public JsonResult goodsCount(@RequestParam("cat") Long catId) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) return JsonResult.fail("lid不存在");

        // 查询数据库
        Long total = goodsService.getMiniLeaderGoodsCount(leaderId, catId);
        return JsonResult.success(total);
    }


    // 商品分类列表
    @GetMapping("/get/goods/cat")
    public JsonResult groupCat() {
        List<GbGoodsCategoryInfo> result = goodsCategoryInfoService.getGoodsCategoryList();
        if (CollectionUtils.isEmpty(result)) {
            return JsonResult.success();
        }
        List<GoodsCategoryResponse> data = GoodsCategoryResponse.getGoodsCategoryResponseList(result);
        return JsonResult.success(data);
    }

    // 添加商品
    @PostMapping("/leader/goods/addGoods")
    public JsonResult leaderAddGoods(@Validated @RequestBody LeaderAddGoodsRequest request) {
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) return JsonResult.fail("lid不存在");
        // 商品数据和图片数据
        List<LeaderAddSpecRequest> addSpecList = request.getAddSpecList();
        if (CollectionUtils.isEmpty(addSpecList)) {
            return JsonResult.fail("添加商品时规格为空，请重新填写");
        }
        GbGoodsInfo data = new GbGoodsInfo();
        data.setCatId(request.getCatId());
        data.setGoodsType(request.getType());
        data.setGoodsName(request.getName());
        data.setSalesPrice(request.getPrice());
        data.setMarketPrice(request.getPrice2());
        //data.setGoodsNum(request.getStockNum()); 默认先设置10000，产品要求
        data.setGoodsNum(10000);
        // 限购
        data.setIsLimit(request.getIsLimit());
        data.setLimitNum(request.getLimitNum());
        data.setGoodsUnit(request.getUnit());
        List<String> imgList = new ArrayList<>();
        imgList.add(request.getImg());
        imgList.add(request.getImg2());
        imgList.add(request.getImg3());
        // 添加数据库goods表
        Long goodsId = goodsService.addMiniLeaderGoodsInfo(leaderId, data, imgList);
        //插入关联表
        return handleGoodsAndSpecInfo(leaderId, goodsId, addSpecList);
    }

    /**
     * 商品与规格关联表
     *
     * @param leaderId
     * @param goodsId
     * @param addSpecList
     */
    private JsonResult handleGoodsAndSpecInfo(Long leaderId, Long goodsId, List<LeaderAddSpecRequest> addSpecList) {
        if (CollectionUtils.isEmpty(addSpecList)) {
            return JsonResult.fail("添加商品时规格为空");
        }
        List<Long> specInfoIds = new ArrayList<>();
        List<Long> specValues = new ArrayList<>();
        for (LeaderAddSpecRequest addSpecRequest : addSpecList) {
            List<LeaderAddSpecValRequest> specValLists = addSpecRequest.getSpecValLists();
            if (CollectionUtils.isEmpty(specValLists)) {
                return JsonResult.fail("添加商品时规格对应的值为空");
            }
            specInfoIds.add(addSpecRequest.getSpecId());
            for (LeaderAddSpecValRequest ret : specValLists) {
                specValues.add(ret.getValId());
            }
        }
        //把对应的商品id插入规格表
        goodsSpecInfoService.updateGoodsIdByIds(leaderId, goodsId, specInfoIds);
        specValueService.updateGoodsIdByIds(leaderId, goodsId, specInfoIds);
        return JsonResult.success("添加成功", goodsId);
    }

    // 查询商品
    @GetMapping("/leader/goods/info")
    public JsonResult goodsInfo(@RequestParam("id") Long goodsId) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) return JsonResult.fail("lid不存在");

        // 查询商品
        GbGoodsInfo goodsInfo = goodsService.getGoodsInfo(goodsId);
        LeaderGoodsResponse response = new LeaderGoodsResponse(goodsInfo);

        // 商品图片问题
        List<String> imgs = goodsService.getMiniGoodsImgList(goodsId, 3);
        if (imgs != null && imgs.size() > 0) {
            response.setImg(imgs.get(0));
            response.setImg2(imgs.get(1));
            response.setImg3(imgs.get(2));
        }

        // 商品规格信息(包含规格值)
        List<GbGoodsSpecInfo> specList = goodsSpecInfoService.getGoodsSpecListByGoodsId(goodsId);
        response.setSpecList(LeaderSpecResponse.getSpecResponseList(specList));

        // 返回
        return JsonResult.success(response);
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
        Map<Long, List<GbGoodsSpecInfo>> specMap = goodsSpecInfoService.getGoodsSpecListByGoodsIds(goodsIds);
        for (LeaderGoodsResponse item : data) {
            List<GbGoodsSpecInfo> specList = specMap.get(item.getId());
            if (CollectionUtils.isEmpty(specList)) {
                item.setSpecList(new ArrayList<>());
            } else {
                item.setSpecList(LeaderSpecResponse.getSpecResponseList(specList));
            }
        }
    }

    // 修改商品, 如果该商品正在团购中, 则不允许修改
    @PostMapping("/leader/goods/edit")
    public JsonResult editGoods(@Validated @RequestBody LeaderGoodsRequest request) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) return JsonResult.fail("lid不存在");

        // 如果该商品正在团购中, 则不允许修改
        if (groupActivityInfoService.isGoodsGrouping(leaderId, request.getId()))
            return JsonResult.fail("该商品正在团购中, 不允许修改");

        // 商品数据和图片数据
        GbGoodsInfo data = new GbGoodsInfo();
        data.setGoodsId(request.getId());
        //data.setCatId(request.getCat());
        //data.setGoodsType(request.getType());
        data.setGoodsName(request.getName());
        data.setSalesPrice(request.getPrice());
        data.setMarketPrice(request.getPrice2());
        data.setIsStock(request.getIsStock());
        data.setGoodsNum(request.getStockNum());

        // 限购
        data.setIsLimit(request.getIsLimit());
        data.setLimitNum(request.getLimitNum());

        data.setGoodsUnit(request.getUnit());
        List<String> imgList = new ArrayList<>();
        imgList.add(request.getImg());
        imgList.add(request.getImg2());
        imgList.add(request.getImg3());

        // 修改数据库
        boolean flag = goodsService.editMiniLeaderGoodsInfo(leaderId, data, imgList);
        if (flag) {
            handleGoodsAndSpecInfo(leaderId, request.getId(), request.getAddSpecList());
            return JsonResult.success();
        } else {
            return JsonResult.fail();
        }
    }

    // 关闭商品, 如果该商品正在团购中, 则不允许修改
    @GetMapping("/leader/goods/close")
    public JsonResult closeGoods(@RequestParam("id") Long goodsId) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) return JsonResult.fail("lid不存在");
        // 查询旧状态
        GbGoodsInfo goodsInfo = goodsService.getGoodsInfo(goodsId);
        if (ObjectUtils.isEmpty(goodsInfo)) {
            return JsonResult.fail("查询商品不存在,系统异常！");
        }
        // 如果该商品正在团购中, 则不允许修改
        if (groupActivityInfoService.isGoodsGrouping(leaderId, goodsId)) {
            return JsonResult.fail("该商品正在团购中, 不允许修改");
        }
        int status = 0;
        if (goodsInfo.getIsClose() == 0) {
            status = 1;
        }
        goodsService.closeMiniLeaderGoodsInfo(leaderId, goodsId, status);
        return JsonResult.success();
    }

    /******************************************************************************************/
    // 查询商品所有包装
    @GetMapping("/leader/goods/package/list")
    public JsonResult goodsPackageList(@RequestParam("id") Long goodsId) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) return JsonResult.fail("lid不存在");

        // 查询所有包装
        List<GbGoodsPackageInfo> result = packageService.getMiniLeaderGoodsPackageList(leaderId, goodsId);
        List<leaderPackageResponse> data = leaderPackageResponse.getPackageResponseList(result);
        return JsonResult.success(data);
    }

    // 批量编辑商品所有包装, 如果该商品正在团购中, 则不允许修改
    @PostMapping("/leader/goods/package/update")
    public JsonResult updateGoodsPackage(@Validated @RequestBody LeaderPackageListRequest request) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) return JsonResult.fail("lid不存在");

        // 先查询商品详情
        GbGoodsInfo goodsInfo = goodsService.getGoodsInfo(request.getGid());
        if (goodsInfo == null) return JsonResult.fail("商品不存在");

        // 补全包装信息
        int nowTime = TimeUtils.getTimeStamp();
        List<GbGoodsPackageInfo> packageInfoList = new ArrayList<>();
        for (LeaderPackageRequest item : request.getLists()) {

            GbGoodsPackageInfo temp = new GbGoodsPackageInfo();
            temp.setPackId(item.getId());
            temp.setLeaderId(goodsInfo.getLeaderId());
            temp.setGoodsId(goodsInfo.getGoodsId());

            temp.setPackName(item.getNum() + goodsInfo.getGoodsUnit());
            temp.setSalesPrice(item.getPrice());
            temp.setMarketPrice(0d);
            temp.setPackNum(item.getNum());
            temp.setGoodsUnit(goodsInfo.getGoodsUnit());
            temp.setAddTime(nowTime);

            packageInfoList.add(temp);
        }

        // 写入数据库, 兼容添加和修改两种情况, 还有删除的情况
        packageService.updateMiniLeaderGoodsPackageList(leaderId, packageInfoList);

        // 返回
        return JsonResult.success("保存成功");
    }

    // 查询商品规格(包含规格值)
    @GetMapping("/leader/goods/spec/list")
    public JsonResult getGoodsSpec(@RequestParam("id") Long goodsId) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) return JsonResult.fail("lid不存在");

        List<GbGoodsSpecInfo> result = goodsSpecInfoService.getMiniLeaderGoodsSpecList(goodsId);
        List<LeaderSpecResponse> data = LeaderSpecResponse.getSpecResponseList(result);
        return JsonResult.success(data);
    }

    /******************************************************************************************/
    // 查询商品规格(添加的时候使用,包含规格值)
    @GetMapping("/leader/goods/getSpec/list")
    public JsonResult getGoodSpecList() {
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            return JsonResult.fail("lid不存在");
        }
        List<GbGoodsSpecInfo> result = goodsSpecInfoService.getLeaderGoodsSpecList(leaderId);
        List<LeaderSpecResponse> data = LeaderSpecResponse.getSpecResponseList(result);
        return JsonResult.success(data);
    }


    // 批量编辑商品所有规格(包含规格值), 如果该商品正在团购中, 则不允许修改
    @PostMapping("/leader/goods/spec/update")
    public JsonResult updateGoodsSpec(@Validated @RequestBody SpecListRequest request) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) return JsonResult.fail("lid不存在");

        // 如果该商品正在团购中, 则不允许修改
        if (groupActivityInfoService.isGoodsGrouping(leaderId, request.getGid()))
            return JsonResult.fail("该商品正在团购中, 不允许修改");

        // 拼凑 GbGoodsSpecInfo 数据表对象 和 GbGoodsSpecValue 数据表对象
        List<GbGoodsSpecInfo> data = new ArrayList<>();
        for (SpecRequest item : request.getLists()) {

            // 构架规格(规格名称)
            GbGoodsSpecInfo temp = new GbGoodsSpecInfo();
            temp.setGoodsId(request.getGid());
            temp.setSpecName(item.getName());

            // 构建规格值列表(规格值)
            List<GbGoodsSpecValue> valList = new ArrayList<>();
            for (SpecValRequest valItem : item.getLists()) {
                GbGoodsSpecValue valTemp = new GbGoodsSpecValue();
                valTemp.setSpecVal(valItem.getVal());
                valList.add(valTemp);
            }
            temp.setSpecValueList(valList);

            // 添加规格到列表
            data.add(temp);
        }

        // 修改数据库, 先删除后修改的方式吧
        goodsSpecInfoService.updateMiniLeaderGoodsSpecVal(leaderId, request.getGid(), data);
        return JsonResult.success();
    }

    /******************************************************************************************/
    //添加规格
    @PostMapping("/leader/goods/spec/add")
    public JsonResult addGoodsSpec(@Validated @RequestBody AddSpecRequest request) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            return JsonResult.fail("lid不存在");
        }
        // 添加数据库
        GbGoodsSpecInfo data = new GbGoodsSpecInfo();
        Long goodsId = request.getGoodId();
        if (goodsId != null && goodsId.intValue() > 0) {
            // 如果该商品正在团购中, 则不允许修改
            if (groupActivityInfoService.isGoodsGrouping(leaderId, goodsId)) {
                return JsonResult.fail("该商品正在团购中, 不允许修改");
            }
            data.setGoodsId(goodsId);
        }

        data.setSpecName(request.getName());
        data.setIsPrice(request.getPrice());
        data.setIsStock(request.getStock());
        data.setLeaderId(leaderId);
        data.setIsClose((byte) 0);
        data.setAddTime(TimeUtils.getTimeStamp());
        goodsSpecInfoService.addMiniLeaderGoodsSpec(data);
        return JsonResult.success();
    }

    // 删除规格和对应的规格值, 如果该商品正在团购中, 则不允许修改
    @PostMapping("/leader/goods/spec/remove")
    public JsonResult removeGoodsSpec(@RequestParam("id") Long specId) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            return JsonResult.fail("lid不存在");
        }
        // 如果该商品正在团购中, 则不允许修改
        GbGoodsSpecInfo specInfo = goodsSpecInfoService.getGoodsSpecInfo(specId);
        if (groupActivityInfoService.isGoodsGrouping(leaderId, specInfo.getGoodsId())) {
            return JsonResult.fail("该商品正在团购中, 不允许修改");
        }

        // 删除规格和规格值
        specValueService.removeMiniLeaderGoodsSpecValList(specId);
        goodsSpecInfoService.removeMiniLeaderGoodsSpec(specId);
        return JsonResult.success();
    }

    // 查询商品规格值
    @GetMapping("/leader/goods/specVal/list")
    public JsonResult getGoodsSpecVal(@RequestParam("id") Long specId) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) return JsonResult.fail("lid不存在");

        // 查询列表
        List<GbGoodsSpecValue> result = specValueService.getMiniLeaderGoodsSpecValList(specId);
        List<SpecValResponse> data = SpecValResponse.getSpecValResponseList(result);
        return JsonResult.success(data);
    }

    // 添加商品规格值, 如果该商品正在团购中, 则不允许修改
    @PostMapping("/leader/goods/specVal/add")
    public JsonResult addGoodsSpecVal(@Validated @RequestBody SpecValRequest request) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            return JsonResult.fail("lid不存在");
        }
        // 添加数据库
        GbGoodsSpecValue data = new GbGoodsSpecValue();
        Long goodsId = request.getGid();
        if (goodsId != null && goodsId.intValue() > 0) {
            // 如果该商品正在团购中, 则不允许修改
            if (groupActivityInfoService.isGoodsGrouping(leaderId, goodsId)) {
                return JsonResult.fail("该商品正在团购中, 不允许修改");
            }
            data.setGoodsId(goodsId);
        }
        data.setSpecId(request.getSid());
        data.setSpecVal(request.getVal());
        data.setLeaderId(leaderId);
        data.setAddTime(TimeUtils.getTimeStamp());
        specValueService.addGoodsSpecVal(data);
        return JsonResult.success();
    }

    // 编辑商品规格值, 如果该商品正在团购中, 则不允许修改
    @PostMapping("/leader/goods/specVal/edit")
    public JsonResult editGoodsSpecVal(@Validated @RequestBody SpecValRequest request) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) return JsonResult.fail("lid不存在");

        // 如果该商品正在团购中, 则不允许修改
        Long goodsId = request.getGid();
        if (goodsId != null && goodsId.intValue() > 0) {
            // 如果该商品正在团购中, 则不允许修改
            if (groupActivityInfoService.isGoodsGrouping(leaderId, goodsId)) {
                return JsonResult.fail("该商品正在团购中, 不允许修改");
            }
        }
        // 修改数据库
        GbGoodsSpecValue data = new GbGoodsSpecValue();
        data.setValId(request.getId());
        data.setSpecId(request.getSid());
        data.setSpecVal(request.getVal());
        specValueService.editGoodsSpecVal(data);
        return JsonResult.success();
    }

    // 删除规格值, 如果该商品正在团购中, 则不允许修改
    @PostMapping("/leader/goods/specVal/remove")
    public JsonResult removeGoodsSpecVal(@Validated @RequestBody SpecValRequest request) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            return JsonResult.fail("lid不存在");
        }
        // 如果该商品正在团购中, 则不允许修改
        Long goodsId = request.getGid();
        if (goodsId != null && goodsId.intValue() > 0) {
            // 如果该商品正在团购中, 则不允许修改
            if (groupActivityInfoService.isGoodsGrouping(leaderId, goodsId)) {
                return JsonResult.fail("该商品正在团购中, 不允许修改");
            }
        }
        // 删除规格值
        specValueService.removeMiniLeaderGoodsSpecVal(request.getId());
        return JsonResult.success();
    }

    /******************************************************************************************/

    // 根据规格罗列所有SKU, 包括已经存在的sku信息
    @GetMapping("/leader/goods/sku/spec")
    public JsonResult getGoodsSkuSpec(@RequestParam("id") Long goodsId) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) return JsonResult.fail("lid不存在");

        // 查询所有规格和规格值, 两者都必须从小到大排序
        List<GbGoodsSpecInfo> specInfoList = goodsSpecInfoService.getMiniGoodsSpecList(goodsId);

        // 查询已经存在的sku信息
        List<GbGoodsSkuInfo> skuList = skuService.getMiniGoodsSkuList(goodsId);
        // 将list转map
        Map<String, GbGoodsSkuInfo> skuMap = new HashMap<>();
        for (GbGoodsSkuInfo item : skuList) {
            String ids = item.getSkuIds();
            skuMap.put(ids, item);
        }

        // 将规格值转化为SKU列表, 包含已经存在的sku信息
        List<GbGoodsSkuInfo> result = SkuGenerateUtils.generateSkuList(specInfoList);
        List<LeaderSkuResponse> data = new ArrayList<>();
        for (GbGoodsSkuInfo item : result) {
            String ids = item.getSkuIds();
            LeaderSkuResponse temp = new LeaderSkuResponse();
            temp.setIds(ids);
            temp.setGid(goodsId);
            temp.setNames(item.getSkuNames());
            if (skuMap.containsKey(ids)) {
                GbGoodsSkuInfo info = skuMap.get(ids);
                temp.setId(info.getSkuId());
                temp.setPrice(info.getSalesPrice());
                temp.setPrice2(info.getMarketPrice());
                temp.setNum(info.getGoodsNum());
                temp.setImg(info.getGoodsImg());
            } else {
                temp.setId(0l);
                temp.setPrice(0d);
                temp.setPrice2(0d);
                temp.setNum(0);
                temp.setImg("");
            }
            data.add(temp);
        }

        // 返回
        return JsonResult.success(data);
    }

    // 查询商品SKU
    //@GetMapping("/leader/goods/sku/list")
    public JsonResult getGoodsSku(@RequestParam("id") Long goodsId) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) return JsonResult.fail("lid不存在");

        // 查询所有sku
        //List<GbGoodsSkuInfo> result = skuService.getMiniLeaderGoodsSkuList(goodsId);
        //List<SkuResponse> data = SkuResponse.getSkuResponseList(result);
        //return JsonResult.success(data);
        return JsonResult.success();
    }

    // 批量添加商品SKU(先删除后添加！！！)
    // 如果该商品正在团购中, 则不允许修改
    @PostMapping("/leader/goods/sku/save")
    public JsonResult saveGoodsSku(@Validated @RequestBody List<LeaderSkuRequest> requestList) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) return JsonResult.fail("lid不存在");

        // 商品不存在
        Long goodsId = requestList.get(0).getGid();
        if (goodsId == 0) return JsonResult.fail("商品不存在");

        // 如果该商品正在团购中, 则不允许修改
        if (groupActivityInfoService.isGoodsGrouping(leaderId, goodsId))
            return JsonResult.fail("该商品正在团购中, 不允许修改");

        // 用户提交的SKU
        List<GbGoodsSkuInfo> dataList = new ArrayList<>();
        for (LeaderSkuRequest request : requestList) {
            GbGoodsSkuInfo temp = new GbGoodsSkuInfo();
            temp.setSkuId(request.getId());
            temp.setGoodsId(request.getGid());
            temp.setLeaderId(leaderId);
            temp.setSkuIds(request.getIds());
            temp.setSkuNames(request.getNames());
            temp.setSalesPrice(request.getPrice());
            temp.setMarketPrice(request.getPrice2());
            temp.setGoodsNum(request.getNum());
            temp.setGoodsImg(request.getImg());
            dataList.add(temp);
        }

        // 先删除后添加
        skuService.saveMiniLeaderGoodsSku(leaderId, goodsId, dataList);
        return JsonResult.success();
    }

    // 调整商品库存
    @PostMapping("/leader/goods/stock")
    public JsonResult goodsStock(@Validated @RequestBody LeaderGoodsStockRequest request) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) return JsonResult.fail("lid不存在");

        // 从请求头中获取员工id
        Long staffId = RequestParamsUtils.getRequestHeaderStaffId();
        if (staffId == 0) return JsonResult.fail("sid不存在");

        // 商品不存在
        GbGoodsInfo goodsInfo = goodsService.getGoodsInfo(request.getGid());
        if (goodsInfo == null) return JsonResult.fail("商品不存在");

        // 增加和减少商品数量: 1增加2减少
        if (request.getGtype() == 1) {
            // 增加
            goodsService.increaseGoodsStock(request.getGid(), request.getGnum());
        } else {
            // 减少, 不能出现负数
            if (request.getGnum() > goodsInfo.getGoodsNum()) request.setGnum(goodsInfo.getGoodsNum());
            goodsService.reduceGoodsStock(request.getGid(), request.getGnum());
        }

        // 添加日志, 消息类型: 1=系统消息2=内部消息3=业务消息
        byte type = 2;
        String oper = "修改了";
        if (request.getGtype() == 1) {
            oper = "增加了";
        } else {
            oper = "减少了";
        }
        GbOrgStaffInfo staffInfo = staffService.getStaffInfo(staffId);
        String content = staffInfo.getStaffName() + " " + oper + " " + goodsInfo.getGoodsName() + " 的 " + request.getGnum() + " 库存。";
        messageService.addMiniLeaderMessageInfo(leaderId, staffId, type, content);

        // 返回
        return JsonResult.success();
    }


}
