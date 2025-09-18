// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 此处后端没有提供注释 GET /static/${param0}/${param1}/&#42;&#42; */
export async function serveStaticResource(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.serveStaticResourceParams,
  options?: { [key: string]: any }
) {
  const { deployKey: param0, appVersion: param1, ...queryParams } = params
  return request<string>(`/static/${param0}/${param1}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 此处后端没有提供注释 GET /static/list */
export async function listDirectoryNames(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.listDirectoryNamesParams,
  options?: { [key: string]: any }
) {
  return request<string[]>('/static/list', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}
