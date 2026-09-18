package top.ehre.mod.mods.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import top.ehre.mod.announcement.service.AnnouncementService;
import top.ehre.mod.category.service.CategoryService;
import top.ehre.mod.clientVersion.service.ClientVersionService;
import top.ehre.mod.mods.domain.dto.ModsPageDTO;
import top.ehre.mod.mods.domain.vo.ModsVO;
import top.ehre.mod.mods.service.ModsService;
import top.ehre.mod.tag.service.TagService;
import top.ehre.mod.util.PageResult;
import top.ehre.mod.util.Result;

import java.util.List;

/**
 * 公共接口
 * @author 19411
 * @date 2025/11/25
 */

@RestController
@RequestMapping("/public")
public class PublicController {
    @Resource
    private ModsService modsService;

    @Resource
    private ClientVersionService clientVersionService;

    @Resource
    private AnnouncementService announcementService;

    @Resource
    private CategoryService categoryService;

    @Resource
    private TagService tagService;

    @GetMapping("/mod")
    public Result modList(@RequestParam(value = "categoryId", required = false) String categoryId) {
        return Result.success(modsService.getListByCategory(categoryId));
    }

    @GetMapping("/mod/category/{categoryId}")
    public Result modListByCategory(@PathVariable("categoryId") String categoryId) {
        return Result.success(modsService.getListByCategory(categoryId));
    }

    @GetMapping("/mod/page")
    public Result modPage(@RequestBody ModsPageDTO modsPageDTO) {
        PageResult<ModsVO> page = modsService.page(modsPageDTO);
        return Result.success(page);
    }

    @GetMapping("/mod/{id}")
    public Result get(@PathVariable("id") String id) {
        ModsVO modsVO = modsService.get(id);
        return Result.success(modsVO);
    }

    @PostMapping("/mod/{id}/view")
    public Result incrementView(@PathVariable("id") String id) {
        return Result.success(modsService.incrementViewCount(id));
    }

    @PostMapping("/mod/{id}/download")
    public Result incrementDownload(@PathVariable("id") String id) {
        return Result.success(modsService.incrementDownloadCount(id));
    }

    @GetMapping("/getPublishVersion")
    public Result getPublishVersion() {
        return Result.success(clientVersionService.getPublicVersion());
    }

    @GetMapping("/announcements")
    public Result getPublishedAnnouncements() {
        return Result.success(announcementService.getPublishedAnnouncements());
    }

    @GetMapping("/category")
    public Result categoryList() {
        return Result.success(categoryService.getList());
    }

    @GetMapping("/tag")
    public Result tagList() {
        return Result.success(tagService.getList());
    }
}
