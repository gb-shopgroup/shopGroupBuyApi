package cn.com.shopgroup.common.wxmini;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信小程序相关配置(wx-mini.*)
 * <p>
 * 用于将微信小程序 appid / secret / 小程序码扫码目标版本 抽离到配置文件中,
 * 便于不同部署环境(dev/test=体验版, prod=正式版)使用不同配置.
 * <p>
 * 老部署未配置 wx-mini.* 时, WxMiniProgramHelper 内置默认值兜底,
 * 行为与改造前保持一致(默认生成正式版二维码).
 */
@Data
@Component
@ConfigurationProperties(prefix = "wx-mini")
public class WxMiniProperties {

    /**
     * 微信小程序 appid
     * 为空时使用 WxMiniProgramHelper 内置默认 appid
     */
    private String appid;

    /**
     * 微信小程序 secret
     * 为空时使用 WxMiniProgramHelper 内置默认 secret
     */
    private String secret;

    /**
     * 小程序码(getwxacodeunlimit)扫码目标版本
     * <ul>
     *   <li>trial   - 体验版</li>
     *   <li>release - 正式版(微信接口默认值)</li>
     *   <li>develop - 开发版(仅开发者本人可扫)</li>
     * </ul>
     * 为空时使用 WxMiniProgramHelper 内置默认版本 "release"
     */
    private String envVersion;
}