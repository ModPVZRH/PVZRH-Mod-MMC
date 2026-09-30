package top.ehre.mod.system.setting.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SystemSettingVO {

    private Boolean registerEnabled;
}
