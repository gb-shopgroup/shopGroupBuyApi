package cn.com.shopgroup.common.wxmini;

import cn.com.shopgroup.common.cache.RedisConstant;
import cn.com.shopgroup.common.cache.RedisHelper;
import cn.com.shopgroup.common.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class WxMiniAccessTokenHelper {

    @Resource
    private RedisHelper redisHelper;


    public String getAccessToken(boolean isForce) {


        String key = RedisConstant.WxMiniAccessTokenKey;
        if (isForce == true || redisHelper.hasKey(key) == false) {
            Map<String, String> result = WxMiniProgramHelper.getAccessToken();
            String success = result.get("success");
            if (Integer.parseInt(success) == 0) {
                return "";
            } else {
                String accessToken = result.get("data");
                redisHelper.setCacheObject(key, accessToken, RedisConstant.WxMiniAccessTokenExpired, TimeUnit.SECONDS);
            }
        }


        return redisHelper.getCacheObject(key);
    }


    public void removeAccessToken() {

        redisHelper.deleteObject(RedisConstant.WxMiniAccessTokenKey);
    }

    //判断小程序用户的token是否失效
    // 校验微信小程序用户token是否失效, 有效返回true
    // 失效场景: token解析失败/非数字用户id/未登录(Redis无登录态)/登录态已变更(退出登录或重新登录)
    public boolean checkUserTokenValid(String userToken) {

        // token为空
        if (userToken == null || userToken.length() == 0) {
            return false;
        }

        // 解析token获取用户id
        String userId = TokenUtils.parseToken(userToken);
        if (userId == null || userId.length() == 0 || userId.matches("^[0-9]+$") == false) {
            return false;
        }

        // 与Redis中的登录态比对(登录/注册时写入, 退出登录清除, 重新登录覆盖, 30天过期)
        Object cacheToken = redisHelper.getCacheObject(RedisConstant.RedisMemberTokenKey + userId);
        if (cacheToken == null) {
            return false;
        }
        return userToken.equals(cacheToken);
    }

}
