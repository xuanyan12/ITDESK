package ink.usr.admin.mapper;

import ink.usr.common.model.mysql.SysApproverModel;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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
}
