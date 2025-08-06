package ink.usr.admin.service;

import ink.usr.admin.dao.DTO.SysMaintenanceApprovalDTO;
import ink.usr.admin.dao.VO.SysMaintenanceApprovalVO;
import ink.usr.common.model.mysql.SysMaintenanceApprovalModel;

import java.util.List;

public interface SysMaintenanceApprovalService {
    
    /**
     * 创建维修审批
     * @param approvalDTO 维修审批DTO
     * @return 创建结果
     */
    boolean createApproval(SysMaintenanceApprovalDTO approvalDTO);
    
    /**
     * 根据ID查询维修审批
     * @param approvalId 审批ID
     * @return 维修审批VO
     */
    SysMaintenanceApprovalVO getApprovalById(Long approvalId);
    
    /**
     * 根据订单ID查询维修审批
     * @param orderId 订单ID
     * @return 维修审批VO列表
     */
    List<SysMaintenanceApprovalVO> getApprovalsByOrderId(Long orderId);
    
    /**
     * 根据审批人ID查询维修审批
     * @param approverId 审批人ID
     * @return 维修审批VO列表
     */
    List<SysMaintenanceApprovalVO> getApprovalsByApproverId(Long approverId);

    /**
     * 根据审批人ID查询维修审批（包含订单详细信息）
     * @param approverId 审批人ID
     * @param approvalType 审批类型：0-待办审批，1-审批历史
     * @return 维修审批VO列表（包含订单详细信息）
     */
    List<SysMaintenanceApprovalVO> getApprovalsWithOrderDetailsByApproverId(Long approverId, Long approvalType);
    
    /**
     * 根据订单ID和审批级别查询维修审批
     * @param orderId 订单ID
     * @param approvalLevel 审批级别
     * @return 维修审批VO
     */
    SysMaintenanceApprovalVO getApprovalByOrderIdAndLevel(Long orderId, Integer approvalLevel);
    
    /**
     * 查询所有维修审批
     * @return 维修审批VO列表
     */
    List<SysMaintenanceApprovalVO> getAllApprovals();
    
    /**
     * 根据状态查询维修审批
     * @param approvalStatus 审批状态
     * @return 维修审批VO列表
     */
    List<SysMaintenanceApprovalVO> getApprovalsByStatus(String approvalStatus);
    
    /**
     * 更新维修审批
     * @param approvalModel 维修审批模型
     * @return 更新结果
     */
    boolean updateApproval(SysMaintenanceApprovalModel approvalModel);
    
    /**
     * 更新审批状态
     * @param approvalId 审批ID
     * @param approvalStatus 审批状态
     * @param approvalReason 审批理由
     * @return 更新结果
     */
    boolean updateApprovalStatus(Long approvalId, String approvalStatus, String approvalReason);
    
    /**
     * 删除维修审批
     * @param approvalId 审批ID
     * @return 删除结果
     */
    boolean deleteApproval(Long approvalId);
    
    /**
     * 根据订单ID删除维修审批
     * @param orderId 订单ID
     * @return 删除结果
     */
    boolean deleteApprovalsByOrderId(Long orderId);
    
    /**
     * 根据审批级别获取审批级别名称
     * @param approvalLevel 审批级别
     * @return 审批级别名称
     */
    String getApprovalLevelName(Integer approvalLevel);
    
    /**
     * 处理审批流程
     * @param orderId 订单ID
     * @param approvalLevel 审批级别
     * @param approvalStatus 审批状态
     * @param approvalReason 审批理由
     * @return 处理结果
     */
    String processApproval(Long orderId, Integer approvalLevel, String approvalStatus, String approvalReason);
} 