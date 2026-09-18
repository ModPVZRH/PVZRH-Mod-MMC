package top.ehre.mod.mods.domain.dto;

import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import top.ehre.mod.util.PageParam;

/**
 * 分页查询DTO
 *
 * @author LibrhHp_0928
 * @since 2025-11-24 18:58:33
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "Mods分页查询对象", description = "分页查询DTO")
public class ModsPageDTO extends PageParam {
    @ApiModelProperty(value = "作者id")
    private String authorId;
    @ApiModelProperty(value = "模组名称")
    private String modName;
    @ApiModelProperty(value = "分类ID")
    private String categoryId;
    @ApiModelProperty(value = "标签ID")
    private String tagId;
}
