package ink.usr.admin.mapper;

import ink.usr.common.model.mysql.SysMaintenanceApprovalModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysMaintenanceApprovalMapper {
    
    /**
     * 插入维修审批
     * @param approval 维修审批
     * @return 影响行数
     */
    int insertApproval(SysMaintenanceApprovalModel approval);
    
    /**
     * 根据ID查询维修审批
     * @param approvalId 审批ID
     * @return 维修审批
     */
    SysMaintenanceApprovalModel selectApprovalById(@Param("approvalId") Long approvalId);
    
    /**
     * 根据订单ID查询维修审批
     * @param orderId 订单ID
     * @return 维修审批列表
     */
    List<SysMaintenanceApprovalModel> selectApprovalsByOrderId(@Param("orderId") Long orderId);
    
    /**
     * 根据审批人ID查询维修审批
     * @param approverId 审批人ID
     * @return 维修审批列表
     */
    List<SysMaintenanceApprovalModel> selectApprovalsByApproverId(@Param("approverId") Long approverId);
    
    /**
     * 根据订单ID和审批级别查询维修审批
     * @param orderId 订单ID
     * @param approvalLevel 审批级别
     * @return 维修审批
     */
    SysMaintenanceApprovalModel selectApprovalByOrderIdAndLevel(@Param("orderId") Long orderId, 
                                                               @Param("approvalLevel") Integer approvalLevel);
    
    /**
     * 查询所有维修审批
     * @return 维修审批列表
     */
    List<SysMaintenanceApprovalModel> selectAllApprovals();
    
    /**
     * 根据状态查询维修审批
     * @param approvalStatus 审批状态
     * @return 维修审批列表
     */
    List<SysMaintenanceApprovalModel> selectApprovalsByStatus(@Param("approvalStatus") String approvalStatus);
    
    /**
     * 更新维修审批
     * @param approval 维修审批
     * @return 影响行数
     */
    int updateApproval(SysMaintenanceApprovalModel approval);
    
    /**
     * 更新审批状态
     * @param approvalId 审批ID
     * @param approvalStatus 审批状态
     * @param approvalTime 审批时间
     * @param approvalReason 审批理由
     * @return 影响行数
     */
    int updateApprovalStatus(@Param("approvalId") Long approvalId,
                            @Param("approvalStatus") String approvalStatus,
                            @Param("approvalTime") String approvalTime,
                            @Param("approvalReason") String approvalReason);
    
    /**
     * 删除维修审批
     * @param approvalId 审批ID
     * @return 影响行数
     */
    int deleteApproval(@Param("approvalId") Long approvalId);
    
    /**
     * 根据订单ID删除维修审批
     * @param orderId 订单ID
     * @return 影响行数
     */
    int deleteApprovalsByOrderId(@Param("orderId") Long orderId);
} 