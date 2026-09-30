package top.ehre.mod.mods.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import top.ehre.mod.mods.service.ModsService;

import top.ehre.mod.mods.domain.vo.ModsVO;
import top.ehre.mod.mods.domain.vo.ModVersionVO;
import top.ehre.mod.mods.domain.dto.ModsPageDTO;
import top.ehre.mod.mods.domain.dto.ModsAddDTO;
import top.ehre.mod.mods.domain.dto.ModsUpdateDTO;
import top.ehre.mod.mods.domain.dto.ModVersionAddDTO;
import top.ehre.mod.mods.domain.dto.ModVersionUpdateDTO;

import top.ehre.mod.util.PageResult;
import top.ehre.mod.util.Result;

import java.util.List;


/**
 *  前端控制器
 *
 * @author LibrhHp_0928
 * @since 2025-11-24 18:58:33
 */
@RestController
@RequestMapping("/mods")
public class ModsController {
    @Resource
    private ModsService modsService;

    @PostMapping("/page")
    @PreAuthorize("hasAuthority('business:mods:get')")
    public Result page(@RequestBody ModsPageDTO modsPageDTO) {
        PageResult<ModsVO> page = modsService.page(modsPageDTO);
        return Result.success(page);
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('business:mods:add')")
    public Result add(@RequestBody ModsAddDTO modsAddDTO) {
        boolean added = modsService.add(modsAddDTO);
        return Result.info(added, null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('business:mods:del')")
    public Result delete(@PathVariable("id") String id) {
        boolean deleted = modsService.delete(id);
        return Result.info(deleted, null);
    }

    @PostMapping("/batchDelete")
    @PreAuthorize("hasAuthority('business:mods:del')")
    public Result batchDelete(@RequestBody List<String> ids) {
        boolean deleted = modsService.batchDelete(ids);
        return Result.info(deleted, null);
    }

    @PutMapping("/update")
    @PreAuthorize("hasAuthority('business:mods:upd')")
    public Result update(@RequestBody ModsUpdateDTO modsUpdateDTO) {
        boolean updated = modsService.update(modsUpdateDTO);
        return Result.info(updated, null);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('business:mods:get')")
    public Result get(@PathVariable("id") String id) {
        ModsVO modsVO = modsService.get(id);
        return Result.success(modsVO);
    }

    @GetMapping("/{modId}/versions")
    @PreAuthorize("hasAuthority('business:mods:get')")
    public Result listVersions(@PathVariable("modId") String modId) {
        List<ModVersionVO> versions = modsService.listVersions(modId);
        return Result.success(versions);
    }

    @PostMapping("/{modId}/versions")
    @PreAuthorize("hasAuthority('business:mods:upd')")
    public Result addVersion(@PathVariable("modId") String modId, @RequestBody ModVersionAddDTO modVersionAddDTO) {
        ModVersionVO version = modsService.addVersion(modId, modVersionAddDTO);
        return Result.success(version);
    }

    @PutMapping("/version/update")
    @PreAuthorize("hasAuthority('business:mods:upd')")
    public Result updateVersion(@RequestBody ModVersionUpdateDTO modVersionUpdateDTO) {
        boolean updated = modsService.updateVersion(modVersionUpdateDTO);
        return Result.info(updated, null);
    }

    @DeleteMapping("/version/{id}")
    @PreAuthorize("hasAuthority('business:mods:upd')")
    public Result deleteVersion(@PathVariable("id") String id) {
        boolean deleted = modsService.deleteVersion(id);
        return Result.info(deleted, null);
    }

}
