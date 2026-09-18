package top.ehre.mod.category.controller;

import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import top.ehre.mod.category.service.CategoryService;
import top.ehre.mod.category.domain.vo.CategoryVO;
import top.ehre.mod.category.domain.dto.CategoryPageDTO;
import top.ehre.mod.category.domain.dto.CategoryAddDTO;
import top.ehre.mod.category.domain.dto.CategoryUpdateDTO;
import top.ehre.mod.util.PageResult;
import top.ehre.mod.util.Result;

import java.util.List;

/**
 * 模组分类控制器
 */
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    @PostMapping("/page")
    @PreAuthorize("hasAuthority('business:category:get')")
    public Result page(@RequestBody CategoryPageDTO categoryPageDTO) {
        PageResult<CategoryVO> page = categoryService.page(categoryPageDTO);
        return Result.success(page);
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('business:category:add')")
    public Result add(@RequestBody CategoryAddDTO categoryAddDTO) {
        boolean added = categoryService.add(categoryAddDTO);
        return Result.info(added, null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('business:category:del')")
    public Result delete(@PathVariable("id") String id) {
        boolean deleted = categoryService.delete(id);
        return Result.info(deleted, null);
    }

    @PostMapping("/batchDelete")
    @PreAuthorize("hasAuthority('business:category:del')")
    public Result batchDelete(@RequestBody List<String> ids) {
        boolean deleted = categoryService.batchDelete(ids);
        return Result.info(deleted, null);
    }

    @PutMapping("/update")
    @PreAuthorize("hasAuthority('business:category:upd')")
    public Result update(@RequestBody CategoryUpdateDTO categoryUpdateDTO) {
        boolean updated = categoryService.update(categoryUpdateDTO);
        return Result.info(updated, null);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('business:category:get')")
    public Result get(@PathVariable("id") String id) {
        CategoryVO categoryVO = categoryService.get(id);
        return Result.success(categoryVO);
    }

    @GetMapping("/list")
    public Result list() {
        return Result.success(categoryService.getList());
    }
}
