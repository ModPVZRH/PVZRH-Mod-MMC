package top.ehre.mod.mods.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;
import top.ehre.mod.mods.domain.entity.ModVersionEntity;

/**
 * 模组版本
 */
@Mapper
@Component
public interface ModVersionMapper extends BaseMapper<ModVersionEntity> {
}
