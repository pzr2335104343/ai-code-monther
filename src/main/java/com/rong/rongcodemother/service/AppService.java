package com.rong.rongcodemother.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.rong.rongcodemother.model.dto.app.AppQueryRequest;
import com.rong.rongcodemother.model.entity.App;
import com.rong.rongcodemother.model.entity.User;
import com.rong.rongcodemother.model.vo.AppVO;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 *  服务层。
 */
public interface AppService extends IService<App> {

    /**
     *
     * @param appId 应用id
     * @param message 消息
     * @param loginUser 登录用户
     * @return 代码流
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser);


    /**
     *
     * @param appId 应用id
     * @param loginUser 登录用户
     * @return 可访问的部署URL
     */
    String deployApp(Long appId,User loginUser);

    /**
     * 异步生成应用截图并更新应用封面
     * @param appId 应用ID
     * @param appUrl 应用访问地址
     */
    void generateAppScreenshotAsync(Long appId, String appUrl);

    /**
     * 根据查询请求获取查询条件
     * @param request 查询请求
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(AppQueryRequest request);

    /**
     * 根据应用获取应用视图对象
     * @param app 应用
     * @return 应用视图对象
     */
    AppVO getAppVO(App app);

    /**
     * 根据应用列表获取应用视图对象列表
     * @param list 应用列表
     * @return 应用视图对象列表
     */
    List<AppVO> getAppVOList(List<App> list);

}
