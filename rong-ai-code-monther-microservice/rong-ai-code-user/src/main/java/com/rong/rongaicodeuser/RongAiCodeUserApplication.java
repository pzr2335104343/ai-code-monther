package com.rong.rongaicodeuser;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.rong.rongaicodeuser.mapper")
@ComponentScan("com.rong")
public class RongAiCodeUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(RongAiCodeUserApplication.class, args);
    }
}
