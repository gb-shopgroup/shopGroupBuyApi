package cn.com.shopgroup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class GroupGoodsApplication {

    public static void main(String[] args) {

        SpringApplication.run(GroupGoodsApplication.class, args);
        log.info("**********商品服务启动.......successful! **************");
    }
}
