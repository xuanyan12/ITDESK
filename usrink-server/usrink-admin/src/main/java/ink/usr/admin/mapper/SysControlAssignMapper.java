package ink.usr.admin.mapper;

import ink.usr.admin.dao.VO.SysControlAssignListVO;
import ink.usr.common.model.mysql.SysControlAssignModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysControlAssignMapper {

    void addAssignInfo(SysControlAssignModel sysControlAssignModel);

    @Select("select * from sys_control_assign")
    List<SysControlAssignModel> getControlAssignList();
    

//    /**
//     * 根据条件查询分配记录（关联申请表获取申请类别和申请理由）
//     * @param queryModel 查询条件
//     * @return 符合条件的分配记录列表（包含申请信息）
//     */
//    List<SysControlAssignModel> getControlAssignListWithConditions(SysControlAssignModel queryModel);

    /**
     * 根据条件查询分配记录（关联申请表，用于展示）
     * @param queryModel 查询条件
     * @return 符合条件的分配记录列表（包含申请类别和申请理由）
     */
    List<SysControlAssignListVO> getControlAssignListWithApprovalInfo(SysControlAssignModel queryModel);

    @Select("select * from sys_control_assign where approvalId = #{approvalId}")
    SysControlAssignModel getControlAssign(Long approvalId);

    boolean updateControlAssign(SysControlAssignModel sysControlAssignModel);
}
