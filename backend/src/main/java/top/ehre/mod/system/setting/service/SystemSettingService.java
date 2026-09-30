package top.ehre.mod.system.setting.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.ehre.mod.system.setting.domain.entity.SystemSettingEntity;
import top.ehre.mod.system.setting.domain.vo.SystemSettingVO;

public interface SystemSettingService extends IService<SystemSettingEntity> {

    SystemSettingVO getSetting();

    boolean updateRegisterEnabled(Boolean registerEnabled);

    boolean isRegisterEnabled();
}
