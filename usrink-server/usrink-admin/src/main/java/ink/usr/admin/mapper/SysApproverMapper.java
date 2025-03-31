package ink.usr.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysApproverMapper {

    @Select("select approverId from sys_approver where userId = #{userId}")
    Long getApproverIdByUserId(Long userId);
}
