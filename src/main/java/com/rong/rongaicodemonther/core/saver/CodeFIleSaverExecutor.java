package com.rong.rongaicodemonther.core.saver;

import com.rong.rongaicodemonther.ai.model.HtmlCodeResult;
import com.rong.rongaicodemonther.ai.model.MultiFileCodeResult;
import com.rong.rongaicodemonther.exception.BusinessException;
import com.rong.rongaicodemonther.exception.ErrorCode;
import com.rong.rongaicodemonther.model.enums.CodeGenTypeEnum;

import java.io.File;

public class CodeFIleSaverExecutor {

    private static final HtmlCodeFileSaverTemplate htmlCodeFileSaverTemplate=new HtmlCodeFileSaverTemplate();
    private static final MultiFileCodeSaverTemplate multiFileCodeSaverTemplate=new MultiFileCodeSaverTemplate();

/**
 * 根据代码生成类型保存对应的代码文件
 * @param codeRequest 代码请求对象，包含需要保存的代码内容
 * @param codeGenTypeEnum 代码生成类型枚举，决定如何保存代码
 * @param appId 应用id
 * @return 返回保存后的文件对象
 * @throws BusinessException 当代码生成类型不支持时抛出异常
 */
    public static File executeSaver(Object codeRequest, CodeGenTypeEnum codeGenTypeEnum,Long appId) {

    // 使用switch表达式根据代码生成类型选择对应的保存方式
        return switch (codeGenTypeEnum) {
        // 当类型为HTML时，使用HTML代码文件保存器保存代码
            case HTML -> htmlCodeFileSaverTemplate.saveCode((HtmlCodeResult) codeRequest,appId);
        // 当类型为MULTI_FILE时，使用多文件代码保存器保存代码
            case MULTI_FILE ->multiFileCodeSaverTemplate.saveCode((MultiFileCodeResult) codeRequest,appId);
        // 默认情况，抛出非法参数异常
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        };
    }
}
