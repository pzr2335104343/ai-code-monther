package com.rong.rongaicodemonther;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.rong.rongaicodemonther.mapper")
public class RongAiCodeMontherApplication {

    public static void main(String[] args) {
        SpringApplication.run(RongAiCodeMontherApplication.class, args);
    }

}
