package cn.com.shopgroup.goods.controller;

import cn.com.shopgroup.common.cache.RedisConstant;
import cn.com.shopgroup.common.cache.RedisHelper;
import cn.com.shopgroup.common.config.UploadConfig;
import cn.com.shopgroup.common.utils.HuaWeiOBS;
import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.common.utils.PosterDTO;
import cn.com.shopgroup.common.utils.PosterGroupUtils;
import cn.com.shopgroup.common.utils.PosterParam;
import cn.com.shopgroup.common.utils.PosterUtils;
import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.common.wxmini.WxMiniAccessTokenHelper;
import cn.com.shopgroup.common.wxmini.WxMiniProgramHelper;
import cn.com.shopgroup.goods.http.request.group.GroupActGoodsRequest;
import cn.com.shopgroup.goods.http.request.group.GroupActRequest;
import cn.com.shopgroup.goods.http.request.leader.LeaderGroupListRequest;
import cn.com.shopgroup.goods.http.response.group.GroupActGoodsResponse;
import cn.com.shopgroup.goods.http.response.leader.GroupActResponse;
import cn.com.shopgroup.goods.http.response.leader.GroupCatResponse;
import cn.com.shopgroup.goods.model.GbGoodsInfo;
import cn.com.shopgroup.goods.model.GbGroupActivityGoods;
import cn.com.shopgroup.goods.model.GbGroupActivityInfo;
import cn.com.shopgroup.goods.model.GbGroupCategoryInfo;
import cn.com.shopgroup.goods.model.GbGroupTag;
import cn.com.shopgroup.goods.service.GbGoodsInfoService;
import cn.com.shopgroup.goods.service.GbGroupTagService;
import cn.com.shopgroup.goods.service.GbGroupActivityInfoService;
import cn.com.shopgroup.goods.service.GbGroupCategoryInfoService;
import cn.com.shopgroup.user.model.GbOrgShopInfo;
import cn.com.shopgroup.user.model.GbOrgStaffInfo;
import cn.com.shopgroup.user.service.GbOrgLeaderInfoService;
import cn.com.shopgroup.user.service.GbOrgShopInfoService;
import cn.com.shopgroup.user.service.GbOrgStaffInfoService;
import cn.com.shopgroup.user.utils.RequestParamsUtils;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

//团长端-团购活动管理
@RestController
@Slf4j
@RequestMapping("/goods/Leader")
public class LeaderGroupManageController {

    @Autowired
    private GbGroupActivityInfoService activityInfoService;

    @Autowired
    private GbOrgStaffInfoService staffService;

    @Autowired
    private GbOrgShopInfoService shopService;

    @Autowired
    private GbGroupCategoryInfoService categoryService;

    @Autowired
    private GbGroupTagService tagService;

    @Autowired
    private GbGoodsInfoService goodsService;

    @Autowired
    private RedisHelper redisHelper;

    @Autowired
    private UploadConfig uploadConfig;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private WxMiniAccessTokenHelper helper;


    // 查询所有团购活动列表
    @PostMapping("/get/groupActivity/list")
    public JsonResult getGroupActiveList(@Validated @RequestBody LeaderGroupListRequest request) {
        log.info("团长端-查询所有团购活动列表,request:{}", JSON.toJSONString(request));
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            return JsonResult.fail("lid不存在");
        }

        // 请求参数矫正
        int page = Optional.ofNullable(request.getPage()).orElse(1);
        int pageSize = Optional.ofNullable(request.getPageSize())
                .map(size -> Math.min(size, 100))
                .orElse(10);
        String activityName = request.getName();
        int status = Optional.ofNullable(request.getStatus().intValue()).orElse(0);

        // 查询列表(1、团长团查询 2 用户端查询)
        List<GbGroupActivityInfo> result = activityInfoService.getMiniLeaderGroupList(1, leaderId, request.getCatId(), activityName, status, page, pageSize);
        List<GroupActResponse> data = GroupActResponse.getGroupResponseList(result);
        return JsonResult.success(data);
    }

    // 查询所有团购活动总数(筛选条件与列表接口一致, 保证分页总页数正确)
    @GetMapping("/get/groupActivity/count")
    public JsonResult groupActivityCount(@RequestParam(value = "cat", required = false) Long catId,
                                         @RequestParam(value = "name", required = false) String name,
                                         @RequestParam(value = "status", required = false, defaultValue = "0") Integer status) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            return JsonResult.fail("lid不存在");
        }
        // 查询总数
        long total = activityInfoService.getMiniLeaderGroupCount(leaderId, catId, name, Optional.ofNullable(status).orElse(0));
        return JsonResult.success(total);
    }

    // 团购标签下拉列表(添加/编辑团购活动时选择标签)
    @GetMapping("/groupActivity/tag/list")
    public JsonResult getGroupTagList() {
        log.info("团长端-团购标签下拉列表,开始");
        return JsonResult.success(tagService.getEnabledTagList());
    }

    // 设置团购活动标签: 根据标签id补全标签名称(冗余), 便于列表/详情直接展示
    private void fillGroupTagData(GbGroupActivityInfo data, Long tagId) {
        Long fixTagId = tagId == null ? 0L : tagId;
        data.setTagId(fixTagId);
        String tagName = "";
        if (fixTagId > 0) {
            GbGroupTag tag = tagService.getTagById(fixTagId);
            if (tag != null) {
                tagName = tag.getTagName();
            }
        }
        data.setTagName(tagName);
    }

    // 添加团购活动
    @PostMapping("/groupActivity/add")
    public JsonResult addGroup(@Validated @RequestBody GroupActRequest request) {
        log.info("[添加团购活动]参数:{}", JSON.toJSONString(request));
        // 1. 校验时间
        if (request.getEndTime() < (request.getStartTime())) {
            return JsonResult.fail("结束时间必须晚于开始时间");
        }
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        log.info("添加团购活动时，团长id:{}", leaderId);
        if (leaderId == 0) {
            return JsonResult.fail("lid不存在");
        }
        // 从请求头中获取员工id
        Long staffId = RequestParamsUtils.getRequestHeaderStaffId();
        log.info("添加团购活动时，员工id:{}", staffId);
        GbOrgStaffInfo staffInfo = new GbOrgStaffInfo();
        // 重构数据
        GbGroupActivityInfo data = new GbGroupActivityInfo();
        if (staffId != 0) {
            staffInfo = staffService.getStaffInfo(staffId);
            log.info("添加团购活动时，stfInfo:{}", JSON.toJSONString(staffInfo));
            if (ObjectUtils.isEmpty(staffInfo)) {
                return JsonResult.fail("未查询到员工信息");
            }
            // 添加人员id
            data.setStaffId(staffId);
            // 添加人员姓名
            data.setStaffName(staffInfo.getStaffName());
        }

        // 团购商品信息兜底: 价格/名称/图片/类型未传时, 取商品表数据(一次批量查询, 避免循环内 N+1)
        fillGroupActGoodsInfo(request.getGoods());
        // 团购id,主键自增
        //data.setGroupId(0);
        // 团购分类id
        data.setCatId(request.getCat());
        // 团长id
        data.setLeaderId(leaderId);
        // 数据隔离id
        data.setIsolationId(0);
        // 商品提货方式,1自提2邮递
        data.setPickupStyle(request.getPickup());
        // 团购名称
        data.setGroupName(request.getName());

        // 单个商品
        if (request.getGoods().size() == 1) {

            // 查询商品图片(不足3张时补空串, 避免越界)
            Long tempId = request.getGoods().get(0).getGid();
            List<String> tempList = goodsService.getMiniGoodsImgList(tempId, 3);
            // 团购主图
            data.setGroupImg(CollectionUtils.isEmpty(tempList) ? "" : tempList.get(0));
            // 团购主图2
            data.setGroupImg2(tempList.size() > 1 ? tempList.get(1) : "");
            // 团购主图3
            data.setGroupImg3(tempList.size() > 2 ? tempList.get(2) : "");
            // 团购价格/最小价格
            data.setGroupPrice(request.getGoods().get(0).getPrice());
            // 团购价格/最大价格(单商品时最大=最小)
            data.setGroupPrice2(request.getGoods().get(0).getPrice());
            // 市场价格/划线价格
            data.setMarketPrice(request.getGoods().get(0).getPrice2());

        } else {

            // 考虑两个商品的情况
            data.setGroupImg(request.getGoods().get(0).getImg());
            if (request.getGoods().size() == 2) {
                data.setGroupImg2(request.getGoods().get(1).getImg());
                data.setGroupImg3(request.getGoods().get(1).getImg()); // 空图片
            } else {
                data.setGroupImg2(request.getGoods().get(1).getImg());
                data.setGroupImg3(request.getGoods().get(2).getImg());
            }
            // 寻找最低价格和最高价格
            double max = 0.0;
            double min = 9999.0;
            for (GroupActGoodsRequest item : request.getGoods()) {
                if (item.getPrice() >= max) {
                    max = item.getPrice();
                }
                if (item.getPrice() <= min) {
                    min = item.getPrice();
                }
            }
            // 团购价格/最小价格
            data.setGroupPrice(min);
            // 团购价格/最大价格
            data.setGroupPrice2(max);
            // 市场价格/划线价格(取第一个商品的划线价)
            data.setMarketPrice(request.getGoods().get(0).getPrice2());
        }

        // 团购介绍
        data.setGroupInfo(request.getInfo());
        // 虚拟订单数量
        data.setVirtualOrder(request.getVirtual());
        data.setStartTime(request.getStartTime());
        data.setEndTime(request.getEndTime());
        // 是否禁用,0上线1下线
        data.setIsClose((byte) 0);
        // 团购标签
        fillGroupTagData(data, request.getTagId());

        // 重新构建商品列表
        List<GbGroupActivityGoods> lists = new ArrayList<>();
        for (GroupActGoodsRequest item : request.getGoods()) {

            GbGroupActivityGoods goods = new GbGroupActivityGoods();
            // 团购id,外键
            goods.setGroupId(0L);
            // 商品id,外键
            goods.setGoodsId(item.getGid());
            // 商品名称,冗余
            goods.setGoodsName(item.getGname());
            // 商品类型,冗余
            goods.setGoodsType(item.getGtype());
            // 商品主图,冗余
            goods.setGroupImg(item.getImg());
            // 团购价格
            goods.setGroupPrice(item.getPrice());
            // 市场价格
            goods.setMarketPrice(item.getPrice2());
            lists.add(goods);
        }
        data.setLists(lists);

        // 写入数据库
        Long groupId = activityInfoService.addMiniLeaderGroupInfo(leaderId, data);
        return JsonResult.success(groupId);
    }

    // 查询团购信息, 还要查询商品列表(价格以团购商品表冗余的团购价为准)
    @GetMapping("/get/groupActivity/info")
    public JsonResult getGroupActivity(@RequestParam("groupId") Long groupId) {
        log.info("goods/get/groupActivity/info groupId:{}",groupId);
        GbGroupActivityInfo groupInfo = activityInfoService.getGroupInfo(groupId);
        if (ObjectUtils.isEmpty(groupInfo)) {
            return JsonResult.fail("未查到相关团购活动信息");
        }
        // 团购商品列表(冗余表, 含团购价/市场价/商品名称/主图)
        List<GbGroupActivityGoods> activityGoodsList = activityInfoService.getGroupActivityGoodsList(groupId);
        if (CollectionUtils.isEmpty(activityGoodsList)) {
            return JsonResult.fail("未查到相关团购商品信息");
        }
        // 商品表数据(补充单位/库存), 一次批量查询
        List<Long> goodsIds = new ArrayList<>();
        for (GbGroupActivityGoods item : activityGoodsList) {
            goodsIds.add(item.getGoodsId());
        }
        List<GbGoodsInfo> goodsList = goodsService.getGoodsInfoList(goodsIds);
        Map<Long, GbGoodsInfo> goodsMap = new HashMap<>();
        for (GbGoodsInfo item : goodsList) {
            goodsMap.put(item.getGoodsId(), item);
        }

        GroupActResponse response = new GroupActResponse(groupInfo);
        List<GroupActGoodsResponse> goodsResponses = new ArrayList<>();
        for (GbGroupActivityGoods item : activityGoodsList) {
            goodsResponses.add(new GroupActGoodsResponse(item, goodsMap.get(item.getGoodsId())));
        }
        response.setGoods(goodsResponses);
        return JsonResult.success(response);
    }

    // 修改团购活动, 团购进行中, 不允许修改
    @PostMapping("/groupActivity/edit")
    public JsonResult editGroup(@Validated @RequestBody GroupActRequest request) {
        log.info("编辑团购活动请求request:{}", JSON.toJSONString(request));
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            return JsonResult.fail("lid不存在");
        }

        // 从请求头中获取员工id
        Long staffId = RequestParamsUtils.getRequestHeaderStaffId();
        log.info("编辑团购活动时，员工id:{}", staffId);
        GbOrgStaffInfo staffInfo = new GbOrgStaffInfo();
        // 重构数据
        GbGroupActivityInfo data = new GbGroupActivityInfo();
        if (staffId != 0) {
            staffInfo = staffService.getStaffInfo(staffId);
            log.info("添加团购活动时，stfInfo:{}", JSON.toJSONString(staffInfo));
            if (ObjectUtils.isEmpty(staffInfo)) {
                return JsonResult.fail("未查询到员工信息");
            }
            // 修改人员姓名
            data.setStaffName(staffInfo.getStaffName());
        }
        // 团购进行中, 不允许修改
        GbGroupActivityInfo groupInfo = activityInfoService.getGroupInfo(request.getId());
        if (ObjectUtils.isEmpty(groupInfo)) {
            return JsonResult.fail("未查询到团购活动信息");
        }
        int nowTime = TimeUtils.getTimeStamp();
        int startTime = groupInfo.getStartTime().intValue();
        int endTime = groupInfo.getEndTime().intValue();
        if (startTime < nowTime && nowTime < endTime) {
            return JsonResult.fail("团购进行中, 不允许修改");
        }

        // 团购商品信息兜底: 价格/名称/图片/类型未传时, 取商品表数据(一次批量查询, 避免循环内 N+1)
        fillGroupActGoodsInfo(request.getGoods());

        // 团购id
        data.setGroupId(request.getId());
        // 团购分类id
        data.setCatId(request.getCat());
        // 团购名称
        data.setGroupName(request.getName());
        // 商品提货方式,1自提2邮递
        data.setPickupStyle(request.getPickup());

        // 单个商品
        if (request.getGoods().size() == 1) {

            // 查询商品图片(不足3张时补空串, 避免越界)
            Long tempId = request.getGoods().get(0).getGid();
            List<String> tempList = goodsService.getMiniGoodsImgList(tempId, 3);
            // 团购主图
            data.setGroupImg(CollectionUtils.isEmpty(tempList) ? "" : tempList.get(0));
            // 团购主图2
            data.setGroupImg2(tempList.size() > 1 ? tempList.get(1) : "");
            // 团购主图3
            data.setGroupImg3(tempList.size() > 2 ? tempList.get(2) : "");
            // 团购价格/最小价格
            data.setGroupPrice(request.getGoods().get(0).getPrice());
            // 团购价格/最大价格(单商品时最大=最小)
            data.setGroupPrice2(request.getGoods().get(0).getPrice());
            // 市场价格/划线价格
            data.setMarketPrice(request.getGoods().get(0).getPrice2());

        } else {

            // 团购主图
            data.setGroupImg(request.getGoods().get(0).getImg());
            // 团购主图2
            if (request.getGoods().size() >= 2) {
                data.setGroupImg2(request.getGoods().get(1).getImg());
            } else {
                data.setGroupImg2(request.getGoods().get(0).getImg());
            }
            // 团购主图3
            if (request.getGoods().size() >= 3) {
                data.setGroupImg3(request.getGoods().get(2).getImg());
            } else {
                data.setGroupImg3(request.getGoods().get(0).getImg());
            }
            // 寻找最低价格和最高价格
            double max = 0.0;
            double min = 9999.0;
            for (GroupActGoodsRequest item : request.getGoods()) {
                if (item.getPrice() >= max) {
                    max = item.getPrice();
                }
                if (item.getPrice() <= min) {
                    min = item.getPrice();
                }
            }
            // 团购价格/最小价格
            data.setGroupPrice(min);
            // 团购价格/最大价格
            data.setGroupPrice2(max);
            // 市场价格/划线价格(取第一个商品的划线价)
            data.setMarketPrice(request.getGoods().get(0).getPrice2());
        }

        // 团购介绍
        data.setGroupInfo(request.getInfo());
        // 虚拟订单数量
        data.setVirtualOrder(request.getVirtual());

        data.setStartTime(request.getStartTime());
        data.setEndTime(request.getEndTime());
        // 团购标签
        fillGroupTagData(data, request.getTagId());

        // 重新构建商品列表
        List<GbGroupActivityGoods> lists = new ArrayList<>();
        for (GroupActGoodsRequest item : request.getGoods()) {

            GbGroupActivityGoods goods = new GbGroupActivityGoods();
            // 团购id,外键
            goods.setGroupId(request.getId());
            // 商品id,外键
            goods.setGoodsId(item.getGid());
            // 商品名称,冗余
            goods.setGoodsName(item.getGname());
            // 商品类型,冗余
            goods.setGoodsType(item.getGtype());
            // 商品主图,冗余
            goods.setGroupImg(item.getImg());
            // 团购价格
            goods.setGroupPrice(item.getPrice());
            // 市场价格
            goods.setMarketPrice(item.getPrice2());
            lists.add(goods);
        }
        data.setLists(lists);

        // 修改数据库
        boolean flag = activityInfoService.editMiniLeaderGroupInfo(leaderId, data);
        if (flag) {
            return JsonResult.success("修改成功");
        } else {
            return JsonResult.fail("修改失败");
        }
    }

    // 关闭团购活动, 平台审核未通过, 不允许打开上线
    // 打开上线的话, 需要清空Redis缓存, 任何修改团购和商品信息, 都必须是关闭状态
    // 这样就不需要修改的时候同步Redis缓存了, 只需要关闭修改完, 打开上线的时候更新一次即可
    @PostMapping("/groupActivity/close")
    public JsonResult closeGroup(@RequestParam("groupId") Long groupId) {
        log.info("goods/groupActivity/close groupId:{}",groupId);
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            return JsonResult.fail("lid不存在");
        }

        // 从请求头中获取员工id
        Long staffId = RequestParamsUtils.getRequestHeaderStaffId();
        GbOrgStaffInfo staffInfo = new GbOrgStaffInfo();
        Long updateId = 0L;
        String updateName = "";
        if (staffId != 0) {
            // 员工信息
            staffInfo = staffService.getStaffInfo(staffId);
            if (ObjectUtils.isEmpty(staffInfo)) {
                return JsonResult.fail("未查询到员工信息");
            }
            updateId = staffId;
            updateName = staffInfo.getStaffName();
        } else {
            updateId = leaderId;
            updateName = "团长自己";
        }

        // 查询旧状态
        GbGroupActivityInfo groupInfo = activityInfoService.getGroupInfo(groupId);
        if (ObjectUtils.isEmpty(groupInfo)) {
            return JsonResult.fail("未查询到团购活动信息");
        }
        if (groupInfo.getIsClose() == 0) {
            // 关闭
            activityInfoService.closeMiniLeaderGroupInfo(leaderId, groupId, 1, updateId, updateName, groupInfo.getGroupName());
        } else {
            // 平台审核未通过, 不允许打开上线
            if (groupInfo.getIsCheck() == 0) {
                return JsonResult.fail("平台未审核, 不允许打开上线");
            }
            // 打开
            activityInfoService.closeMiniLeaderGroupInfo(leaderId, groupId, 0, updateId, updateName, groupInfo.getGroupName());
        }

        // 清空团购缓存数据
        String key = RedisConstant.RedisGroupInfoKey + groupId;
        redisHelper.deleteObject(key);

        // 清空团购商品缓存数据
        String key2 = RedisConstant.RedisGroupGoodsListKey + groupId;
        redisHelper.deleteObject(key2);

        // 清空团购商品库存缓存数据
        //List<GbGroupActivityGoods> goodsList = service.getGroupActivityGoodsList(groupId);
        //for(GbGroupActivityGoods item : goodsList){
        //    String tempKey = RedisConstant.RedisGoodsStockKey + item.getGoodsId();
        //    redisHelper.deleteObject(tempKey);
        //}

        // 返回
        return JsonResult.success();
    }

    // 团购分类列表
    @GetMapping("/get/groupActivity/cat")
    public JsonResult groupCat() {

        List<GbGroupCategoryInfo> result = categoryService.getMiniGroupCategoryList();
        List<GroupCatResponse> data = GroupCatResponse.getGroupCatResponseList(result);
        return JsonResult.success(data);
    }

    // 分享团购海报生成1
    @PostMapping("/share/groupActivity/poster")
    public JsonResult shareGroup(@RequestParam("groupId") Long groupId) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) return JsonResult.fail("lid不存在");

//        // 从请求头中获取员工id
//        Long staffId = RequestParamsUtils.getRequestHeaderStaffId();
//        if (staffId == 0) return JsonResult.fail("sid不存在");

        // 查询团购详情
        GbGroupActivityInfo groupInfo = activityInfoService.getGroupInfo(groupId);
        if (ObjectUtils.isEmpty(groupInfo)) {
            return JsonResult.fail("团购不存在");
        }

        // 第一个团购商品
        List<GbGroupActivityGoods> goodsList = activityInfoService.getGroupActivityGoodsList(groupId);
        if (CollectionUtils.isEmpty(goodsList)) {
            return JsonResult.fail("团购商品不存在");
        }
        GbGroupActivityGoods goodsInfo = goodsList.get(0);

        // 构建分享海报信息
        PosterDTO dto = new PosterDTO();
        dto.setGoodsName(goodsInfo.getGoodsName()); // 团长名称还是商品名称
        dto.setSaleText("已售" + (groupInfo.getVirtualOrder() + groupInfo.getOrderTotal()) + "单");
        dto.setPriceText("￥" + goodsInfo.getGroupPrice());
        dto.setBtnText("立即跟团");
        dto.setImgUrl(goodsInfo.getGroupImg());

        // 生成海报
        ByteArrayInputStream bis = null;
        try {
            bis = PosterUtils.generatePosterStream(dto);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 判断本地上传还是云存储上传: # 1=本地上传, 2=云端上传
        int type = uploadConfig.getType();
        String domain = uploadConfig.getDomain();
        String rootPath = uploadConfig.getPath();

        // 文件名称和访问路径
        String url = "";
        String obsKey = "poster/" + TimeUtils.getTodayStr() + "/" + UUID.randomUUID() + ".png";

        // 本地路径创建
        if (type == 1) {

            // 本地指定路径
            File fileFolder = new File(rootPath + File.separator + "poster" + File.separator + TimeUtils.getTodayStr());
            if (!fileFolder.exists()) fileFolder.mkdirs();

            // 保存到本地指定路径
            try {
                this.saveStreamToLocalFile(bis, rootPath + File.separator + obsKey);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 本地访问路径
            url = domain + "/" + obsKey;

        } else {

            // 上传到华为云OBS
            url = HuaWeiOBS.upload(obsKey, bis);
        }

        // 返回访问路径
        return JsonResult.success("查询成功", url);
    }

    // 分享团购活动海报（带有logo的海报）
    @PostMapping("/share/groupActivity/make/poster")
    public JsonResult makeShareGroupPoster(@RequestParam("groupId") Long groupId) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) return JsonResult.fail("lid不存在");

        // 从请求头中获取员工id
//        Long staffId = RequestParamsUtils.getRequestHeaderStaffId();
//        if (staffId == 0) return JsonResult.fail("sid不存在");

        // 查询团购详情
        GbGroupActivityInfo groupInfo = activityInfoService.getGroupInfo(groupId);
        if (ObjectUtils.isEmpty(groupInfo)) {
            return JsonResult.fail("团购不存在");
        }

        // 第一个团购商品
        List<GbGroupActivityGoods> goodsList = activityInfoService.getGroupActivityGoodsList(groupId);
        if (CollectionUtils.isEmpty(goodsList)) {
            return JsonResult.fail("团购商品不存在");
        }
        GbGroupActivityGoods goodsInfo = goodsList.get(0);

        // 店铺logo
        String shopName = "店小团";
        String shopLogo = "https://shopgroup.obs.cn-north-9.myhuaweicloud.com/logo.png";
        GbOrgShopInfo shopInfo = shopService.getMiniLeaderShop(leaderId);
        if (shopInfo != null && shopInfo.getShopName().length() > 0) shopName = shopInfo.getShopName();
        if (shopInfo != null && shopInfo.getShopLogo().length() > 0) shopLogo = shopInfo.getShopLogo();

        // 开始生成海报
        try {

            // 本地爆款推荐图片
            Resource resource = (Resource) resourceLoader.getResource("classpath:static/poster.png");

            // 统一获取AccessToken
            String accessToken = helper.getAccessToken(false);
            if (accessToken == null || accessToken.length() == 0) return JsonResult.fail("获取AccessToken失败");

            // 获取二维码
            String page = "pages/group/index";
            String scene = "id=" + groupInfo.getGroupId() + "&lid=" + groupInfo.getLeaderId();
            int wh = 280; // 280像素大小, 跟海报生成的 二维码尺寸 保持一致哦

            // 直接获取二维码文件流即可
            BufferedImage ercodeImage = WxMiniProgramHelper.getMiniProgramPageERcodeBufferedImage(accessToken, page, scene, wh);
            //File outFile = new File("E:/idea_workspace/GroupBuyApi/image/public/ercode.png");
            //ImageIO.write(ercodeImage, "png", outFile);
            //BufferedImage ercodeImage = ImageIO.read(outFile);

            // 构架海报参数
            PosterParam param = new PosterParam();
            param.shopLogoUrl = shopLogo;
            param.shopName = shopName;
            param.showTime = TimeUtils.getNowTime();
            param.hotImgLocalPath = resource.getInputStream(); //本地爆款推荐图片
            param.goodsImgUrl = goodsInfo.getGroupImg(); //商品网络图片
            param.goodsName = goodsInfo.getGoodsName();
            param.price = "￥" + goodsInfo.getGroupPrice() + "元";
            param.sales = "已售" + (groupInfo.getVirtualOrder() + groupInfo.getOrderTotal()) + "件";
            param.ercodeImage = ercodeImage; //本地二维码图片
            param.tipText = "长按识别,跟团购买";

            // 生成海报
            ByteArrayInputStream poster = PosterGroupUtils.generatePoster(param);

            // 判断本地上传还是云存储上传: # 1=本地上传, 2=云端上传
            int type = uploadConfig.getType();
            String domain = uploadConfig.getDomain();
            String rootPath = uploadConfig.getPath();

            // 文件名称
            String obsKey = "ercode2/" + TimeUtils.getTodayStr() + "/" + UUID.randomUUID() + ".png";

            // 本地路径创建
            if (type == 1) {
                File fileFolder = new File(rootPath + File.separator + "ercode2" + File.separator + TimeUtils.getTodayStr());
                if (!fileFolder.exists()) fileFolder.mkdirs();
            }

            // 区分本地上传还是云端上传
            if (type == 1) {
                WxMiniProgramHelper.saveLocalFile(rootPath + "/" + obsKey, poster);
            } else {
                HuaWeiOBS.upload(obsKey, poster);
            }

            // 获取图片访问路径
            String url = domain + "/" + obsKey;
            return JsonResult.success("生成海报成功", url);

        } catch (IOException e) {
            e.printStackTrace();
        }

        // 失败
        return JsonResult.fail();
    }


    /**
     * 团购商品信息兜底: 价格/名称/图片/类型未传时, 取商品表数据
     * 一次批量查询商品信息, 避免循环内 N+1 查询
     */
    private void fillGroupActGoodsInfo(List<GroupActGoodsRequest> goodsList) {
        if (CollectionUtils.isEmpty(goodsList)) {
            return;
        }
        // 收集商品id, 一次查询
        List<Long> goodsIds = new ArrayList<>();
        for (GroupActGoodsRequest item : goodsList) {
            goodsIds.add(item.getGid());
        }
        List<GbGoodsInfo> goodsInfoList = goodsService.getGoodsInfoList(goodsIds);
        Map<Long, GbGoodsInfo> goodsInfoMap = new HashMap<>();
        for (GbGoodsInfo goodsInfo : goodsInfoList) {
            goodsInfoMap.put(goodsInfo.getGoodsId(), goodsInfo);
        }
        // 逐项兜底
        for (GroupActGoodsRequest item : goodsList) {
            GbGoodsInfo goodsInfo = goodsInfoMap.get(item.getGid());
            if (goodsInfo == null) {
                continue;
            }
            // 团购价格兜底(取商品销售价)
            if (item.getPrice() == null || item.getPrice() <= 0) {
                if (goodsInfo.getSalesPrice() != null) {
                    item.setPrice(goodsInfo.getSalesPrice());
                }
            }
            // 市场价兜底(取商品市场价)
            if (item.getPrice2() == null || item.getPrice2() <= 0) {
                if (goodsInfo.getMarketPrice() != null) {
                    item.setPrice2(goodsInfo.getMarketPrice());
                }
            }
            // 冗余字段兜底: 名称/主图/类型
            if (item.getGname() == null || item.getGname().trim().length() == 0) {
                item.setGname(goodsInfo.getGoodsName());
            }
            if (item.getImg() == null || item.getImg().trim().length() == 0) {
                item.setImg(goodsInfo.getGoodsImg());
            }
            if (item.getGtype() == null) {
                item.setGtype(goodsInfo.getGoodsType());
            }
        }
    }

    // 输入流保存本地文件
    private void saveStreamToLocalFile(ByteArrayInputStream inputStream, String savePath) throws IOException {

        File targetFile = new File(savePath);

        if (!targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs();
        }

        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.flush();
        }
    }

}
