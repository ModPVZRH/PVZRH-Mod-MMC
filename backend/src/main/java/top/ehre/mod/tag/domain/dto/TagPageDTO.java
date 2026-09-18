package top.ehre.mod.tag.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import top.ehre.mod.util.PageParam;

/**
 * 模组标签分页查询DTO
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "Tag分页查询对象", description = "模组标签分页查询DTO")
public class TagPageDTO extends PageParam {

    @ApiModelProperty("标签名称")
    private String name;
}
