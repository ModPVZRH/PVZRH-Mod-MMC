package top.ehre.mod.mods.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 新增模组版本
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "模组版本新增对象", description = "新增模组版本")
public class ModVersionAddDTO {

    @ApiModelProperty("版本号")
    private String version;

    @ApiModelProperty("版本描述")
    private String description;

    @ApiModelProperty("直链下载地址")
    private String downloadDirectUrl;

    @ApiModelProperty("网盘下载地址")
    private String downloadCloudUrl;

    @ApiModelProperty("是否设为当前版本，默认是")
    private Boolean current;
}
