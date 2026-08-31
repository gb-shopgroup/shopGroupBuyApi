package cn.com.shopgroup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class GroupUserApplication {

    public static void main(String[] args) {

        SpringApplication.run(GroupUserApplication.class, args);
        log.info("************用户服务已启动,successful!**********");
    }
}
