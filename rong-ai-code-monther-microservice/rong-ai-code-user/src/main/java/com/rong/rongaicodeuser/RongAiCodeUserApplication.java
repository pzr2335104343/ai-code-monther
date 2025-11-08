package com.rong.rongaicodeuser;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDubbo
@MapperScan("com.rong.rongaicodeuser.mapper")
@ComponentScan("com.rong")
public class RongAiCodeUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(RongAiCodeUserApplication.class, args);
    }
}
