package com.rong.rongaicodemonther.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.rong.rongaicodemonther.model.dto.AppQueryRequest;
import com.rong.rongaicodemonther.model.entity.App;
import com.rong.rongaicodemonther.mapper.AppMapper;
import com.rong.rongaicodemonther.model.vo.AppVO;
import com.rong.rongaicodemonther.service.AppService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *  服务层实现。
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest request) {
        if (request == null) {
            return QueryWrapper.create();
        }
        Long id = request.getId();
        Long userId = request.getUserId();
        String appName = request.getAppName();
        String cover = request.getCover();
        String codeGenType = request.getCodeGenType();
        String deployKey = request.getDeployKey();
        Integer priority = request.getPriority();
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("id", id)
                .eq("userId", userId)
                .eq("priority", priority)
                .eq("cover", cover)
                .eq("deployKey", deployKey)
                .eq("codeGenType", codeGenType)
                .orderBy(sortField, "ascend".equals(sortOrder));
        if (StrUtil.isNotBlank(appName)) {
            queryWrapper.like("appName", appName);
        }
        return queryWrapper;
    }

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO vo = new AppVO();
        BeanUtils.copyProperties(app, vo);
        return vo;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> list) {
        if (CollUtil.isEmpty(list)) {
            return new ArrayList<>();
        }
        return list.stream().map(this::getAppVO).collect(Collectors.toList());
    }
}
