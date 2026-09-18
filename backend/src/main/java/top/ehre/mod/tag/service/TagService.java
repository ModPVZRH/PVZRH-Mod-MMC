package top.ehre.mod.tag.service;

import top.ehre.mod.tag.domain.entity.TagEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import top.ehre.mod.tag.domain.vo.TagVO;
import top.ehre.mod.tag.domain.dto.TagPageDTO;
import top.ehre.mod.tag.domain.dto.TagAddDTO;
import top.ehre.mod.tag.domain.dto.TagUpdateDTO;
import top.ehre.mod.util.PageResult;

import java.util.List;

/**
 * 模组标签服务
 */
public interface TagService extends IService<TagEntity> {

    PageResult<TagVO> page(TagPageDTO tagPageDTO);

    boolean add(TagAddDTO tagAddDTO);

    boolean delete(String id);

    boolean batchDelete(List<String> ids);

    boolean update(TagUpdateDTO tagUpdateDTO);

    TagVO get(String id);

    List<TagVO> getList();
}
