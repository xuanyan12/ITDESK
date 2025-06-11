package ink.usr.admin.dao.DTO;

import lombok.Data;

/**
 * 审批操作数据传输对象
 */
@Data
public class SysApprovalDTO {
    
    /**
     * 审批ID
     */
    private Long id;
    
    /**
     * 审批流程ID
     */
    private Long flowId;
    
    /**
     * 审批状态
     * 可选值: 审批通过, 审批不通过, 已通过, 已驳回
     */
    private String status;
    
    /**
     * 审批时间
     */
    private String approvedAt;
    
    /**
     * 审批理由
     */
    private String reason;
} 