package ink.usr.admin.dao.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysMaintenanceApprovalDTO {
    // 维修订单ID
    private Long orderId;
    // 审批人ID
    private Long approverId;
    // 审批人姓名
    private String approverName;
    // 审批级别：1-成本负责人,2-IT负责人
    private Integer approvalLevel;
    // 审批状态：待审批,已通过,已驳回
    private String approvalStatus;
    // 审批理由
    private String approvalReason;
    // IT操作（仅IT负责人审批时使用）
    private String itOperation;
} 