package top.ehre.mod.tag.controller;

import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import top.ehre.mod.tag.service.TagService;
import top.ehre.mod.tag.domain.vo.TagVO;
import top.ehre.mod.tag.domain.dto.TagPageDTO;
import top.ehre.mod.tag.domain.dto.TagAddDTO;
import top.ehre.mod.tag.domain.dto.TagUpdateDTO;
import top.ehre.mod.util.PageResult;
import top.ehre.mod.util.Result;

import java.util.List;

/**
 * 模组标签控制器
 */
@RestController
@RequestMapping("/tag")
public class TagController {

    @Resource
    private TagService tagService;

    @PostMapping("/page")
    @PreAuthorize("hasAuthority('business:tag:get')")
    public Result page(@RequestBody TagPageDTO tagPageDTO) {
        PageResult<TagVO> page = tagService.page(tagPageDTO);
        return Result.success(page);
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('business:tag:add')")
    public Result add(@RequestBody TagAddDTO tagAddDTO) {
        boolean added = tagService.add(tagAddDTO);
        return Result.info(added, null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('business:tag:del')")
    public Result delete(@PathVariable("id") String id) {
        boolean deleted = tagService.delete(id);
        return Result.info(deleted, null);
    }

    @PostMapping("/batchDelete")
    @PreAuthorize("hasAuthority('business:tag:del')")
    public Result batchDelete(@RequestBody List<String> ids) {
        boolean deleted = tagService.batchDelete(ids);
        return Result.info(deleted, null);
    }

    @PutMapping("/update")
    @PreAuthorize("hasAuthority('business:tag:upd')")
    public Result update(@RequestBody TagUpdateDTO tagUpdateDTO) {
        boolean updated = tagService.update(tagUpdateDTO);
        return Result.info(updated, null);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('business:tag:get')")
    public Result get(@PathVariable("id") String id) {
        TagVO tagVO = tagService.get(id);
        return Result.success(tagVO);
    }

    @GetMapping("/list")
    public Result list() {
        return Result.success(tagService.getList());
    }
}
