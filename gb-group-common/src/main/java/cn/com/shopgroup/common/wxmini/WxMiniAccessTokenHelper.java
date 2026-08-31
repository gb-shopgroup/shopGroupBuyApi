package cn.com.shopgroup.common.wxmini;

import cn.com.shopgroup.common.cache.RedisConstant;
import cn.com.shopgroup.common.cache.RedisHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class WxMiniAccessTokenHelper {

    @Autowired
    private RedisHelper redisHelper;


    public String getAccessToken(boolean isForce){


        String key = RedisConstant.WxMiniAccessTokenKey;
        if(isForce == true || redisHelper.hasKey(key) == false){
            Map<String,String> result = WxMiniProgramHelper.getAccessToken();
            String success = result.get("success");
            if(Integer.parseInt(success) == 0){
                return "";
            }else{
                String accessToken = result.get("data");
                redisHelper.setCacheObject(key, accessToken, RedisConstant.WxMiniAccessTokenExpired, TimeUnit.SECONDS);
            }
        }

 
        return redisHelper.getCacheObject(key);
    }


    public void removeAccessToken(){

        redisHelper.deleteObject(RedisConstant.WxMiniAccessTokenKey);
    }

}
