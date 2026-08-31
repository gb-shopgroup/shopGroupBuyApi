package cn.com.shopgroup.user.config;

import cn.com.shopgroup.common.config.UploadConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private UploadConfig uploadConfig;

    // 本地公有文件访问前缀
    private static final String UPLOAD_REQ = "/image/file/**";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 本地共有文件存储路径
        registry.addResourceHandler(UPLOAD_REQ).addResourceLocations("file:///" + uploadConfig.getPath() + "/");
    }
}