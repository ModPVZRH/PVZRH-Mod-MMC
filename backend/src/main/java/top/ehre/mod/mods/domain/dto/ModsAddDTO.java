package top.ehre.mod.mods.domain.dto;

import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import java.util.List;


/**
 * 新增DTO
 *
 * @author LibrhHp_0928
 * @since 2025-11-24 18:58:33
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "Mods新增对象", description = "新增DTO")
public class ModsAddDTO {

    @ApiModelProperty("ID")
    private String id;

    @ApiModelProperty("Mod名称")
    private String modName;

    @ApiModelProperty("Mod英文名")
    private String englishName;

    @ApiModelProperty("作者ID")
    private String authorId;

    @ApiModelProperty("分类ID")
    private String categoryId;

    @ApiModelProperty("标签ID列表")
    private List<String> tagIds;

    @ApiModelProperty("Mod介绍")
    private String modDescription;

    @ApiModelProperty("模组图标")
    private String iconUrl;

    @ApiModelProperty("视频地址")
    private String videoUrl;

    @ApiModelProperty("支持游戏")
    private String gameName;

    @ApiModelProperty("支持版本")
    private String supportedVersions;

    @ApiModelProperty("是否前置")
    private Boolean isPreposition;

    @ApiModelProperty("是否整合包")
    private Boolean isModpack;

    @ApiModelProperty("Mod框架")
    private String frameworkName;

    @ApiModelProperty("是否显示直链")
    private Boolean showDirectUrl;

    @ApiModelProperty("直链下载地址")
    private String downloadDirectUrl;

    @ApiModelProperty("网盘下载地址")
    private String downloadCloudUrl;

    @ApiModelProperty("Mod版本")
    private String version;

    @ApiModelProperty("文件大小(")
    private Long fileSize;

    @ApiModelProperty("下载次数")
    private String downloadCount;

    @ApiModelProperty("查看次数")
    private String viewCount;

    @ApiModelProperty("是否通过审核")
    private Boolean isApproved;

    @ApiModelProperty("是否推荐")
    private Boolean isFeatured;

    @ApiModelProperty("是否可见")
    private Boolean isVisible;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty("修改时间")
    private LocalDateTime updatedAt;

}
