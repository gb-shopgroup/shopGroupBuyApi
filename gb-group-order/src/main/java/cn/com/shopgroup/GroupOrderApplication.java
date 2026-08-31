package cn.com.shopgroup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class GroupOrderApplication {

    public static void main(String[] args) {

        SpringApplication.run(GroupUserApplication.class, args);
        log.info("**********订单服务器启动.......successful! **************");
    }
}
