package cn.com.shopgroup.common.yeepay;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Data
@Component
@Configuration
@ConfigurationProperties(prefix = "pay-config")
public class YeepayConfig {

    private String url;

    private String WxAppId;

    private String parentMerchantNo;
    private String PayNotifyUrl;

    private String RefundNotifyUrl;

    private String MerNotifyUrl;

}
