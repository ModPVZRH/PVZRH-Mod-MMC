package top.ehre.mod.tag.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 模组标签修改DTO
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "Tag修改对象", description = "模组标签修改DTO")
public class TagUpdateDTO {

    @ApiModelProperty("ID")
    private String id;

    @ApiModelProperty("标签名称")
    private String name;

    @ApiModelProperty("标签颜色")
    private String color;
}
