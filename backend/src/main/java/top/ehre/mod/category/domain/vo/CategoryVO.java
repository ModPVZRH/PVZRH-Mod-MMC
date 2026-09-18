package top.ehre.mod.category.domain.vo;

import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 模组分类视图VO
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "Category视图对象", description = "模组分类视图VO")
public class CategoryVO {

    @ApiModelProperty("ID")
    private String id;

    @ApiModelProperty("分类名称")
    private String name;

    @ApiModelProperty("分类描述")
    private String description;

    @ApiModelProperty("排序")
    private Integer sortOrder;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty("修改时间")
    private LocalDateTime updatedAt;
}
