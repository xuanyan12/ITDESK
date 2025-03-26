package ink.usr.admin.mapper;

import ink.usr.admin.dao.DTO.SysApplyDTO;
import ink.usr.common.model.mysql.SysApprovalRequestModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysApplyMapper {


    List<SysApprovalRequestModel> getApplyList(Long userId);

    void addApply(SysApprovalRequestModel sysApprovalRequestModel);
}
