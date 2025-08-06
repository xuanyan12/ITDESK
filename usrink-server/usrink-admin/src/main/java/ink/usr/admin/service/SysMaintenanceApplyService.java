package ink.usr.admin.service;

import ink.usr.admin.dao.DTO.SysMaintenanceApplyRequestDTO;
import ink.usr.admin.dao.VO.SysApplyListVO;
import ink.usr.admin.dao.VO.SysMaintenanceApplyVO;
import ink.usr.common.model.mysql.SysMaintenanceApplyModel;

import java.util.List;

public interface SysMaintenanceApplyService {

    /**
     * 获取维修申请列表（兼容原有接口）
     * @param userId 用户ID
     * @return 申请列表
     */
    List<SysApplyListVO> getApplyList(Long userId);

    /**
     * 添加维修申请
     * @param sysMaintenanceApplyRequestDTO 维修申请请求DTO
     * @return 添加结果
     */
    String addApply(SysMaintenanceApplyRequestDTO sysMaintenanceApplyRequestDTO);

    /**
     * 根据ID查询维修申请
     * @param maintenanceId 维修申请ID
     * @return 维修申请VO
     */
    SysMaintenanceApplyVO getApplyById(Long maintenanceId);

    /**
     * 根据申请人查询维修申请
     * @param applicant 申请人ID
     * @return 维修申请VO列表
     */
    List<SysMaintenanceApplyVO> getAppliesByApplicant(Long applicant);

    /**
     * 查询所有维修申请
     * @return 维修申请VO列表
     */
    List<SysMaintenanceApplyVO> getAllApplies();

    /**
     * 根据状态查询维修申请
     * @param applyStatus 申请状态
     * @return 维修申请VO列表
     */
    List<SysMaintenanceApplyVO> getAppliesByStatus(String applyStatus);

    /**
     * 更新维修申请
     * @param applyModel 维修申请模型
     * @return 更新结果
     */
    boolean updateApply(SysMaintenanceApplyModel applyModel);

    /**
     * 更新申请状态
     * @param maintenanceId 维修申请ID
     * @param applyStatus 申请状态
     * @return 更新结果
     */
    boolean updateApplyStatus(Long maintenanceId, String applyStatus);

    /**
     * 删除维修申请
     * @param maintenanceId 维修申请ID
     * @return 删除结果
     */
    boolean deleteApply(Long maintenanceId);

    /**
     * 根据维修类别获取维修类别名称
     * @param fixCategory 维修类别
     * @return 维修类别名称
     */
    String getFixCategoryName(String fixCategory);
}
