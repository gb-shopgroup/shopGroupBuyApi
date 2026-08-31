package cn.com.shopgroup.user.controller.leader;

import cn.com.shopgroup.common.cache.RedisConstant;
import cn.com.shopgroup.common.cache.RedisHelper;
import cn.com.shopgroup.common.config.UploadConfig;
import cn.com.shopgroup.common.utils.HuaWeiOBS;
import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.user.http.request.ShopErCodeRequest;
import cn.com.shopgroup.user.http.request.ShopRequest;
import cn.com.shopgroup.user.http.response.ShopResponse;
import cn.com.shopgroup.user.model.GbOrgShopInfo;
import cn.com.shopgroup.user.service.GbOrgShopInfoService;
import cn.com.shopgroup.user.utils.QRCodeUtil;
import cn.com.shopgroup.user.utils.RequestParamsUtils;
import com.alibaba.csp.sentinel.util.StringUtil;
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
import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
        if (redisHelper.hasKey(redisKey) == false) {
            // 查询数据库 ---获取店铺信息
            GbOrgShopInfo shopInfo = shopInfoService.getMiniLeaderShop(leaderId);
            if (ObjectUtils.isEmpty(shopInfo)) {
                return JsonResult.success("暂无店铺信息");
            }
            // 缓存起来
            redisHelper.setCacheObject(redisKey, shopInfo, RedisConstant.RedisShopInfoExpired, TimeUnit.SECONDS);
            ShopResponse data = new ShopResponse(shopInfo);
            return JsonResult.success(data);
        }

        // 读取缓存数据
        ShopResponse data = redisHelper.getCacheObject(redisKey);
        return JsonResult.success(data);
    }

    // 修改店铺信息
    @PostMapping("/leader/shop/save")
    public JsonResult saveShop(@Validated @RequestBody ShopRequest request) {

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
        shopInfo.setShopBanner(request.getBanner());
        shopInfo.setShopName(request.getName());
        shopInfo.setShopMobile(request.getMobile());
        shopInfo.setShopInfo(request.getShopInfo());
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

    // 团长-我的店铺二维码,上传到服务器返回URL
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

        // 生成二维码图片
        byte[] bytes;
        try {
            BufferedImage qrImg = QRCodeUtil.createQRCode(String.valueOf(shopId), 640, 640);
            bytes = QRCodeUtil.imageToBytes(qrImg, "png");
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
        // 返回访问路径
        return JsonResult.success("二维码生成成功", url);
    }


}
