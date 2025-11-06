package com.rong.rongaicodemother.core.parser;

import com.rong.rongaicodemother.exception.BusinessException;
import com.rong.rongaicodemother.exception.ErrorCode;
import com.rong.rongaicodemother.model.enums.CodeGenTypeEnum;

/**
 * 代码解析执行器
 * 根据代码生成类型执行相应的解析逻辑
 *
 * @author yupi
 */
public class CodeParserExecutor {

    private static final HtmlCodeParser htmlCodeParser = new HtmlCodeParser();
    private static final MultiFileCodeParser multiFileCodeParser = new MultiFileCodeParser();

    /**
     * 执行代码解析
     * 根据不同的代码生成类型调用相应的解析器进行代码解析
     *
     * @param codeContent 代码内容，需要被解析的原始代码字符串
     * @param codeGenType 代码生成类型，指定使用哪种解析器进行处理
     * @return 解析结果（HtmlCodeResult 或 MultiFileCodeResult）
     */
    public static Object executeParser(String codeContent, CodeGenTypeEnum codeGenType) {
        return switch (codeGenType) {
            case HTML -> htmlCodeParser.parseCode(codeContent);
            case MULTI_FILE -> multiFileCodeParser.parseCode(codeContent);
            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的代码生成类型");
        };

    }
}
