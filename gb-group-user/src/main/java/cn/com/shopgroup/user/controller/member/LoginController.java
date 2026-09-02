package cn.com.shopgroup.user.controller.member;

import cn.com.shopgroup.common.cache.RedisConstant;
import cn.com.shopgroup.common.cache.RedisHelper;
import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.common.utils.TokenUtils;
import cn.com.shopgroup.common.wxmini.WxMiniAccessTokenHelper;
import cn.com.shopgroup.common.wxmini.WxMiniProgramHelper;
import cn.com.shopgroup.user.http.request.MemberRequest;
import cn.com.shopgroup.user.http.response.LoginMemberResponse;
import cn.com.shopgroup.user.model.GbMemberInfo;
import cn.com.shopgroup.user.service.GbMemberInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
public class LoginController {

    // 记录日志
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @Resource
    private GbMemberInfoService service;

    @Resource
    private RedisHelper redisHelper;

    @Resource
    private WxMiniAccessTokenHelper helper;


    // 根据code获取手机号
    @GetMapping("/phone")
    public JsonResult getPhone(@RequestParam("code") String code) {

        // 统一获取AccessToken
        String accessToken = helper.getAccessToken(false);
        if (accessToken == null || accessToken.length() == 0) return JsonResult.fail("获取AccessToken失败");

        // 再根据access_token和code获取手机号
        Map<String, String> result = WxMiniProgramHelper.getPhoneNumber(accessToken, code);
        String success = result.get("success");
        String phone = result.get("data");
        if (Integer.parseInt(success) == 0) {
            if (phone.equalsIgnoreCase("access_token")) {
                // 微信小程序access_token失效了
                helper.removeAccessToken();
                return JsonResult.fail("请稍后重试");
            } else {
                // 返回错误
                return JsonResult.fail(phone);
            }
        } else {
            // 新用户返回手机号即可
            return JsonResult.success("查询成功", phone);
        }
    }

    // 根据code获取openid
    @GetMapping("/openid")
    public JsonResult getOpenId(@RequestParam("code") String code) {

        // 通过微信api获取openid
        Map<String, String> res = WxMiniProgramHelper.getOpenId(code);

        // 返回结果
        String data = res.get("data");
        String success = res.get("success");
        if (Integer.parseInt(success) == 0) {
            return JsonResult.fail(data);
        } else {
            return JsonResult.success("查询成功", data);
        }
    }

    // openid自动登录
    @GetMapping("/login")
    public JsonResult login(@RequestParam("openid") String openid) {

        // 根据openid查询用户
        GbMemberInfo result = service.getMiniMemberByOpenId(openid);
        if (result == null) return JsonResult.success();
        LoginMemberResponse data = new LoginMemberResponse(result);
        //if(data == null){ return JsonResult.success(); }

        // 生成token
        String token = TokenUtils.createToken(String.valueOf(result.getMemberId()));

        // 设置token
        data.setToken(token);

        // 记录登录态到Redis(30天有效), 用于退出登录时清除
        redisHelper.setCacheObject(RedisConstant.RedisMemberTokenKey + result.getMemberId(), token, RedisConstant.RedisMemberTokenExpired, TimeUnit.SECONDS);

        // 返回
        return JsonResult.success(data);
    }

    // 注册新用户
    @PostMapping("/reg")
    public JsonResult reg(@RequestBody MemberRequest request) {

        // 查询是否已经注册成功了
        String openid = request.getOpenid();
        GbMemberInfo results = service.getMiniMemberByOpenId(openid);
        if (results != null && results.getMemberId() > 0) {
            LoginMemberResponse data = new LoginMemberResponse(results);
            String token = TokenUtils.createToken(String.valueOf(results.getMemberId()));
            data.setToken(token);
            // 记录登录态到Redis(30天有效), 用于退出登录时清除
            redisHelper.setCacheObject(RedisConstant.RedisMemberTokenKey + results.getMemberId(), token, RedisConstant.RedisMemberTokenExpired, TimeUnit.SECONDS);
            return JsonResult.success(data);
        }

        // 注册用户
        GbMemberInfo data = new GbMemberInfo();
        data.setMobile(request.getMobile());
        data.setNickname(request.getName());
        data.setAvatar(request.getAvatar());
        data.setOpenid(request.getOpenid());
        data.setMapId(0l);
        data.setMapName(request.getMap());
        data.setLeaderId(request.getLeader());
        data.setIsolationId(0);
        data.setIsClose((byte) 0);
        data.setAddTime(TimeUtils.getTimeStamp());
        service.addMiniMember(data);
        Long memberId = data.getMemberId();

        // 生成token
        String token = TokenUtils.createToken(String.valueOf(memberId));

        // 记录登录态到Redis(30天有效), 用于退出登录时清除
        redisHelper.setCacheObject(RedisConstant.RedisMemberTokenKey + memberId, token, RedisConstant.RedisMemberTokenExpired, TimeUnit.SECONDS);

        // 响应数据
        LoginMemberResponse result = new LoginMemberResponse();
        //result.setId(memberId);
        result.setName(request.getName());
        result.setAvatar(request.getAvatar());
        result.setMobile(request.getMobile());
        result.setOpenid(request.getOpenid());
        result.setToken(token);
        return JsonResult.success(result);
    }

    // 退出登录(小程序调用): 清除服务端登录态, 小程序端需同步删除本地token
    @PostMapping("/logout")
    public JsonResult logout() {

        // 获取请求头中的token
        String token = TokenUtils.getToken();
        if (token == null || token.length() == 0) {
            return JsonResult.fail("token不存在");
        }

        // 解析token获取用户id
        String userId = TokenUtils.parseToken(token);
        if (userId == null || userId.length() == 0 || userId.matches("^[0-9]+$") == false) {
            return JsonResult.fail("用户不存在");
        }

        // 清除该用户在Redis中的登录态
        redisHelper.deleteObject(RedisConstant.RedisMemberTokenKey + userId);

        // 返回
        return JsonResult.success();
    }


}
