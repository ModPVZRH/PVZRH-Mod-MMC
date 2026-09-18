package top.ehre.mod.category.service;

import top.ehre.mod.category.domain.entity.CategoryEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import top.ehre.mod.category.domain.vo.CategoryVO;
import top.ehre.mod.category.domain.dto.CategoryPageDTO;
import top.ehre.mod.category.domain.dto.CategoryAddDTO;
import top.ehre.mod.category.domain.dto.CategoryUpdateDTO;
import top.ehre.mod.util.PageResult;

import java.util.List;

/**
 * 模组分类服务
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageResult<CategoryVO> page(CategoryPageDTO categoryPageDTO);

    boolean add(CategoryAddDTO categoryAddDTO);

    boolean delete(String id);

    boolean batchDelete(List<String> ids);

    boolean update(CategoryUpdateDTO categoryUpdateDTO);

    CategoryVO get(String id);

    List<CategoryVO> getList();
}
