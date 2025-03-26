package ink.usr.common.model.mysql;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysApprovalHistoryModel implements Serializable {
    // 审批历史id
    private Long historyId;
    // 审批请求id
    private Long approvalId;
    // 审批人id
    private Long approverId;
    // 审批动作
    private String action;
    // 审批操作时间
    private String timestamp;
    // 审批人对于申请的备注
    private String comments;
    
}
