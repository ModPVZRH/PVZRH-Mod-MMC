import axios from 'axios';
import {localRead, localRemove} from "@/utils/local-util.js";
import {tGlobal} from '@/i18n/index.js';

const TOKEN_HEADER = 'token';
// 创建axios对象
const cengxuyuanAxios = axios.create({
  baseURL: import.meta.env.VITE_APP_API_URL, // !!!需要修改!!!
});


// ================================= 请求拦截器 =================================

cengxuyuanAxios.interceptors.request.use(
    (config) => {
      const token = localRead(TOKEN_HEADER);
      if (token) {
        config.headers[TOKEN_HEADER] = token;
      }
      return config;
    },
    (error) => {
      return Promise.reject(error);
    }
);

// ================================= 响应拦截器 =================================

cengxuyuanAxios.interceptors.response.use(
    (response) => {
      let contentType = response.headers['content-type'] ? response.headers['content-type'] : response.headers['Content-Type'];
      if (contentType.indexOf('application/json') === -1) {
        return Promise.resolve(response);
      }

      if (response.data && response.data instanceof Blob) {
        return Promise.reject(response.data);
      }

      const res = response.data;
      if (res.code && res.code !== 0) {
        if (res.code === 100) {
          localRemove('modMenu')
          localRemove('user')
          localRemove(TOKEN_HEADER)
          console.log('reset')
        }
        ElMessage.error(res.msg);
        return Promise.reject(response);
      } else {
        return Promise.resolve(res);
      }
    },
    (error) => {
      if (error.message.indexOf('timeout') !== -1) {
        ElMessage.error(tGlobal('network.timeout'));
      } else if (error.message === 'Network Error') {
        ElMessage.error(tGlobal('network.error'));
      } else if (error.message.indexOf('Request') !== -1) {
        ElMessage.error(tGlobal('network.requestError'));
      }
      return Promise.reject(error);
    }
);

/**
 * 通用请求封装
 * @param config
 */
export const request = (config) => {
  return cengxuyuanAxios.request(config);
};

/**
 * get请求
 */
export const getRequest = (url, params) => {
  return request({url, method: 'get', params});
};


/**
 * post请求
 */
export const postRequest = (url, data) => {
  return request({
    data,
    url,
    method: 'post',
  });
};

/**
 * put请求
 */
export const putRequest = (url, data) => {
  return request({
    data,
    url,
    method: 'put',
  });
};

/**
 * delete请求
 */
export const deleteRequest = (url, params) => {
  return request({
    url,
    method: 'delete',
    params,
  });
};