package com.rong.rongcodemother.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Spring MVC Json 配置
 */
@JsonComponent
public class JsonConfig {

    /**
     * 添加 Long 转 json 精度丢失的配置
     */
    @Bean
    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
        // 使用Jackson2ObjectMapperBuilder创建ObjectMapper实例，关闭XML映射支持（只处理JSON）
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();

        // 创建Jackson的模块（用于注册自定义序列化/反序列化规则）
        SimpleModule module = new SimpleModule();

        // 为Long类型和long基本类型注册序列化器：使用ToStringSerializer
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);

        // 将自定义模块注册到ObjectMapper中，使其生效
        objectMapper.registerModule(module);
        return objectMapper;
    }
}
