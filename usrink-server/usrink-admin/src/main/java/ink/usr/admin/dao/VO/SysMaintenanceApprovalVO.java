package ink.usr.admin.dao.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysMaintenanceApprovalVO {
    // 审批ID
    private Long approvalId;
    // 维修订单ID
    private Long orderId;
    // 订单编号
    private String orderNumber;
    // 审批人ID
    private Long approverId;
    // 审批人姓名
    private String approverName;
    // 审批级别：1-成本负责人,2-IT负责人
    private Integer approvalLevel;
    // 审批级别名称
    private String approvalLevelName;
    // 审批状态：待审批,已通过,已驳回
    private String approvalStatus;
    // 审批时间
    private String approvalTime;
    // 审批理由
    private String approvalReason;
    // 创建时间
    private String createTime;
    // 更新时间
    private String updateTime;
    
    // 维修订单相关字段
    // 电脑名
    private String ciName;
    // 故障描述
    private String problemDescription;
    // 成本中心
    private String costCenter;
    // 申请人
    private String applicant;
    // 订单类型
    private String orderType;
    // 订单状态
    private String orderStatus;
    // 申请人姓名
    private String applicantName;
    // 责任人
    private String responsiblePerson;
    // 流程信号：0-流程未到达，1-流程已到达
    private Integer status1Signal;
} 