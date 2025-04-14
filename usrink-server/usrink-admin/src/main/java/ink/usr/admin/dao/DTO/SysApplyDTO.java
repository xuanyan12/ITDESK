package ink.usr.admin.dao.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysApplyDTO {
    // 使用人id
    private Long applicant;
    // 申请类别
    private String deviceCategory;
    // 电脑类型
    private String deviceType;
    // 成本中心
    private String costCenter;
    // 所属公司
    private String company;
    // 责任人
    private String responsiblility;
    // 电脑情形
    private String deviceSituation;
    // 公司系统
    private String companySystem;
    // 申请理由
    private String reason;

}
