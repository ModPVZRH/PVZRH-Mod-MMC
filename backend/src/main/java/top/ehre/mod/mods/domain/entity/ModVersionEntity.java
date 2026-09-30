package top.ehre.mod.mods.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("mod_version")
@ApiModel(value = "modVersion对象", description = "模组版本")
public class ModVersionEntity {

    @ApiModelProperty("ID")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @ApiModelProperty("模组ID")
    private String modId;

    @ApiModelProperty("版本号")
    private String version;

    @ApiModelProperty("版本描述")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String description;

    @ApiModelProperty("直链下载地址")
    private String downloadDirectUrl;

    @ApiModelProperty("网盘下载地址")
    private String downloadCloudUrl;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty("修改时间")
    private LocalDateTime updatedAt;
}
