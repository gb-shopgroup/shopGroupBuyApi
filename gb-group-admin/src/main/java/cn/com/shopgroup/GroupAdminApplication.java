package cn.com.shopgroup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class GroupAdminApplication {

    public static void main(String[] args) {

        SpringApplication.run(GroupAdminApplication.class, args);
        log.info("**********管理后台服务【group-admin】启动.......successful! **************");
    }
}
