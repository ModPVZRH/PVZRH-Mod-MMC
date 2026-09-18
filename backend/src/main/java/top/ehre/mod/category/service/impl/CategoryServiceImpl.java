package top.ehre.mod.category.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import top.ehre.mod.category.domain.entity.CategoryEntity;
import top.ehre.mod.category.mapper.CategoryMapper;
import top.ehre.mod.category.service.CategoryService;
import top.ehre.mod.mods.domain.entity.ModsEntity;
import top.ehre.mod.mods.mapper.ModsMapper;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import top.ehre.mod.category.domain.vo.CategoryVO;
import top.ehre.mod.category.domain.dto.CategoryPageDTO;
import top.ehre.mod.category.domain.dto.CategoryAddDTO;
import top.ehre.mod.category.domain.dto.CategoryUpdateDTO;
import top.ehre.mod.util.PageResult;
import top.ehre.mod.util.PageUtil;
import top.ehre.mod.exception.BusinessException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 模组分类服务实现
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, CategoryEntity> implements CategoryService {

    @Resource
    CategoryMapper categoryMapper;

    @Resource
    ModsMapper modsMapper;

    @Override
    public PageResult<CategoryVO> page(CategoryPageDTO categoryPageDTO) {
        Page<?> page = PageUtil.convert2PageQuery(categoryPageDTO);
        List<CategoryVO> list = categoryMapper.queryPage(page, categoryPageDTO);
        return PageUtil.convert2PageResult(page, list);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean add(CategoryAddDTO categoryAddDTO) {
        validateName(categoryAddDTO.getName(), null);
        CategoryEntity category = new CategoryEntity();
        BeanUtils.copyProperties(categoryAddDTO, category);
        category.setName(categoryAddDTO.getName().trim());
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        boolean saved = save(category);
        if (!saved) {
            throw new BusinessException("添加失败");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean delete(String id) {
        CategoryEntity exists = getById(id);
        if (exists == null) {
            throw new BusinessException("不存在该对象");
        }
        assertNotUsed(id);
        boolean removed = removeById(id);
        if (!removed) {
            throw new BusinessException("删除失败");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean batchDelete(List<String> ids) {
        if (ids != null) {
            ids.forEach(this::assertNotUsed);
        }
        boolean removed = removeBatchByIds(ids);
        if (!removed) {
            throw new BusinessException("删除失败");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean update(CategoryUpdateDTO categoryUpdateDTO) {
        if (categoryUpdateDTO.getId() == null) {
            throw new BusinessException("主键不能为空");
        }
        CategoryEntity exists = getById(categoryUpdateDTO.getId());
        if (exists == null) {
            throw new BusinessException("不存在该对象");
        }
        validateName(categoryUpdateDTO.getName(), categoryUpdateDTO.getId());
        CategoryEntity category = new CategoryEntity();
        BeanUtils.copyProperties(categoryUpdateDTO, category);
        category.setName(categoryUpdateDTO.getName().trim());
        boolean updated = updateById(category);
        if (!updated) {
            throw new BusinessException("更新失败");
        }
        return true;
    }

    @Override
    public CategoryVO get(String id) {
        CategoryEntity category = getById(id);
        if (category == null) {
            throw new BusinessException("不存在该对象");
        }
        CategoryVO categoryVO = new CategoryVO();
        BeanUtils.copyProperties(category, categoryVO);
        return categoryVO;
    }

    @Override
    public List<CategoryVO> getList() {
        List<CategoryEntity> list = list(new LambdaQueryWrapper<CategoryEntity>()
                .orderByAsc(CategoryEntity::getSortOrder)
                .orderByAsc(CategoryEntity::getId));
        return list.stream().map(entity -> {
            CategoryVO vo = new CategoryVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).toList();
    }

    private void validateName(String name, String excludeId) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("分类名称不能为空");
        }
        LambdaQueryWrapper<CategoryEntity> wrapper = new LambdaQueryWrapper<CategoryEntity>()
                .eq(CategoryEntity::getName, name.trim());
        if (excludeId != null && !excludeId.isBlank()) {
            wrapper.ne(CategoryEntity::getId, excludeId);
        }
        if (count(wrapper) > 0) {
            throw new BusinessException("分类名称已存在");
        }
    }

    private void assertNotUsed(String id) {
        Long used = modsMapper.selectCount(new LambdaQueryWrapper<ModsEntity>()
                .eq(ModsEntity::getCategoryId, id));
        if (used != null && used > 0) {
            throw new BusinessException("该分类下仍有模组，无法删除");
        }
    }
}
