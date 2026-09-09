package cn.com.shopgroup.interceptor;

import cn.com.shopgroup.common.cache.RedisConstant;
import cn.com.shopgroup.common.cache.RedisHelper;
import cn.com.shopgroup.common.utils.TokenUtils;
import cn.com.shopgroup.common.exception.TokenException;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TokenCheckInterceptor implements HandlerInterceptor {

    // 令牌标识
    private final static String header = "Authorization";

    // Redis辅助类
    private RedisHelper redisHelper;

    // 构造方法注入 Redis辅助类
    public TokenCheckInterceptor(RedisHelper helper){

        this.redisHelper = helper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 是否提交token
        String token = request.getHeader(header);
        if(token == null || token.length() <= 0){
            throw new TokenException("缺少token令牌");
        }
        // 解析token
        String key = TokenUtils.parseToken(token);
        if(key == null || key.length() <= 0){
            throw new TokenException("缺少token令牌");
        }
        // 验证key的合法性
        if(redisHelper.hasKey(RedisConstant.RedisSysAdminTokenKey + key) == false){
            throw new TokenException("非法token令牌");
        }
        return true;
    }

}
