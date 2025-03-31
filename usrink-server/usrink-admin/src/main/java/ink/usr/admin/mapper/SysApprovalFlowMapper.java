package ink.usr.admin.mapper;

import ink.usr.common.model.mysql.SysApprovalFlowModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysApprovalFlowMapper {

    @Select("select * from sys_approval_flow where approverId = #{approverId}")
    List<SysApprovalFlowModel> getApprovalFlowListByApproverId(Long approverId);
}
