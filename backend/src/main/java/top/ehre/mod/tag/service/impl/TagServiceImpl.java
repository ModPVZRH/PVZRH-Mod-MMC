package top.ehre.mod.tag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import top.ehre.mod.tag.domain.entity.TagEntity;
import top.ehre.mod.tag.mapper.TagMapper;
import top.ehre.mod.tag.service.TagService;
import top.ehre.mod.mods.mapper.ModsMapper;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import top.ehre.mod.tag.domain.vo.TagVO;
import top.ehre.mod.tag.domain.dto.TagPageDTO;
import top.ehre.mod.tag.domain.dto.TagAddDTO;
import top.ehre.mod.tag.domain.dto.TagUpdateDTO;
import top.ehre.mod.util.PageResult;
import top.ehre.mod.util.PageUtil;
import top.ehre.mod.exception.BusinessException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 模组标签服务实现
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, TagEntity> implements TagService {

    @Resource
    TagMapper tagMapper;

    @Resource
    ModsMapper modsMapper;

    @Override
    public PageResult<TagVO> page(TagPageDTO tagPageDTO) {
        Page<?> page = PageUtil.convert2PageQuery(tagPageDTO);
        List<TagVO> list = tagMapper.queryPage(page, tagPageDTO);
        return PageUtil.convert2PageResult(page, list);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean add(TagAddDTO tagAddDTO) {
        validateName(tagAddDTO.getName(), null);
        TagEntity tag = new TagEntity();
        BeanUtils.copyProperties(tagAddDTO, tag);
        tag.setName(tagAddDTO.getName().trim());
        if (tag.getColor() == null || tag.getColor().isBlank()) {
            tag.setColor("#409EFF");
        }
        boolean saved = save(tag);
        if (!saved) {
            throw new BusinessException("添加失败");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean delete(String id) {
        TagEntity exists = getById(id);
        if (exists == null) {
            throw new BusinessException("不存在该对象");
        }
        modsMapper.deleteModTagsByTagId(id);
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
            ids.forEach(modsMapper::deleteModTagsByTagId);
        }
        boolean removed = removeBatchByIds(ids);
        if (!removed) {
            throw new BusinessException("删除失败");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean update(TagUpdateDTO tagUpdateDTO) {
        if (tagUpdateDTO.getId() == null) {
            throw new BusinessException("主键不能为空");
        }
        TagEntity exists = getById(tagUpdateDTO.getId());
        if (exists == null) {
            throw new BusinessException("不存在该对象");
        }
        validateName(tagUpdateDTO.getName(), tagUpdateDTO.getId());
        TagEntity tag = new TagEntity();
        BeanUtils.copyProperties(tagUpdateDTO, tag);
        tag.setName(tagUpdateDTO.getName().trim());
        boolean updated = updateById(tag);
        if (!updated) {
            throw new BusinessException("更新失败");
        }
        return true;
    }

    @Override
    public TagVO get(String id) {
        TagEntity tag = getById(id);
        if (tag == null) {
            throw new BusinessException("不存在该对象");
        }
        TagVO tagVO = new TagVO();
        BeanUtils.copyProperties(tag, tagVO);
        return tagVO;
    }

    @Override
    public List<TagVO> getList() {
        List<TagEntity> list = list(new LambdaQueryWrapper<TagEntity>()
                .orderByAsc(TagEntity::getId));
        return list.stream().map(entity -> {
            TagVO vo = new TagVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).toList();
    }

    private void validateName(String name, String excludeId) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("标签名称不能为空");
        }
        LambdaQueryWrapper<TagEntity> wrapper = new LambdaQueryWrapper<TagEntity>()
                .eq(TagEntity::getName, name.trim());
        if (excludeId != null && !excludeId.isBlank()) {
            wrapper.ne(TagEntity::getId, excludeId);
        }
        if (count(wrapper) > 0) {
            throw new BusinessException("标签名称已存在");
        }
    }
}
