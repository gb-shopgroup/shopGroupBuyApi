package cn.com.shopgroup.yeepay;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

// 易宝支付环境配置: 绑定不同环境配置文件(application-dev/test/prod.yml)中的 pay-config 前缀属性, 启动时按激活的环境取值
@Data
@Component
@Configuration
@ConfigurationProperties(prefix = "pay-config")
public class YeePayConfig {

    // 支付回调地址(发起支付时作为 notifyUrl 传给易宝)
    private String url;

    // 微信小程序appId
    private String WxAppId;

    // 易宝父商户号
    private String parentMerchantNo;

    // 支付结果通知地址(未传 notifyUrl 时兜底)
    private String PayNotifyUrl;

    // 退款结果通知地址
    private String RefundNotifyUrl;

    // 添加收款商户结果通知地址
    private String MerNotifyUrl;

    // 启动时将当前环境的配置值同步给 YeePayUtils(静态参数), 供静态调用方法使用
    @PostConstruct
    public void init() {
        YeePayUtils.init(this);
    }

}
