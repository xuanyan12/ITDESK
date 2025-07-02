package ink.usr.admin.mapper;

import ink.usr.admin.dao.DTO.SysApplyDTO;
import ink.usr.admin.dao.VO.SysApplyListVO;
import ink.usr.admin.dao.VO.SysApprovalRequestListVO;
import ink.usr.common.model.mysql.SysApprovalFlowModel;
import ink.usr.common.model.mysql.SysApprovalRequestModel;
import ink.usr.common.model.mysql.SysApprovalTokenModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysApplyMapper {


    List<SysApprovalRequestModel> getApplyList(Long userId);

    void addApply(SysApprovalRequestModel sysApprovalRequestModel);

    String getCostCenterByApplyUserId(Long userId);

    Long getApproverIdByCostCenter(String costCenter);

    void addApplyFlow(SysApprovalFlowModel sysApprovalFlowModel);

    @Select("select * from sys_approval_request where approvalId = #{approvalId}")
    SysApprovalRequestModel getByApprovalId(Long approvalId);

    void InsertToken(SysApprovalTokenModel sysApprovalTokenModel);

     // 新增：优化的查询方法，使用JOIN减少数据库往返
     SysApprovalRequestListVO getApprovalInfoWithUserNames(Long approvalId);
}
