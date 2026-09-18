package top.ehre.mod.tag.domain.vo;

import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 模组标签视图VO
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "Tag视图对象", description = "模组标签视图VO")
public class TagVO {

    @ApiModelProperty("ID")
    private String id;

    @ApiModelProperty("标签名称")
    private String name;

    @ApiModelProperty("标签颜色")
    private String color;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty("修改时间")
    private LocalDateTime updatedAt;
}
