package top.ehre.mod.category.domain.entity;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 模组分类
 */
@Data
@Accessors(chain = true)
@TableName("mod_category")
@ApiModel(value = "category对象", description = "模组分类")
public class CategoryEntity {

    @ApiModelProperty("ID")
    @TableId(value = "id", type = IdType.AUTO)
    private String id;

    @ApiModelProperty("分类名称")
    private String name;

    @ApiModelProperty("分类描述")
    private String description;

    @ApiModelProperty("排序")
    private Integer sortOrder;

    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

    @ApiModelProperty("修改时间")
    private LocalDateTime updatedAt;
}
