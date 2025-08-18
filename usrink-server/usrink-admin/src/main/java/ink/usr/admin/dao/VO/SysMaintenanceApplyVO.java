package ink.usr.admin.dao.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysMaintenanceApplyVO {
    // 维修申请ID
    private Long maintenanceId;
    // 使用人id
    private Long applicant;
    // 使用人姓名
    private String applicantName;
    // 维修类别
    private String fixCategory;
    // 维修类别名称
    private String fixCategoryName;
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