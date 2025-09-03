package com.rong.rongaicodemonther.core;

import com.rong.rongaicodemonther.ai.AiCodeGeneratorService;
import com.rong.rongaicodemonther.ai.model.HtmlCodeResult;
import com.rong.rongaicodemonther.ai.model.MultiFileCodeResult;
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
            case HTML -> generateAndSaveHtmlCode(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCode(userMessage);
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 生成 HTML 模式的代码并保存
     *
     * @param userMessage 用户提示词
     * @return 保存的目录
     */
    private File generateAndSaveHtmlCode(String userMessage) {
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
        return CodeFileSaver.saveHtmlCodeResult(result);
    }

    /**
     * 生成多文件模式的代码并保存
     *
     * @param userMessage 用户提示词
     * @return 保存的目录
     */
    private File generateAndSaveMultiFileCode(String userMessage) {
        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
        return CodeFileSaver.saveMultiFileCodeResult(result);
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
            case HTML -> generateAndSaveHtmlCodeStream(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCodeStream(userMessage);
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 生成 HTML 模式的代码并保存(流式)
     *
     * @param userMessage 用户提示词
     * @return 保存的目录
     */
    private Flux<String> generateAndSaveHtmlCodeStream(String userMessage) {
        Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
        StringBuilder htmlCode = new StringBuilder();
        return result.doOnNext(htmlCode::append).doOnComplete(() -> {
            try {
                // 解析代码
                HtmlCodeResult htmlCodeResult = CodeParser.parseHtmlCode(String.valueOf(htmlCode));
                // 保存代码
                File file = CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
                log.info("保存的文件路径为：{}", file.getAbsolutePath());
            }
            catch (Exception e) {
                log.error("生成HTML代码失败", e);
            }

        });

    }

    /**
     * 生成多文件模式的代码并保存(流式)
     *
     * @param userMessage 用户提示词
     * @return 保存的目录
     */
    private Flux<String> generateAndSaveMultiFileCodeStream(String userMessage) {
        Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
        StringBuilder mutiFileCode = new StringBuilder();
        return result.doOnNext(mutiFileCode::append).doOnComplete(() -> {
            try {
                // 解析代码
                MultiFileCodeResult MultiFileCodeResult = CodeParser.parseMultiFileCode(String.valueOf(mutiFileCode));
                // 保存代码
                File file = CodeFileSaver.saveMultiFileCodeResult(MultiFileCodeResult);
                log.info("保存的多文件路径为：{}", file.getAbsolutePath());
            }
            catch (Exception e) {
                log.error("生成多文件代码失败", e);
            }

        });
    }

}
