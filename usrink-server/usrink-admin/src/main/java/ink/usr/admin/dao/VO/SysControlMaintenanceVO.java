package ink.usr.admin.dao.VO;

import ink.usr.common.model.mysql.SysControlMaintenanceModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysControlMaintenanceVO extends SysControlMaintenanceModel {

    // 电脑使用情况
    private String pcClass;

    // 电脑状态
    private String pcStatus;

    // 电脑类型
    private String deviceClass;

    // 制造商
    private String manufacture;

    // 电脑型号
    private String modelOrVersion;

    // 出厂时间
    private String lifeCycleStart;

    // 供应商公司
    private String vendor;

}
