package top.ehre.mod.mods.mapper;

import top.ehre.mod.mods.domain.entity.ModsEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.ehre.mod.mods.domain.dto.ModsPageDTO;
import top.ehre.mod.mods.domain.vo.ModsVO;
import top.ehre.mod.tag.domain.vo.TagVO;
import org.springframework.stereotype.Component;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.jdbc.SQL;

import java.util.List;

/**
 *  Mapper 接口
 *
 * @author LibrhHp_0928
 * @since 2025-11-24 18:58:33
 */
@Mapper
@Component
public interface ModsMapper extends BaseMapper<ModsEntity> {
    @SelectProvider(type = ModsSqlProvider.class, method = "queryPage")
    List<ModsVO> queryPage(Page page, @Param("pageDTO") ModsPageDTO pageDTO);
    int addOtherAuthor(@Param("id") String modId, @Param("authorId") String authorId);

    List<String> getOtherAuthors(@Param("id") String modId);

    int deleteOtherAuthor(@Param("id") String modId);

    int incrementDownloadCount(@Param("id") String id);

    int incrementViewCount(@Param("id") String id);

    int addModTag(@Param("id") String modId, @Param("tagId") String tagId);

    int deleteModTags(@Param("id") String modId);

    int deleteModTagsByTagId(@Param("tagId") String tagId);

    List<TagVO> getTags(@Param("id") String modId);

    class ModsSqlProvider {
        public String queryPage(final Page page, final ModsPageDTO pageDTO) {
            return new SQL() {{
                SELECT("mods.id,mods.mod_name,mods.english_name,mods.author_id,mods.mod_description,mods.icon_url,mods.video_url,mods.game_name,mods.supported_versions,mods.is_preposition,mods.is_modpack,mods.framework_name,mods.show_direct_url,mods.download_direct_url,mods.download_cloud_url,mods.version,mods.file_size,mods.download_count,mods.view_count,mods.is_approved,mods.is_featured,mods.is_visible,mods.created_at,mods.updated_at,mods.category_id,mod_category.name AS category_name");
                FROM("mods");
                LEFT_OUTER_JOIN("mod_category ON mods.category_id = mod_category.id");
                if (pageDTO != null) {
                    if (pageDTO.getAuthorId() != null && !pageDTO.getAuthorId().isBlank()){
                        WHERE("mods.author_id = #{pageDTO.authorId}");
                    }
                    if (pageDTO.getModName() != null && !pageDTO.getModName().isBlank()){
                        WHERE("INSTR(mods.mod_name, #{pageDTO.modName})");
                    }
                    if (pageDTO.getCategoryId() != null && !pageDTO.getCategoryId().isBlank()){
                        WHERE("mods.category_id = #{pageDTO.categoryId}");
                    }
                    if (pageDTO.getTagId() != null && !pageDTO.getTagId().isBlank()){
                        WHERE("mods.id IN (SELECT mod_id FROM mods_tag WHERE tag_id = #{pageDTO.tagId})");
                    }
                }
            }}.toString();
        }
    }
    

}
