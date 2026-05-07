package edu.tut.cms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("edu.tut.cms.server.mapper")
public class CommunityManageSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommunityManageSystemApplication.class, args);
    }

}
