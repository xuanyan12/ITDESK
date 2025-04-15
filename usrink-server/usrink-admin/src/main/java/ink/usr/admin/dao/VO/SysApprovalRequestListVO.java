package ink.usr.admin.dao.VO;

import ink.usr.common.model.mysql.SysApprovalFlowModel;
import ink.usr.common.model.mysql.SysApprovalRequestModel;
import ink.usr.common.model.mysql.SysControlModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysApprovalRequestListVO extends SysApprovalFlowModel {
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
    // 电脑情形
    private String deviceSituation;
    // 公司系统
    private String companySystem;
    // 申请理由
    private String reason;
    // 申请人名称
    private String userName;
    // 责任人名称
    private String responsibilityName;
}
