package top.ehre.mod.system.setting.controller;

import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.ehre.mod.exception.BusinessException;
import top.ehre.mod.system.setting.domain.dto.SystemSettingUpdateDTO;
import top.ehre.mod.system.setting.service.SystemSettingService;
import top.ehre.mod.util.Result;

@RestController
@RequestMapping("/system-setting")
public class SystemSettingController {

    @Resource
    private SystemSettingService systemSettingService;

    @GetMapping
    @PreAuthorize("hasAuthority('system:user:upd')")
    public Result get() {
        return Result.success(systemSettingService.getSetting());
    }

    @PutMapping
    @PreAuthorize("hasAuthority('system:user:upd')")
    public Result update(@RequestBody SystemSettingUpdateDTO systemSettingUpdateDTO) {
        if (systemSettingUpdateDTO == null) {
            throw new BusinessException("是否开启注册不能为空");
        }
        boolean updated = systemSettingService.updateRegisterEnabled(systemSettingUpdateDTO.getRegisterEnabled());
        return Result.info(updated, null);
    }
}
