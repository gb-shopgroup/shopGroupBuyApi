package cn.com.shopgroup.common.wxmini;

import cn.com.shopgroup.common.cache.RedisConstant;
import cn.com.shopgroup.common.cache.RedisHelper;
import cn.com.shopgroup.common.utils.TokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class WxMiniAccessTokenHelper {

    @Resource
    private RedisHelper redisHelper;


    public String getAccessToken(boolean isForce) {

        String key = RedisConstant.WxMiniAccessTokenKey;

        // 缓存命中且非强制刷新, 直接返回
        if (isForce == false && redisHelper.hasKey(key)) {
            return redisHelper.getCacheObject(key);
        }

        // 分布式锁: 保证同一时间只有一个实例向微信刷新access_token,
        // 避免多实例并发刷新导致token互相覆盖失效(微信返回40001 invalid credential not latest)
        String lockKey = RedisConstant.WxMiniAccessTokenLockKey;
        boolean locked = false;
        try {
            // 最多等待3秒尝试获取锁, 拿不到说明其他实例正在刷新, 稍等后复用其结果
            for (int i = 0; i < 12; i++) {
                if (redisHelper.getLock(lockKey, 20)) {
                    locked = true;
                    break;
                }
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            // 拿到锁后二次检查缓存(等待期间其他实例可能已刷新完成)
            if (isForce == false && redisHelper.hasKey(key)) {
                return redisHelper.getCacheObject(key);
            }

            // 调用微信稳定版接口刷新access_token
            Map<String, String> result = WxMiniProgramHelper.getAccessToken(isForce);
            String success = result.get("success");
            if (Integer.parseInt(success) == 0) {
                log.warn("获取微信access_token失败: {}", result.get("data"));
                return "";
            }
            String accessToken = result.get("data");
            redisHelper.setCacheObject(key, accessToken, RedisConstant.WxMiniAccessTokenExpired, TimeUnit.SECONDS);
            return accessToken;
        } finally {
            if (locked) {
                redisHelper.releaseLock(lockKey);
            }
        }
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
