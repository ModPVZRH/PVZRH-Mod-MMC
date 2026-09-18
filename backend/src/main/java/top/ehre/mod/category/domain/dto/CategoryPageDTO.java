package top.ehre.mod.category.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import top.ehre.mod.util.PageParam;

/**
 * 模组分类分页查询DTO
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "Category分页查询对象", description = "模组分类分页查询DTO")
public class CategoryPageDTO extends PageParam {

    @ApiModelProperty("分类名称")
    private String name;
}
