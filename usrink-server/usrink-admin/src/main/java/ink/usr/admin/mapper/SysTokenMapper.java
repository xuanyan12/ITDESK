package ink.usr.admin.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;


@Mapper
public interface SysTokenMapper {

    @Update("update sys_approval_token set used = 1 where flowId = #{flowId}")
    int updateTokenByFlowId(Long flowId);
    
    /**
     * 更新指定申请ID的二级审批流token的used状态
     * @param approvalId 申请ID
     * @return 更新的记录数
     */
    @Update("update sys_approval_token set used = 1 where flowId = (select flowId from sys_approval_flow where approvalId = #{approvalId} and stage = 2)")
    int updateTokenBySecondStageFlow(@Param("approvalId") Long approvalId);
}
