package top.ehre.mod.category.mapper;

import top.ehre.mod.category.domain.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.ehre.mod.category.domain.dto.CategoryPageDTO;
import top.ehre.mod.category.domain.vo.CategoryVO;
import org.springframework.stereotype.Component;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.jdbc.SQL;

import java.util.List;

/**
 * 模组分类 Mapper
 */
@Mapper
@Component
public interface CategoryMapper extends BaseMapper<CategoryEntity> {

    @SelectProvider(type = CategorySqlProvider.class, method = "queryPage")
    List<CategoryVO> queryPage(Page page, @Param("pageDTO") CategoryPageDTO pageDTO);

    class CategorySqlProvider {
        public String queryPage(final Page page, final CategoryPageDTO pageDTO) {
            return new SQL() {{
                SELECT("id,name,description,sort_order,created_at,updated_at");
                FROM("mod_category");
                if (pageDTO != null) {
                    if (pageDTO.getName() != null && !pageDTO.getName().isBlank()) {
                        WHERE("INSTR(name, #{pageDTO.name})");
                    }
                }
            }}.toString();
        }
    }
}
