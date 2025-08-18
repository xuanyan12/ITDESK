package ink.usr.admin.mapper;

import ink.usr.common.model.mysql.SysMaintenanceApplyModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysMaintenanceApplyMapper {
    
    /**
     * 插入维修申请
     * @param apply 维修申请
     * @return 影响行数
     */
    int insertApply(SysMaintenanceApplyModel apply);
    
    /**
     * 根据ID查询维修申请
     * @param maintenanceId 维修申请ID
     * @return 维修申请
     */
    SysMaintenanceApplyModel selectApplyById(@Param("maintenanceId") Long maintenanceId);
    
    /**
     * 根据申请人查询维修申请
     * @param applicant 申请人ID
     * @return 维修申请列表
     */
    List<SysMaintenanceApplyModel> selectAppliesByApplicant(@Param("applicant") Long applicant);
    
    /**
     * 查询所有维修申请
     * @return 维修申请列表
     */
    List<SysMaintenanceApplyModel> selectAllApplies();
    
    /**
     * 根据状态查询维修申请
     * @param applyStatus 申请状态
     * @return 维修申请列表
     */
    List<SysMaintenanceApplyModel> selectAppliesByStatus(@Param("applyStatus") String applyStatus);
    
    /**
     * 更新维修申请
     * @param apply 维修申请
     * @return 影响行数
     */
    int updateApply(SysMaintenanceApplyModel apply);
    
    /**
     * 更新申请状态
     * @param maintenanceId 维修申请ID
     * @param applyStatus 申请状态
     * @return 影响行数
     */
    int updateApplyStatus(@Param("maintenanceId") Long maintenanceId, @Param("applyStatus") String applyStatus);
    
    /**
     * 删除维修申请
     * @param maintenanceId 维修申请ID
     * @return 影响行数
     */
    int deleteApply(@Param("maintenanceId") Long maintenanceId);
} 