package ink.usr.admin.dao.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysMaintenanceApplyRequestDTO {
    // 责任人name
    private String responsibilityName;
    // 使用人userName
    private String userName;
    // 电脑名称
    private String computerName;
    // 维修类别
    private String fixCategory;
    // 成本中心
    private String costCenter;
    // 所属公司
    private String company;
    // 责任人
    private String responsiblility;
    // 故障描述
    private String problemDescription;
}
