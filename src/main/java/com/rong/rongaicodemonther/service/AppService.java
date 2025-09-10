package com.rong.rongaicodemonther.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.rong.rongaicodemonther.model.dto.AppQueryRequest;
import com.rong.rongaicodemonther.model.entity.App;
import com.rong.rongaicodemonther.model.vo.AppVO;

import java.util.List;

/**
 *  服务层。
 */
public interface AppService extends IService<App> {

    QueryWrapper getQueryWrapper(AppQueryRequest request);

    AppVO getAppVO(App app);

    List<AppVO> getAppVOList(List<App> list);
}
