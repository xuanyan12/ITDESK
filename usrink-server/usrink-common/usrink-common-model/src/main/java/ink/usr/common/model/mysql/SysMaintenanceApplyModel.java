package ink.usr.common.model.mysql;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysMaintenanceApplyModel implements Serializable {
    // 维修申请ID
    private Long maintenanceId;
    // 使用人id
    private Long applicant;
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
    // 电脑编号
    private String ciName;
    // 申请状态：待处理,已处理,已取消
    private String applyStatus;
    // 创建时间
    private String createTime;
    // 更新时间
    private String updateTime;
} 