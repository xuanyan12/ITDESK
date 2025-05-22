package ink.usr.admin.service;

import ink.usr.admin.dao.VO.SysApprovalFlowVO;
import ink.usr.admin.dao.VO.SysApproversVO;
import ink.usr.common.model.mysql.SysApprovalFlowModel;

import java.util.List;

public interface SysApprovalFlowService {


    List<SysApprovalFlowVO> getApprovalFlowListByApproverId(Long approverId, Long approvalType);

    SysApproversVO getApproversByAprrovalId(Long aprrovalId);
    
    /**
     * 验证审批token是否有效
     * @param flowId 审批流ID
     * @param token 临时token
     * @return 是否有效
     */
    boolean validateApprovalToken(Long flowId, String token);
    
    /**
     * 获取审批流信息
     * @param flowId 审批流ID
     * @return 审批流信息
     */
    SysApprovalFlowModel getApprovalFlowById(Long flowId);
    
    /**
     * 更新审批状态
     * @param flowId 审批流ID
     * @param requestId 申请ID
     * @param status 审批状态
     * @param reason 审批理由
     * @return 是否成功
     */
    boolean updateApprovalStatus(Long flowId, Long requestId, String status, String reason);
    
    /**
     * 获取审批流数量
     * @param approverId 审批人ID
     * @param approvalType 审批类型（0：待处理，1：已处理）
     * @return 审批流数量
     */
    int getApprovalFlowCountByApproverId(Long approverId, Long approvalType);
}
