package cn.com.shopgroup.controller;

import cn.com.shopgroup.common.cache.RedisConstant;
import cn.com.shopgroup.common.cache.RedisHelper;
import cn.com.shopgroup.common.exception.BusinessException;
import cn.com.shopgroup.common.utils.IpUtils;
import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.common.utils.TokenUtils;
import cn.com.shopgroup.exception.AdminErrorCodeEnum;
import cn.com.shopgroup.http.request.LoginRequest;
import cn.com.shopgroup.http.response.UserResponse;
import cn.com.shopgroup.user.model.GbSysUserInfo;
import cn.com.shopgroup.user.service.GbSysUserInfoService;
import com.google.code.kaptcha.Producer;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
public class AdminLoginController {

    @Resource
    private GbSysUserInfoService service;

    // Kaptcha生产者
    @Resource
    private Producer kaptchaProducer;

    // Redis工具类
    @Resource
    private RedisHelper redisHelper;

    // 获取后台登录图形验证码(Base64图片, 5分钟有效)
    @GetMapping("/admin/login/kaptcha")
    public JsonResult kaptcha() {

        // 生成验证码并将其存入Redis中(5分钟)
        String captchaKey = DigestUtils.md5DigestAsHex(IpUtils.getClientIp().getBytes());
        String captchaText = kaptchaProducer.createText();
        String key = RedisConstant.RedisSysAdminKaptchaKey + captchaKey;
        redisHelper.setCacheObject(key, captchaText, RedisConstant.RedisSysAdminKaptchaExpired, TimeUnit.SECONDS);

        // 生成验证码图片并转为 Base64 字符串格式
        String base64Image = "";
        BufferedImage image = kaptchaProducer.createImage(captchaText);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", baos);
            byte[] imageBytes = baos.toByteArray();
            base64Image = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            throw new BusinessException(AdminErrorCodeEnum.CAPTCHA_IMAGE_FAILED);
        }

        // 返回 Base64 图片
        return JsonResult.success("生成验证码图片成功", base64Image);
    }

    // 后台登录(验证码+账号密码, 返回token)
    @PostMapping("/admin/login/submit")
    public JsonResult login(@RequestBody LoginRequest request) {

        // 先检查验证码
        String captchaKey = DigestUtils.md5DigestAsHex(IpUtils.getClientIp().getBytes());
        String redisKey = RedisConstant.RedisSysAdminKaptchaKey + captchaKey;
        String captchaText = redisHelper.getCacheObject(redisKey);
        if (captchaText == null || captchaText.equalsIgnoreCase(request.getCode()) == false) {
            throw new BusinessException(AdminErrorCodeEnum.CAPTCHA_ERROR);
        }

        // 再查询系统用户
        GbSysUserInfo userInfo = service.getUserByUsername(request.getUsername());

        // 用户不存在
        if (userInfo == null) throw new BusinessException(AdminErrorCodeEnum.USER_NOT_EXIST);

        // 用户已关闭
        if (userInfo.getIsClose() == 1) throw new BusinessException(AdminErrorCodeEnum.USER_DISABLED);

        // 密码不正确
        String salt = "dxt2009";
        String pwd = request.getPassword() + salt;
        String md5 = DigestUtils.md5DigestAsHex(pwd.getBytes());
        if (md5.equalsIgnoreCase(userInfo.getPassWord()) == false) {
            throw new BusinessException(AdminErrorCodeEnum.PASSWORD_ERROR);
        }

        // 返回数据
        UserResponse response = new UserResponse();
        response.setName(userInfo.getTrueName());
        response.setAvatar(userInfo.getAvatar());

        // 生成token
        String uuid = UUID.randomUUID().toString();
        String key = DigestUtils.md5DigestAsHex(uuid.getBytes());
        response.setToken(TokenUtils.createToken(key));
        // 将token解密出key, 然后去Redis中获取管理员id值
        redisHelper.setCacheObject(RedisConstant.RedisSysAdminTokenKey + key, userInfo.getUserId(), RedisConstant.RedisSysAdminTokenExpired, TimeUnit.SECONDS);

        // 返回
        return JsonResult.success(response);
    }

}
