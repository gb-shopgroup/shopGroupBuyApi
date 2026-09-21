package cn.com.shopgroup.user.controller.leader;

import cn.com.shopgroup.common.cache.RedisConstant;
import cn.com.shopgroup.common.cache.RedisHelper;
import cn.com.shopgroup.common.config.UploadConfig;
import cn.com.shopgroup.common.exception.BusinessException;
import cn.com.shopgroup.common.utils.HuaWeiOBS;
import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.common.wxmini.WxMiniAccessTokenHelper;
import cn.com.shopgroup.user.exception.UserErrorCodeEnum;
import cn.com.shopgroup.user.http.request.PointRequest;
import cn.com.shopgroup.user.http.response.PointResponse;
import cn.com.shopgroup.user.model.GbOrgPointInfo;
import cn.com.shopgroup.user.service.GbOrgPointInfoService;
import cn.com.shopgroup.user.utils.RequestParamsUtils;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Optional;
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
    private WxMiniAccessTokenHelper helper;

    @Resource
    private RedisHelper redisHelper;

    // 团长端-查询提货点列表
    @GetMapping("/list")
    public JsonResult pointList(@RequestParam(value = "name", required = false) String name) {
        log.info("[get] /user/leader/point/list name:{}", name);
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            throw new BusinessException(UserErrorCodeEnum.LEADER_NOT_EXIST);
        }
        // 无搜索词的全量列表才走缓存:
        // 1) 搜索词组合无法枚举, 写操作时无法逐一失效, 缓存易脏且命中率低, 带name直接查库;
        // 2) 此处不过滤is_close(管理端要看含已作废的全部), 与C端RedisPointListKey(is_close=0)口径不同, 用独立key
        String key = RedisConstant.RedisLeaderPointListKey + leaderId;
        if (StringUtils.isEmpty(name)) {
            List<GbOrgPointInfo> cached = redisHelper.getCacheObject(key);
            if (!ObjectUtils.isEmpty(cached)) {
                return JsonResult.success(PointResponse.getPointResponseList(cached));
            }
        }
        // 查询数据库
        List<GbOrgPointInfo> result = service.getPointListForLeader(leaderId, name);
        if (CollectionUtils.isEmpty(result)) {
            return JsonResult.success();
        }
        // 全量列表回填缓存(空结果不缓存, 避免刚清库的团长30天内看不到新增的提货点)
        if (StringUtils.isEmpty(name)) {
            redisHelper.setCacheObject(key, result, RedisConstant.RedisLeaderPointListExpired, TimeUnit.SECONDS);
        }
        List<PointResponse> data = PointResponse.getPointResponseList(result);
        return JsonResult.success(data);
    }

    // 提货点任一写操作后失效缓存: C端列表(仅is_close=0) + 管理端全量列表
    private void deletePointListCache(Long leaderId) {
        redisHelper.deleteObject(RedisConstant.RedisPointListKey + leaderId);
        redisHelper.deleteObject(RedisConstant.RedisLeaderPointListKey + leaderId);
    }

    // 提货点详情写操作(edit/close/二维码生成)后失效详情缓存
    private void deletePointInfoCache(Long pointId) {
        redisHelper.deleteObject(RedisConstant.RedisPointInfoKey + pointId);
    }

    // 查提货点详情(走缓存): /info 与 /ercode 共用, miss 时查库并回填
    private GbOrgPointInfo getPointInfoWithCache(Long pointId) {
        String key = RedisConstant.RedisPointInfoKey + pointId;
        GbOrgPointInfo pointInfo = redisHelper.getCacheObject(key);
        if (!ObjectUtils.isEmpty(pointInfo)) {
            return pointInfo;
        }
        pointInfo = service.getPointInfo(pointId);
        if (ObjectUtils.isEmpty(pointInfo)) {
            return null;
        }
        redisHelper.setCacheObject(key, pointInfo, RedisConstant.RedisPointInfoExpired, TimeUnit.SECONDS);
        return pointInfo;
    }

    //添加团购活动时，调用查询提货点列表
    @GetMapping("/addGroup/list")
    public JsonResult addGroupPointList() {
        log.info("[get] /user/leader/point/addGroup/list");
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            throw new BusinessException(UserErrorCodeEnum.LEADER_NOT_EXIST);
        }
        // 复用 getGroupPoint 的缓存(RedisPointListKey):
        // 其底层数据源 getMiniPointList 与本接口原 getMiniLeaderPointList 的查询条件完全一致
        // (leaderId + is_close=0 + orderByAsc(pointId) + limit 20), 且 add/edit/close 已统一失效该缓存
        List<PointResponse> data = service.getGroupPoint(leaderId);
        if (CollectionUtils.isEmpty(data)) {
            return JsonResult.success();
        }
        return JsonResult.success(data);
    }


    // 添加提货点
    @PostMapping("/add")
    public JsonResult addPoint(@Validated @RequestBody PointRequest request) {
        log.info("添加自提点信息req:{}", JSON.toJSONString(request));
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) throw new BusinessException(UserErrorCodeEnum.LEADER_NOT_EXIST);

        // 添加信息
        GbOrgPointInfo data = new GbOrgPointInfo();
        data.setPointName(request.getName());
        data.setPointAddress(request.getAddress());
        data.setPointImg(request.getImg());
        data.setLongitude(request.getLon());
        data.setLatitude(request.getLat());
        //如果不传默认20公里
        Integer pointScope = Optional.ofNullable(request.getScope()).orElse(20);
        data.setPointScope(pointScope);
        data.setPointInfo(request.getInfo());
        data.setPerson(request.getPerson());
        data.setPhone(request.getPhone());
        data.setLeaderId(request.getLeaderId());
        Long pointId = service.addMiniLeaderPoint(leaderId, data);

        // 删除提货点列表缓存(30天长缓存, 不失效则一直读到旧列表)
        deletePointListCache(leaderId);
        // 返回成功
        return JsonResult.success("添加成功", pointId);
    }

    // 修改提货点
    @PostMapping("/edit")
    public JsonResult editPoint(@Validated @RequestBody PointRequest request) {
        log.info("修改提货点信息req:{}", JSON.toJSONString(request));
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            throw new BusinessException(UserErrorCodeEnum.LEADER_NOT_EXIST);
        }
        // 提货点id必传(是更新的定位条件)
        if (request.getId() == null || request.getId() < 1) {
            throw new BusinessException(UserErrorCodeEnum.REQUEST_PARAM_ILLEGAL);
        }

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
        // 修复: 补传联系人/电话(原来漏传, 且 service 层曾把它们当 WHERE 条件, 导致更新永不命中)
        data.setPerson(request.getPerson());
        data.setPhone(request.getPhone());
        boolean flag = service.editMiniLeaderPoint(leaderId, data);
        if (!flag) {
            // 提货点不存在 / 不属于当前团长 / 无可更新字段
            log.warn("修改提货点失败, leaderId:{}, pointId:{}", leaderId, request.getId());
            throw new BusinessException(UserErrorCodeEnum.DATA_NOT_FOUND);
        }

        // 修改成功后删除提货点列表缓存, 避免 C 端与管理端 30 天内一直读旧数据
        deletePointListCache(leaderId);
        // 详情缓存同步失效
        deletePointInfoCache(request.getId());
        return JsonResult.success("修改成功");
    }

    // 作废/恢复 提货点
    @GetMapping("/close")
    public JsonResult closePoint(@RequestParam("id") Long pointId) {

        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) throw new BusinessException(UserErrorCodeEnum.LEADER_NOT_EXIST);

        // 查询旧的
        GbOrgPointInfo pointInfo = service.getPointInfo(pointId);
        if (ObjectUtils.isEmpty(pointInfo)) {
            throw new BusinessException(UserErrorCodeEnum.DATA_NOT_FOUND);
        }
        boolean flag;
        if (pointInfo.getIsClose() == 0) {
            flag = service.closeMiniLeaderPoint(leaderId, pointId, 1);
        } else {
            flag = service.closeMiniLeaderPoint(leaderId, pointId, 0);
        }
        if (!flag) {
            // 提货点不存在 / 不属于当前团长
            log.warn("作废/恢复提货点失败, leaderId:{}, pointId:{}", leaderId, pointId);
            throw new BusinessException(UserErrorCodeEnum.DATA_NOT_FOUND);
        }

        // 作废/恢复都会改变 C 端与管理端的自提点列表, 同步删除提货点列表缓存
        deletePointListCache(leaderId);
        // 详情缓存同步失效(is_close 已变化)
        deletePointInfoCache(pointId);
        // 返回
        return JsonResult.success();
    }

    //查询提货点详情
    @GetMapping("/info")
    public JsonResult getPointInfo(@RequestParam("pointId") Long pointId) {
        log.info("[get] /user/leader/point/info pointId:{}", pointId);
        if (pointId == null || pointId < 1) {
            throw new BusinessException(UserErrorCodeEnum.REQUEST_PARAM_ILLEGAL);
        }
        // 先读缓存再查库(miss 时回填)
        GbOrgPointInfo pointInfo = getPointInfoWithCache(pointId);
        if (ObjectUtils.isEmpty(pointInfo)) {
            return JsonResult.success();
        }
        PointResponse data = new PointResponse(pointInfo);
        return JsonResult.success(data);
    }


    // 提货点二维码
    @GetMapping("/ercode")
    public JsonResult ercode(@RequestParam("id") Long pointId) {

        // 查询二维码是否已经生成过(走详情缓存)
        GbOrgPointInfo pointInfo = getPointInfoWithCache(pointId);
        if (ObjectUtils.isEmpty(pointInfo)) {
            // 修复: 原代码 pointId 不存在时直接 getPointErcode() 会 NPE
            throw new BusinessException(UserErrorCodeEnum.DATA_NOT_FOUND);
        }
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

        // 再获取二维码(access_token失效时自动强刷重试一次, 真实失败原因会记录日志)
        String page = "pages/order/index";
        String scene = "pid=" + pointId;
        int wh = 1280; // 图片像素(最高1280像素)
        byte[] qrBytes = helper.getWxaCodeUnlimitWithRetry(page, scene, wh);
        if (ObjectUtils.isEmpty(qrBytes)) {
            log.error("生成提货点小程序码失败 pointId:{}", pointId);
            throw new BusinessException(UserErrorCodeEnum.QRCODE_GEN_FAILED);
        }

        // 区分本地上传还是云端上传
        try {
            if (type == 1) {
                FileOutputStream fos = new FileOutputStream(rootPath + "/" + obsKey);
                fos.write(qrBytes);
                fos.close();
            } else {
                HuaWeiOBS.upload(obsKey, new ByteArrayInputStream(qrBytes));
            }
        } catch (Exception e) {
            log.error("上传提货点二维码失败 pointId:{}", pointId, e);
            throw new BusinessException(UserErrorCodeEnum.QRCODE_UPLOAD_FAILED);
        }

        // 获取图片访问路径
        String url = domain + "/" + obsKey;

        // 同步修改用户数据表(小程序码永久有效, 生成一次后库里地址长期复用)
        service.updatePointErcode(pointId, url);
        // point_ercode 已更新到库, 删除详情缓存让下次读取回源拿到新二维码地址
        deletePointInfoCache(pointId);

        // 返回访问路径
        return JsonResult.success("查询成功", url);
    }


}
