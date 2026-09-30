import { getRequest, putRequest } from '@/lib/axios';

export const systemSettingApi = {
  getPublic: () => {
    return getRequest('/public/setting');
  },

  update: (param) => {
    return putRequest('/system-setting', param);
  }
};
