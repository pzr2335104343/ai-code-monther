package com.rong.rongaicodemonther.core;

import com.rong.rongaicodemonther.ai.AiCodeGeneratorService;
import com.rong.rongaicodemonther.ai.model.HtmlCodeResult;
import com.rong.rongaicodemonther.ai.model.MultiFileCodeResult;
import com.rong.rongaicodemonther.core.parser.CodeParserExecutor;
import com.rong.rongaicodemonther.core.saver.CodeFIleSaverExecutor;
import com.rong.rongaicodemonther.exception.BusinessException;
import com.rong.rongaicodemonther.exception.ErrorCode;
import com.rong.rongaicodemonther.model.enums.CodeGenTypeEnum;
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
    private AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 统一入口：根据类型生成并保存代码
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFIleSaverExecutor.executeSaver(result, codeGenTypeEnum);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFIleSaverExecutor.executeSaver(result, codeGenTypeEnum);
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
     * @return 保存的目录
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(codeStream, codeGenTypeEnum);
            }
            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(codeStream, codeGenTypeEnum);
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
     * @return 处理后的代码流
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType) {

        StringBuilder completeCode = new StringBuilder();
        return codeStream.doOnNext(completeCode::append).doOnComplete(() -> {
            try {
                // 解析代码
                Object htmlCodeResult = CodeParserExecutor.executeParser(completeCode.toString(), codeGenType);
                // 保存代码
                File file = CodeFIleSaverExecutor.executeSaver(htmlCodeResult, codeGenType);
                log.info("保存的文件路径为：{}", file.getAbsolutePath());
            } catch (Exception e) {
                log.error("生成HTML代码失败", e);
            }
        });
    }
}
