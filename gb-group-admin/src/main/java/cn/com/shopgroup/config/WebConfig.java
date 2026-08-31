package cn.com.shopgroup.config;

import cn.com.shopgroup.common.cache.RedisHelper;
import cn.com.shopgroup.interceptor.TokenCheckInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private RedisHelper redisHelper;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // 向 TokenCheckInterceptor 拦截器中注入 RedisHelper
        registry.addInterceptor(new TokenCheckInterceptor(redisHelper)).addPathPatterns("/admin/**").excludePathPatterns("/admin/login/**");
    }

}
