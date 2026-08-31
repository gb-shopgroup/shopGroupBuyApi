package cn.com.shopgroup.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KaptchaConfig {

    @Bean
    public Producer kaptchaProducer() {

        // 创建配置属性对象
        Properties properties = new Properties();
        // 验证码字符集（数字和大写英文字母）
        properties.setProperty("kaptcha.textproducer.char.string", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        // 验证码字符长度（4位）
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        // 图片宽度（120像素）
        properties.setProperty("kaptcha.image.width", "160");
        // 图片高度（40像素）
        properties.setProperty("kaptcha.image.height", "60");
        // 字体大小（30字号）
        properties.setProperty("kaptcha.textproducer.font.size", "30");
        // 字体颜色（黑色）
        properties.setProperty("kaptcha.textproducer.font.color", "0,0,0");

        // 初始化配置
        Config config = new Config(properties);
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        defaultKaptcha.setConfig(config);

        // 返回配置
        return defaultKaptcha;
    }

}
