package top.ehre.mod.tag.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 模组标签新增DTO
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "Tag新增对象", description = "模组标签新增DTO")
public class TagAddDTO {

    @ApiModelProperty("标签名称")
    private String name;

    @ApiModelProperty("标签颜色")
    private String color;
}
