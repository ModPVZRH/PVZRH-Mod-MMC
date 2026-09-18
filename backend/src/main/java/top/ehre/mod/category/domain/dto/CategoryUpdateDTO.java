package top.ehre.mod.category.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 模组分类修改DTO
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "Category修改对象", description = "模组分类修改DTO")
public class CategoryUpdateDTO {

    @ApiModelProperty("ID")
    private String id;

    @ApiModelProperty("分类名称")
    private String name;

    @ApiModelProperty("分类描述")
    private String description;

    @ApiModelProperty("排序")
    private Integer sortOrder;
}
