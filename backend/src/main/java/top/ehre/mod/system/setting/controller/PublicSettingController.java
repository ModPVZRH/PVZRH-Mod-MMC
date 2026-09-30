package top.ehre.mod.system.setting.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.ehre.mod.system.setting.service.SystemSettingService;
import top.ehre.mod.util.Result;

@RestController
@RequestMapping("/public/setting")
public class PublicSettingController {

    @Resource
    private SystemSettingService systemSettingService;

    @GetMapping
    public Result get() {
        return Result.success(systemSettingService.getSetting());
    }
}
