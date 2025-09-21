package com.rong.rongcodemother.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.rong.rongcodemother.model.dto.chathistory.ChatHistoryQueryRequest;
import com.rong.rongcodemother.model.entity.ChatHistory;
import com.rong.rongcodemother.model.entity.User;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.time.LocalDateTime;

/**
 * 服务层。
 *
 * @author rong
 */
public interface ChatHistoryService extends IService<ChatHistory> {


    /**
     * 添加聊天记录
     *
     * @param appId       应用ID
     * @param message     消息内容
     * @param messageType 消息类型
     * @param userId      用户ID
     * @return 是否添加成功
     */
    boolean addChatMessage(Long appId, String message, String messageType, Long userId);


    /**
     * 根据应用ID删除聊天记录
     *
     * @param appId 应用ID
     * @return 是否删除成功
     */
    boolean deleteByAppId(Long appId);

    /**
     * 分页查询对话历史
     *
     * @param appId          应用ID
     * @param pageSize       页面大小
     * @param lastCreateTime 最新的创建时间
     * @param loginUser      登录用户
     * @return 分页结果
     */
    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize, LocalDateTime lastCreateTime, User loginUser);

    /**
     * 获取查询包装类
     *
     * @param chatHistoryQueryRequest 聊天记录查询请求
     * @return QueryWrapper
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);
}
