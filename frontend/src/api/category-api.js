import { postRequest, getRequest, deleteRequest, putRequest } from '@/lib/axios';

export const categoryApi = {
  page: (param) => {
    return postRequest('/category/page', param);
  },

  add: (param) => {
    return postRequest('/category/add', param);
  },

  delete: (id) => {
    return deleteRequest(`/category/${id}`);
  },

  batchDelete: (ids) => {
    return postRequest(`/category/batchDelete`, ids);
  },

  update: (param) => {
    return putRequest('/category/update', param);
  },

  get: (id) => {
    return getRequest(`/category/${id}`);
  },

  getList: () => {
    return getRequest('/category/list');
  }
};
