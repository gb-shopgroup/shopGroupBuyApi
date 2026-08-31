package cn.com.shopgroup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@Slf4j
public class GroupTaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(GroupUserApplication.class, args);
        log.info("**********任务服务器启动.......successful! **************");
    }
}