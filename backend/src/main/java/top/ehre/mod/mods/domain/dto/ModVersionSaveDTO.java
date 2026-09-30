package top.ehre.mod.mods.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 保存模组版本。id 为空时视为新增。
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "模组版本保存对象", description = "保存模组版本")
public class ModVersionSaveDTO {

    @ApiModelProperty("ID，新增时为空")
    private String id;

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
}
