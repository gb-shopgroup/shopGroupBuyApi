package cn.com.shopgroup.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "upload-config")
public class UploadConfig {

    private int type;
    private String path;
    private String path2;
    private String domain;
    private String domain2;

}
