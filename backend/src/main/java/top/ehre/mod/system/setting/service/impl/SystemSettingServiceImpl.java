package top.ehre.mod.system.setting.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.ehre.mod.exception.BusinessException;
import top.ehre.mod.system.setting.domain.entity.SystemSettingEntity;
import top.ehre.mod.system.setting.domain.vo.SystemSettingVO;
import top.ehre.mod.system.setting.mapper.SystemSettingMapper;
import top.ehre.mod.system.setting.service.SystemSettingService;

@Service
public class SystemSettingServiceImpl extends ServiceImpl<SystemSettingMapper, SystemSettingEntity>
        implements SystemSettingService {

    private static final int SETTING_ID = 1;

    @Override
    public SystemSettingVO getSetting() {
        SystemSettingEntity setting = getOrCreate();
        return new SystemSettingVO().setRegisterEnabled(Boolean.TRUE.equals(setting.getRegisterEnabled()));
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean updateRegisterEnabled(Boolean registerEnabled) {
        if (registerEnabled == null) {
            throw new BusinessException("是否开启注册不能为空");
        }
        SystemSettingEntity setting = getOrCreate();
        setting.setRegisterEnabled(registerEnabled);
        if (!updateById(setting)) {
            throw new BusinessException("更新失败");
        }
        return true;
    }

    @Override
    public boolean isRegisterEnabled() {
        return Boolean.TRUE.equals(getOrCreate().getRegisterEnabled());
    }

    private SystemSettingEntity getOrCreate() {
        SystemSettingEntity setting = getById(SETTING_ID);
        if (setting != null) {
            return setting;
        }
        setting = new SystemSettingEntity();
        setting.setId(SETTING_ID);
        setting.setRegisterEnabled(true);
        save(setting);
        return setting;
    }
}
