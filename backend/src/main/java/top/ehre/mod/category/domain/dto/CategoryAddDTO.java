package top.ehre.mod.category.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 模组分类新增DTO
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "Category新增对象", description = "模组分类新增DTO")
public class CategoryAddDTO {

    @ApiModelProperty("分类名称")
    private String name;

    @ApiModelProperty("分类描述")
    private String description;

    @ApiModelProperty("排序")
    private Integer sortOrder;
}
