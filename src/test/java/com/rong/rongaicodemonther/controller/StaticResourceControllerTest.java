package com.rong.rongaicodemonther.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * 静态资源访问控制器测试
 */
@SpringBootTest
@AutoConfigureWebMvc
public class StaticResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetStaticResourceInfo() throws Exception {
        // 测试获取静态资源信息接口
        mockMvc.perform(MockMvcRequestBuilders.get("/static/info/test-deploy-key"))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testGetStaticResourceNotFound() throws Exception {
        // 测试访问不存在的静态资源
        mockMvc.perform(MockMvcRequestBuilders.get("/static/nonexistent/index.html"))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void testGetStaticResourceWithInvalidPath() throws Exception {
        // 测试访问非法路径（路径遍历攻击）
        mockMvc.perform(MockMvcRequestBuilders.get("/static/test/../../../etc/passwd"))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }
}
