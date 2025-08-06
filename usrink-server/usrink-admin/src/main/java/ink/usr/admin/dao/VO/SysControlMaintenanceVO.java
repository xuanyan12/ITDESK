package ink.usr.admin.dao.VO;

import ink.usr.common.model.mysql.SysControlMaintenanceModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysControlMaintenanceVO extends SysControlMaintenanceModel {
    // 维修记录VO - 只包含维修相关信息，不包含设备详细信息
    // 所有字段都已继承自SysControlMaintenanceModel
}
