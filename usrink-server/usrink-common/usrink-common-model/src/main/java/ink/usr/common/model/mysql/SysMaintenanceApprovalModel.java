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
public class SysMaintenanceApprovalModel implements Serializable {
    // 审批ID
    private Long approvalId;
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
    // 审批时间
    private String approvalTime;
    // 审批理由
    private String approvalReason;
    // 创建时间
    private String createTime;
    // 更新时间
    private String updateTime;
} 