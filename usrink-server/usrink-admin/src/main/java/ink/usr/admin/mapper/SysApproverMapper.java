package ink.usr.admin.mapper;

import ink.usr.common.model.mysql.SysApproverModel;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SysApproverMapper {

    @Select("select approverId from sys_approver where userId = #{userId}")
    Long getApproverIdByUserId(Long userId);

    @Select("select role from sys_approver where approverId = #{approverId}")
    String getApproverRoleByApproverId(Long approverId);

    @Select("select approverId from sys_approver where role = 'ITApprover'")
    Long getITApprover();

    void insertITApprover(SysApproverModel sysApproverModel);

    @Delete("delete from sys_approver where name = #{NTAccount}")
    void deleteByNTAccount(String NTAccount);
    
    /**
     * 获取所有审批人信息
     * @return 审批人列表
     */
    @Select("SELECT * FROM sys_approver")
    List<SysApproverModel> getAllApprovers();
    
    /**
     * 根据角色获取审批人列表
     * @param role 角色名称
     * @return 审批人列表
     */
    @Select("SELECT * FROM sys_approver WHERE role = #{role}")
    List<SysApproverModel> getApproversByRole(String role);
    
    /**
     * 更新审批人信息
     * @param approver 审批人信息
     * @return 更新的记录数
     */
    int updateApprover(SysApproverModel approver);
    
    /**
     * 插入新的审批人
     * @param approver 审批人信息
     * @return 插入的记录数
     */
    @Insert("INSERT INTO sys_approver(name, email, role, costCenter, createdAt, updatedAt, userId) " +
            "VALUES(#{name}, #{email}, #{role}, #{costCenter}, #{createdAt}, #{updatedAt}, #{userId})")
    int insertApprover(SysApproverModel approver);
    
    /**
     * 根据ID删除审批人
     * @param id 审批人ID
     * @return 删除的记录数
     */
    @Delete("DELETE FROM sys_approver WHERE approverId = #{id}")
    int deleteApproverById(Long id);

    @Select("select DISTINCT approverId from sys_approver where userId = #{userId}")
    List<Long> getApproverIdListBySingleApproverId(Long userId);

    @Select("select approverId from sys_approver where costCenter = #{costCenter}")
    Long getApproverIdUseCostCenter(@Param("userId") Long userId, @Param("costCenter") String costCenter);


    @Select("select * from sys_approver where approverId = #{approverId}")
    SysApproverModel getApproverInfoByApproverId(Long approverId);

    @Select("select distinct costCenter from sys_approver")
    List<String> getCostCenterList();
}
