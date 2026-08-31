package cn.com.shopgroup.user.controller.leader;

import cn.com.shopgroup.common.cache.RedisConstant;
import cn.com.shopgroup.common.cache.RedisHelper;
import cn.com.shopgroup.common.config.UploadConfig;
import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.common.wxmini.WxMiniAccessTokenHelper;
import cn.com.shopgroup.common.wxmini.WxMiniProgramHelper;
import cn.com.shopgroup.user.http.request.PointRequest;
import cn.com.shopgroup.user.http.response.PointResponse;
import cn.com.shopgroup.user.model.GbOrgPointInfo;
import cn.com.shopgroup.user.service.GbOrgPointInfoService;
import cn.com.shopgroup.user.utils.RequestParamsUtils;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user/leader/point")
@Slf4j
public class LeaderPointController {

    @Resource
    private GbOrgPointInfoService service;

    @Resource
    private UploadConfig uploadConfig;

    @Resource
    private RedisHelper redisHelper;

    @Resource
    private WxMiniAccessTokenHelper helper;

    // 查询提货点列表
    @GetMapping("/list")
    public JsonResult pointList() {
        log.info("[get] /user/leader/point/list");
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            return JsonResult.fail("lid不存在");
        }  // 先读缓存再读数据库
        String key = RedisConstant.RedisPointListKey + leaderId;
        if (redisHelper.hasKey(key) == false) {
            // 查询数据库
            List<GbOrgPointInfo> result = service.getMiniLeaderPointList(leaderId);
            log.info("查询提货点列表 leaderId:{},rest:{}",leaderId, JSON.toJSONString(result));
            if(CollectionUtils.isEmpty(result)){
                return JsonResult.success();
            }
            List<PointResponse> data = PointResponse.getPointResponseList(result);
            // 缓存到Redis
            redisHelper.setCacheObject(key, data, RedisConstant.RedisPointListExpired, TimeUnit.SECONDS);
            return JsonResult.success(data);
        }

        // 读取数据库
        List<PointResponse> data = redisHelper.getCacheObject(key);
        return JsonResult.success(data);
    }


    // 添加提货点
    @PostMapping("/add")
    public JsonResult addPoint(@Validated @RequestBody PointRequest request) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) return JsonResult.fail("lid不存在");

        // 添加信息
        GbOrgPointInfo data = new GbOrgPointInfo();
        data.setPointName(request.getName());
        data.setPointAddress(request.getAddress());
        data.setPointImg(request.getImg());
        data.setLongitude(request.getLon());
        data.setLatitude(request.getLat());
        data.setPointScope(request.getScope());
        data.setPointInfo(request.getInfo());
        data.setPerson(request.getPerson());
        data.setPhone(request.getPhone());
        Long pointId = service.addMiniLeaderPoint(leaderId, data);

        // 删除提货点缓存
        String key = RedisConstant.RedisPointListKey + leaderId;
        redisHelper.deleteObject(key);

        // 返回成功
        return JsonResult.success("添加成功", pointId);
    }

    // 修改提货点
    @PostMapping("/edit")
    public JsonResult editPoint(@Validated @RequestBody PointRequest request) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) return JsonResult.fail("lid不存在");

        // 修改信息
        GbOrgPointInfo data = new GbOrgPointInfo();
        data.setPointId(request.getId());
        data.setPointName(request.getName());
        data.setPointAddress(request.getAddress());
        data.setPointImg(request.getImg());
        data.setLongitude(request.getLon());
        data.setLatitude(request.getLat());
        data.setPointScope(request.getScope());
        data.setPointInfo(request.getInfo());
        boolean flag = service.editMiniLeaderPoint(leaderId, data);

        // 删除提货点缓存
        String key = RedisConstant.RedisPointListKey + leaderId;
        redisHelper.deleteObject(key);

        // 返回
        return JsonResult.success();
    }

    // 关闭提货点
    @GetMapping("/close")
    public JsonResult closePoint(@RequestParam("id") Long pointId) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) return JsonResult.fail("lid不存在");

        // 查询旧的
        GbOrgPointInfo pointInfo = service.getPointInfo(pointId);
        if (pointInfo.getIsClose() == 0) {
            service.closeMiniLeaderPoint(leaderId, pointId, 1);
        } else {
            service.closeMiniLeaderPoint(leaderId, pointId, 0);
        }

        // 删除提货点缓存
        String key = RedisConstant.RedisPointListKey + leaderId;
        redisHelper.deleteObject(key);

        // 返回
        return JsonResult.success();
    }

    // 提货点二维码
    @GetMapping("/ercode")
    public JsonResult ercode(@RequestParam("id") Long pointId) {

        // 查询二维码是否已经生成过
        GbOrgPointInfo pointInfo = service.getPointInfo(pointId);
        String ercode = pointInfo.getPointErcode();
        if (ercode != null && ercode.length() > 0) {
            return JsonResult.success("查询成功", ercode);
        }

        // 判断本地上传还是云存储上传: # 1=本地上传, 2=云端上传
        int type = uploadConfig.getType();
        String domain = uploadConfig.getDomain();
        String rootPath = uploadConfig.getPath();

        // 文件名称
        String obsKey = "point/" + TimeUtils.getTodayStr() + "/" + UUID.randomUUID() + ".png";

        // 本地路径创建
        if (type == 1) {
            File fileFolder = new File(rootPath + File.separator + "point" + File.separator + TimeUtils.getTodayStr());
            if (!fileFolder.exists()) fileFolder.mkdirs();
        }

        // 统一获取AccessToken
        String accessToken = helper.getAccessToken(false);
        if (accessToken == null || accessToken.length() == 0) return JsonResult.fail("获取AccessToken失败");

        // 再获取二维码
        String page = "pages/order/index";
        String scene = "pid=" + pointId;
        int wh = 1280; // 图片像素(最高1280像素)

        // 区分本地上传还是云端上传
        if (type == 1) {
            WxMiniProgramHelper.getMiniProgramPageERcode(accessToken, page, scene, rootPath + "/" + obsKey, wh, type);
        } else {
            WxMiniProgramHelper.getMiniProgramPageERcode(accessToken, page, scene, obsKey, wh, type);
        }

        // 获取图片访问路径
        String url = domain + "/" + obsKey;

        // 同步修改用户数据表
        service.updatePointErcode(pointId, url);

        // 返回访问路径
        return JsonResult.success("查询成功", url);
    }


}
