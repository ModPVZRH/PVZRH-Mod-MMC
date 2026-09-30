package top.ehre.mod.mods.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 模组版本
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "模组版本视图对象", description = "模组版本")
public class ModVersionVO {

    @ApiModelProperty("ID")
    private String id;

    @ApiModelProperty("模组ID")
    private String modId;

    @ApiModelProperty("版本号")
    private String version;

    @ApiModelProperty("版本描述")
    private String description;

    @ApiModelProperty("直链下载地址")
    private String downloadDirectUrl;

    @ApiModelProperty("网盘下载地址")
    private String downloadCloudUrl;

    @ApiModelProperty("是否当前版本")
    private Boolean current;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty("修改时间")
    private LocalDateTime updatedAt;
}
