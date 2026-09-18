package top.ehre.mod.tag.mapper;

import top.ehre.mod.tag.domain.entity.TagEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.ehre.mod.tag.domain.dto.TagPageDTO;
import top.ehre.mod.tag.domain.vo.TagVO;
import org.springframework.stereotype.Component;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.jdbc.SQL;

import java.util.List;

/**
 * 模组标签 Mapper
 */
@Mapper
@Component
public interface TagMapper extends BaseMapper<TagEntity> {

    @SelectProvider(type = TagSqlProvider.class, method = "queryPage")
    List<TagVO> queryPage(Page page, @Param("pageDTO") TagPageDTO pageDTO);

    class TagSqlProvider {
        public String queryPage(final Page page, final TagPageDTO pageDTO) {
            return new SQL() {{
                SELECT("id,name,color,created_at,updated_at");
                FROM("mod_tag");
                if (pageDTO != null) {
                    if (pageDTO.getName() != null && !pageDTO.getName().isBlank()) {
                        WHERE("INSTR(name, #{pageDTO.name})");
                    }
                }
            }}.toString();
        }
    }
}
