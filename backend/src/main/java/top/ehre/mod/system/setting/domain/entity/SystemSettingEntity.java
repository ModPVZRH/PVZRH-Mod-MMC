package top.ehre.mod.system.setting.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 系统设置，表中只保留 id = 1 的一行。
 */
@Data
@Accessors(chain = true)
@TableName("system_setting")
public class SystemSettingEntity {

    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

    private Boolean registerEnabled;

    private LocalDateTime updateTime;
}
