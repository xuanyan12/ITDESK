package ink.usr.admin.dao.DTO;

import lombok.Data;

/**
 * 临时审批DTO
 * 用于接收前端传递的审批数据
 */
@Data
public class TempApprovalDTO {
    /**
     * 流程ID
     */
    private Long flowId;
    
    /**
     * 验证令牌
     */
    private String token;
    
    /**
     * 审批ID
     */
    private Long id;
    
    /**
     * 审批状态
     * 可选值: "审批通过" 或 "审批不通过"
     */
    private String status;
    
    /**
     * 审批理由
     */
    private String reason;
} 