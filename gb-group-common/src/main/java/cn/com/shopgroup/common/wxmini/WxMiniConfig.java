package cn.com.shopgroup.common.wxmini;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 启动时将 wx-mini.* 配置同步到 WxMiniProgramHelper 的静态字段,
 * 保持现有所有静态方法调用方无需修改.
 * <p>
 * 配置为空时不覆盖静态字段默认值, 保持向下兼容.
 */
@Slf4j
@Component
public class WxMiniConfig {

    @Resource
    private WxMiniProperties properties;

    @PostConstruct
    public void init() {
        if (properties == null) {
            log.info("[WxMini] WxMiniProperties 未注入, 微信小程序配置保持默认值");
            return;
        }
        if (StringUtils.hasText(properties.getAppid())) {
            WxMiniProgramHelper.APP_ID = properties.getAppid();
        }
        if (StringUtils.hasText(properties.getSecret())) {
            WxMiniProgramHelper.APP_SECRET = properties.getSecret();
        }
        if (StringUtils.hasText(properties.getEnvVersion())) {
            WxMiniProgramHelper.ENV_VERSION = properties.getEnvVersion();
        }
        log.info("[WxMini] 微信小程序配置加载完成: envVersion={}", WxMiniProgramHelper.ENV_VERSION);
    }
}