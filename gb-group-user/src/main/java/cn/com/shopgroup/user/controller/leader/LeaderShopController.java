package cn.com.shopgroup.user.controller.leader;

import cn.com.shopgroup.common.cache.RedisConstant;
import cn.com.shopgroup.common.cache.RedisHelper;
import cn.com.shopgroup.common.config.UploadConfig;
import cn.com.shopgroup.common.exception.BusinessException;
import cn.com.shopgroup.common.utils.HuaWeiOBS;
import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.common.wxmini.WxMiniAccessTokenHelper;
import cn.com.shopgroup.common.wxmini.WxMiniProgramHelper;
import cn.com.shopgroup.user.exception.UserErrorCodeEnum;
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
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@Api(value = "团长端-店铺管理")
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
            throw new BusinessException(UserErrorCodeEnum.LEADER_NOT_EXIST);
        }
        return handleGetShop(leaderId);
    }

    private JsonResult handleGetShop(Long leaderId) {
        String redisKey = RedisConstant.RedisShopInfoKey + leaderId;
        if (redisHelper.hasKey(redisKey) == false) {
            // 查询数据库 ---获取店铺信息
            GbOrgShopInfo shopInfo = shopInfoService.getMiniLeaderShop(leaderId);
            if (ObjectUtils.isEmpty(shopInfo)) {
                return JsonResult.success(UserErrorCodeEnum.DATA_NOT_FOUND.getMessage());
            }
            // 缓存起来
            ShopResponse data = new ShopResponse(shopInfo);
            redisHelper.setCacheObject(redisKey, data, RedisConstant.RedisShopInfoExpired, TimeUnit.SECONDS);
            return JsonResult.success(data);
        }

        // 读取缓存数据
        ShopResponse data = redisHelper.getCacheObject(redisKey);
        return JsonResult.success(data);
    }

    // 修改/保存-店铺信息
    @PostMapping("/leader/shop/save")
    public JsonResult saveShop(@Validated @RequestBody ShopRequest request) {
        log.info("修改店铺信息.../leader/shop/save req:{}", JSON.toJSONString(request));
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            throw new BusinessException(UserErrorCodeEnum.LEADER_NOT_EXIST);
        }
        GbOrgShopInfo shopInfo = new GbOrgShopInfo();
        Long shopId = request.getShopId() == null ? 0L : request.getShopId();
        if (shopId.intValue() > 0) {
            //shopInfo = shopInfoService.getInfoByLeaderAndShopId(leaderId, request.getShopId());
            //目前一个团长只有一个店铺，后续需求变动，再根据情况处理
            shopInfo = shopInfoService.getMiniLeaderShop(leaderId);
            log.info("修改店铺信息.先查询店铺结果shopInfo:{}", JSON.toJSONString(shopInfo));
            if (ObjectUtils.isEmpty(shopInfo)) {
                String msg = "leaderId=" + leaderId + UserErrorCodeEnum.DATA_NOT_FOUND.getMessage();
                throw new BusinessException(msg);
            }
        }
        // 保存店铺信息
        shopInfo.setShopBanner(request.getBanner());
        shopInfo.setShopName(request.getName());
        shopInfo.setShopMobile(request.getMobile());
        shopInfo.setShopInfo(request.getShopInfo());
        shopInfo.setShopCodeUrl(request.getShopCodeUrl());
        shopInfo.setShopLogo(request.getShopLogo());
        shopInfo.setShopShortName(request.getShortName());
        boolean flag = false;
        if (shopId.intValue() > 0) {
            flag = shopInfoService.updateShopInfo(shopInfo);
        } else {
            flag = shopInfoService.addShopInfo(shopInfo);
        }

        // 返回
        if (flag) {
            // 删除店铺缓存
            String key = RedisConstant.RedisShopInfoKey + leaderId;
            redisHelper.deleteObject(key);
            return JsonResult.success();
        } else {
            throw new BusinessException(UserErrorCodeEnum.UPDATE_FAILED);
        }
    }

    // 更新店铺码图片地址（保存店铺二维码上传后的访问URL）
    @PostMapping("/leader/shop/update")
    public JsonResult saveShop(@Validated @RequestBody ShopErCodeRequest request) {
        log.info("修改店铺信息.../leader/shop/update req:{}", JSON.toJSONString(request));
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            throw new BusinessException(UserErrorCodeEnum.LEADER_NOT_EXIST);
        }
        GbOrgShopInfo shopInfo = shopInfoService.getInfoByLeaderAndShopId(leaderId, request.getShopId());
        if (ObjectUtils.isEmpty(shopInfo)) {
            String msg = "shopId=" + request.getShopId()
                    + ",leaderId=" + leaderId + UserErrorCodeEnum.DATA_NOT_FOUND.getMessage();
            throw new BusinessException(msg);
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
            throw new BusinessException(UserErrorCodeEnum.UPDATE_FAILED);
        }
    }

    // 通过leaderId团长店铺详情
    @GetMapping("/leader/getGroup/shop")
    public JsonResult getGroupShop(@RequestParam("leaderId") Long leaderId) {
        return handleGetShop(leaderId);
    }

    // 团长-我的店铺小程序码(微信小程序码,page=pages/order/index,scene=shopId=xx),上传到服务器返回URL
    @PostMapping("/leader/shop/makeQrCode")
    public JsonResult shopMakeQrcode(@RequestParam("shopId") Long shopId) {
        log.info("【团长-我的店铺二维码生成】/leader/shop/makeQrCode shopId:{}", shopId);
        // 从请求头中获取团长id
        Long leaderId = RequestParamsUtils.getRequestHeaderLeaderId();
        if (leaderId == 0) {
            throw new BusinessException(UserErrorCodeEnum.LEADER_NOT_EXIST);
        }
        GbOrgShopInfo shopInfo = shopInfoService.getByShopId(shopId);
        if (ObjectUtils.isEmpty(shopInfo)) {
            log.error("生成二维码失败,未查询到相关店铺信息" + shopId);
            throw new BusinessException("shopId=" + shopId + UserErrorCodeEnum.QRCODE_GEN_FAILED.getMessage());
        }
        if (StringUtil.isNotEmpty(shopInfo.getShopCodeUrl())) {
            log.error("二维码已经存在");
            throw new BusinessException(UserErrorCodeEnum.QRCODE_EXISTED);
        }

        // 统一获取AccessToken
        String accessToken = helper.getAccessToken(false);
        if (StringUtil.isEmpty(accessToken)) {
            throw new BusinessException(UserErrorCodeEnum.ACCESS_TOKEN_FAILED);
        }

        // 生成店铺小程序码图片(与 /order/group/order/makeErcode 一致,走微信小程序码接口)
        byte[] bytes;
        try {
            // 小程序码落地页与scene参数: 需与小程序前端onLoad解析保持一致
            String page = "pages/order/index";
            String scene = "shopId=" + shopId;
            int wh = 1280; // 图片像素(最高1280像素)
            BufferedImage qrImg = WxMiniProgramHelper.getMiniProgramPageERcodeBufferedImage(accessToken, page, scene, wh);
            if (ObjectUtils.isEmpty(qrImg)) {
                log.error("生成店铺小程序码失败,微信返回空图片 shopId:{}", shopId);
                throw new BusinessException(UserErrorCodeEnum.QRCODE_GEN_FAILED);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImg, "png", baos);
            bytes = baos.toByteArray();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("生成二维码失败：" + e.getMessage());
            throw new BusinessException(UserErrorCodeEnum.QRCODE_GEN_FAILED);
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
            throw new BusinessException(UserErrorCodeEnum.QRCODE_UPLOAD_FAILED);
        }

        // 获取图片访问路径
        String url = domain + "/" + obsKey;
        shopInfo.setShopCodeUrl(url);
        shopInfoService.updateShopInfo(shopInfo);
        // 返回访问路径
        return JsonResult.success("二维码生成成功", url);
    }


}
