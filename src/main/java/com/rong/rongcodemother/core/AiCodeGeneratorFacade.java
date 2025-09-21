package com.rong.rongcodemother.core;

import com.rong.rongcodemother.ai.AiCodeGeneratorService;
import com.rong.rongcodemother.ai.AiCodeGeneratorServiceFactory;
import com.rong.rongcodemother.ai.model.HtmlCodeResult;
import com.rong.rongcodemother.ai.model.MultiFileCodeResult;
import com.rong.rongcodemother.core.parser.CodeParserExecutor;
import com.rong.rongcodemother.core.saver.CodeFIleSaverExecutor;
import com.rong.rongcodemother.exception.BusinessException;
import com.rong.rongcodemother.exception.ErrorCode;
import com.rong.rongcodemother.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI 代码生成外观类，组合生成和保存功能
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    /**
     * 统一入口：根据类型生成并保存代码
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @param appId 应用id
     * @return 保存的目录
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum,Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFIleSaverExecutor.executeSaver(result,codeGenTypeEnum,appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFIleSaverExecutor.executeSaver(result,codeGenTypeEnum,appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 统一入口：根据类型生成并保存代码(流式)
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @param appId 应用id
     * @return 保存的目录
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum,Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(codeStream, codeGenTypeEnum,appId);
            }
            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(codeStream, codeGenTypeEnum,appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 通用流式代码处理方法
     *
     * @param codeStream  代码流
     * @param codeGenType 生成类型
     * @param appId 应用id
     * @return 处理后的代码流
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType,Long appId) {
        StringBuilder completeCode = new StringBuilder();
        return codeStream.doOnNext(completeCode::append).doOnComplete(() -> {
            try {
                // 根据类型解析并保存代码
                Object object = CodeParserExecutor.executeParser(completeCode.toString(), codeGenType);
                File file = CodeFIleSaverExecutor.executeSaver(object,codeGenType,appId);
                log.info("保存的文件路径为：{}", file.getAbsolutePath());
            } catch (Exception e) {
                log.error("生成代码失败", e);
            }
        });
    }
}
