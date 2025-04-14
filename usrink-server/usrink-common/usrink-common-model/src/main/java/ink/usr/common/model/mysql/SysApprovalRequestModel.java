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
public class SysApprovalRequestModel implements Serializable {
    // 审批请求id
    private Long approvalId;
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
    // 请求有效期（过期时间）
    private String timestamp;
    // 当前审批状态
    private String status;
    // 审批请求时间
    private String createdAt;
    // 审批请求更新时间
    private String updatedAt;

}
