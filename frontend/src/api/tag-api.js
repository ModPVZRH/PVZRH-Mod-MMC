import { postRequest, getRequest, deleteRequest, putRequest } from '@/lib/axios';

export const tagApi = {
  page: (param) => {
    return postRequest('/tag/page', param);
  },

  add: (param) => {
    return postRequest('/tag/add', param);
  },

  delete: (id) => {
    return deleteRequest(`/tag/${id}`);
  },

  batchDelete: (ids) => {
    return postRequest(`/tag/batchDelete`, ids);
  },

  update: (param) => {
    return putRequest('/tag/update', param);
  },

  get: (id) => {
    return getRequest(`/tag/${id}`);
  },

  getList: () => {
    return getRequest('/tag/list');
  }
};
