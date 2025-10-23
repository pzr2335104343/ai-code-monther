package com.rong.rongcodemother;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@MapperScan("com.rong.rongcodemother.mapper")
public class RongAiCodeMontherApplication {

    public static void main(String[] args) {
        SpringApplication.run(RongAiCodeMontherApplication.class, args);
    }

}
