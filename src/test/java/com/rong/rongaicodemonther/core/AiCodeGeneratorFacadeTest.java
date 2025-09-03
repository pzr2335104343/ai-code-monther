package com.rong.rongaicodemonther.core;

import com.rong.rongaicodemonther.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiCodeGeneratorFacadeTest {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    void generateAndSaveCode() {
        aiCodeGeneratorFacade.generateAndSaveCode("做个程序员鱼皮的工作记录小工具,不超过20行0", CodeGenTypeEnum.HTML);
        aiCodeGeneratorFacade.generateAndSaveCode("做个程序员鱼皮的留言板,不超过20行", CodeGenTypeEnum.HTML);
        System.out.println(1);
    }
}