package ink.usr.admin.service;

import ink.usr.admin.dao.VO.SysApproversVO;
import ink.usr.common.model.mysql.SysApprovalFlowModel;

import java.util.List;

public interface SysApprovalFlowService {


    List<SysApprovalFlowModel> getApprovalFlowListByApproverId(Long approverId, Long approvalType);

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
     * @return 是否成功
     */
    boolean updateApprovalStatus(Long flowId, Long requestId, String status);
}
