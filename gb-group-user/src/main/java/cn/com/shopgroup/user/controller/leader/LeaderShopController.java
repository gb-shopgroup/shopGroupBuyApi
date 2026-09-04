package cn.com.shopgroup.user.controller.leader;

import cn.com.shopgroup.common.cache.RedisConstant;
import cn.com.shopgroup.common.cache.RedisHelper;
import cn.com.shopgroup.common.config.UploadConfig;
import cn.com.shopgroup.common.utils.HuaWeiOBS;
import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.common.wxmini.WxMiniAccessTokenHelper;
import cn.com.shopgroup.common.wxmini.WxMiniProgramHelper;
import cn.com.shopgroup.user.http.request.ShopErCodeRequest;
import cn.com.shopgroup.user.http.request.ShopRequest;
import cn.com.shopgroup.user.http.response.ShopResponse;
import cn.com.shopgroup.user.model.GbOrgShopInfo;
import cn.com.shopgroup.user.service.GbOrgShopInfoService;
import cn.com.shopgroup.user.utils.RequestParamsUtils;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson2.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

@RestController
@Api(value = "团长端首页展示订单数据统计对象")
@RequestMapping("/user")
@Slf4j
public class LeaderShopController {

    @Resource
    private GbOrgShopInfoService shopInfoService;

    @Resource
    private RedisHelper redisHelper;

    @Resource
    private UploadConfig uploadConfig;

    @Resource
    private WxMiniAccessTokenHelper helper;

    @ApiOperation("查看店铺信息")
    @GetMapping("/leader/shop/info")
    public JsonResult getShopInfo() {
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            return JsonResult.fail("lid不存在");
        }
        return handleGetShop(leaderId);
    }

    private JsonResult handleGetShop(Long leaderId) {
        String redisKey = RedisConstant.RedisShopInfoKey + leaderId;
        // 命中缓存直接返回
        if (redisHelper.hasKey(redisKey)) {
            ShopResponse cacheData = null;
            try {
                cacheData = redisHelper.getCacheObject(redisKey);
            } catch (Exception e) {
                log.error("读取店铺缓存失败,key:{},重新查询数据库", redisKey, e);
            }
            if (cacheData != null) {
                return JsonResult.success(cacheData);
            }
            // 缓存数据异常(反序列化失败/空值), 清除脏缓存后重新查询
            redisHelper.deleteObject(redisKey);
        }

        // 查询数据库 ---获取店铺信息
        GbOrgShopInfo shopInfo = shopInfoService.getMiniLeaderShop(leaderId);
        if (ObjectUtils.isEmpty(shopInfo)) {
            return JsonResult.success("暂无店铺信息");
        }
        // 缓存起来
        ShopResponse data = new ShopResponse(shopInfo);
        try {
            redisHelper.setCacheObject(redisKey, data, RedisConstant.RedisShopInfoExpired, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("写店铺缓存失败,key:{}", redisKey, e);
        }
        return JsonResult.success(data);
    }

    // 修改店铺信息
    @PostMapping("/leader/shop/save")
    public JsonResult saveShop(@Validated @RequestBody ShopRequest request) {
        log.info("修改店铺信息.../leader/shop/save req:{}", JSON.toJSONString(request));
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            return JsonResult.fail("lid不存在");
        }
        GbOrgShopInfo shopInfo = shopInfoService.getInfoByLeaderAndShopId(leaderId, request.getShopId());
        log.info("修改店铺信息.先查询店铺结果shopInfo:{}", JSON.toJSONString(shopInfo));
        if (ObjectUtils.isEmpty(shopInfo)) {
            String msg = "shopId=" + request.getShopId() + ",leaderId=" + leaderId + "未查询相关店铺信息";
            return JsonResult.fail(msg);
        }
        // 保存店铺信息
        shopInfo.setShopBanner(request.getBanner());
        shopInfo.setShopName(request.getName());
        shopInfo.setShopShortName(request.getShortName());
        shopInfo.setShopMobile(request.getMobile());
        shopInfo.setShopInfo(request.getShopInfo());
        shopInfo.setShopCodeUrl(request.getShopCodeUrl());
        shopInfo.setShopLogo(request.getShopLogo());
        boolean flag = shopInfoService.updateShopInfo(shopInfo);
        // 返回
        if (flag) {
            // 删除店铺缓存
            String key = RedisConstant.RedisShopInfoKey + leaderId;
            redisHelper.deleteObject(key);
            return JsonResult.success();
        } else {
            return JsonResult.fail();
        }
    }

    // 修改店铺信息
    @PostMapping("/leader/shop/update")
    public JsonResult saveShop(@Validated @RequestBody ShopErCodeRequest request) {
        log.info("修改店铺信息.../leader/shop/update req:{}", JSON.toJSONString(request));
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            return JsonResult.fail("lid不存在");
        }
        GbOrgShopInfo shopInfo = shopInfoService.getInfoByLeaderAndShopId(leaderId, request.getShopId());
        if (ObjectUtils.isEmpty(shopInfo)) {
            String msg = "shopId=" + request.getShopId() + ",leaderId=" + leaderId + "未查询相关店铺信息";
            return JsonResult.fail(msg);
        }
        // 保存店铺信息
        shopInfo.setShopCodeUrl(request.getShopUrl());
        boolean flag = shopInfoService.updateShopInfo(shopInfo);
        // 返回
        if (flag) {
            // 删除店铺缓存
            String key = RedisConstant.RedisShopInfoKey + leaderId;
            redisHelper.deleteObject(key);
            return JsonResult.success();
        } else {
            return JsonResult.fail();
        }
    }

    // 通过leaderId团长店铺详情
    @GetMapping("/leader/getGroup/shop")
    public JsonResult getGroupShop(@RequestParam("leaderId") Long leaderId) {
        return handleGetShop(leaderId);
    }

    // 团长-我的店铺小程序码,上传到服务器返回URL
    @PostMapping("/leader/shop/makeQrCode")
    public JsonResult shopMakeQrcode(@RequestParam("shopId") Long shopId) {
        log.info("【团长-我的店铺二维码生成】/leader/shop/makeQrCode shopId:{}", shopId);
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            return JsonResult.fail("lid不存在");
        }
        GbOrgShopInfo shopInfo = shopInfoService.getByShopId(shopId);
        if (ObjectUtils.isEmpty(shopInfo)) {
            log.error("生成二维码失败,未查询到相关店铺信息" + shopId);
            return JsonResult.fail("shopId=" + shopId + "二维码生成失败");
        }
        if (StringUtil.isNotEmpty(shopInfo.getShopCodeUrl())) {
            log.error("二维码已经存在");
            return JsonResult.fail("二维码已经存在，二维码生成失败");
        }

        // 统一获取AccessToken
        String accessToken = helper.getAccessToken(false);
        if (accessToken == null || accessToken.length() == 0) return JsonResult.fail("获取AccessToken失败");

        // 生成小程序码图片
        byte[] bytes;
        try {
            // 小程序码落地页与scene参数: 需与小程序前端onLoad解析保持一致
            String page = "pages/group/index";
            String scene = "lid=" + leaderId;
            int wh = 640; // 图片像素(最高1280像素)
            BufferedImage qrImg = WxMiniProgramHelper.getMiniProgramPageERcodeBufferedImage(accessToken, page, scene, wh);
            if (qrImg == null) {
                log.error("生成小程序码失败,微信返回为空");
                return JsonResult.fail("二维码生成失败");
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImg, "png", baos);
            bytes = baos.toByteArray();
        } catch (Exception e) {
            log.error("生成二维码失败：" + e.getMessage());
            return JsonResult.fail("二维码生成失败");
        }
        // 判断本地上传还是云存储上传: # 1=本地上传, 2=云端上传
        int type = uploadConfig.getType();
        String domain = uploadConfig.getDomain();
        String rootPath = uploadConfig.getPath();

        // 文件名称
        String obsKey = "shop/" + TimeUtils.getTodayStr() + "/" + UUID.randomUUID() + ".png";

        // 区分本地上传还是云端上传
        try {
            if (type == 1) {
                // 本地上传: 创建目录并写入图片文件
                File fileFolder = new File(rootPath + File.separator + "shop" + File.separator + TimeUtils.getTodayStr());
                if (!fileFolder.exists()) fileFolder.mkdirs();
                FileOutputStream fos = new FileOutputStream(rootPath + "/" + obsKey);
                fos.write(bytes);
                fos.close();
            } else {
                // 云端上传
                HuaWeiOBS.upload(obsKey, new ByteArrayInputStream(bytes));
            }
        } catch (Exception e) {
            log.error("上传二维码失败：" + e.getMessage());
            e.printStackTrace();
            return JsonResult.fail("二维码上传失败");
        }

        // 获取图片访问路径
        String url = domain + "/" + obsKey;
        shopInfo.setShopCodeUrl(url);
        shopInfoService.updateShopInfo(shopInfo);
        // 删除店铺缓存, 保证 /leader/shop/info 等接口能查到最新店铺码
        redisHelper.deleteObject(RedisConstant.RedisShopInfoKey + leaderId);
        // 返回访问路径
        return JsonResult.success("二维码生成成功", url);
    }


}
