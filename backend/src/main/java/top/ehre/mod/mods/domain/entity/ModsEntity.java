package top.ehre.mod.mods.domain.entity;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 
 *
 * @author LibrhHp_0928
 * @since 2025-11-24 18:58:33
 */
@Data
@Accessors(chain = true)
@TableName("mods")
@ApiModel(value = "mods对象", description = "")
public class ModsEntity {


    @ApiModelProperty("ID")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @ApiModelProperty("Mod名称")
    private String modName;

    @ApiModelProperty("Mod英文名")
    private String englishName;

    @ApiModelProperty("作者ID")
    private String authorId;

    @ApiModelProperty("Mod介绍")
    private String modDescription;

    @ApiModelProperty("分类ID")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String categoryId;

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

    @ApiModelProperty("是否展示直链")
    private Boolean showDirectUrl;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty("修改时间")
    private LocalDateTime updatedAt;
}
