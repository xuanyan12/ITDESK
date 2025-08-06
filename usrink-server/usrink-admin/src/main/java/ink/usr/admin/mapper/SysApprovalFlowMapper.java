package ink.usr.admin.mapper;

import ink.usr.admin.dao.VO.SysApprovalFlowVO;
import ink.usr.common.model.mysql.SysApprovalFlowModel;
import ink.usr.common.model.mysql.SysApprovalRequestModel;
import ink.usr.common.model.mysql.SysApprovalTokenModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Insert;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysApprovalFlowMapper {

    @Select("select * from sys_approval_flow where approverId = #{approverId} and status = '审批中' order by createdAt desc")
    List<SysApprovalFlowModel> getApprovalFlowListByApproverId(Long approverId);

    @Select("select * from sys_approval_flow where approverId = #{approverId} and status != '审批中' order by updatedAt desc")
    List<SysApprovalFlowModel> getApprovalFlowListHistoryByApproverId(Long approverId);

    String getApproversByAprroval1Id(Long aprrovalId);

    String getApproversByAprroval2Id(Long aprrovalId);

    String getUserNameByAprrovalId(Long aprrovalId);
    
    /**
     * 根据flowId获取审批流信息
     */
    @Select("select * from sys_approval_flow where flowId = #{flowId}")
    SysApprovalFlowModel getApprovalFlowById(Long flowId);

    @Select("select status from sys_approval_flow where approvalId = #{approvalId} and stage = 1")
    String getStatusCaseInApprover2(Long approvalId);
    
    /**
     * 获取审批token
     */
    @Select("select * from sys_approval_token where flowId = #{flowId} and token = #{token}")
    SysApprovalTokenModel getApprovalToken(@Param("flowId") Long flowId, @Param("token") String token);
    
    /**
     * 获取流程ID对应的活跃(未使用)的token
     * @param flowId 流程ID
     * @return token信息
     */
    @Select("select * from sys_approval_token where flowId = #{flowId} and used = 0 order by createdAt desc limit 1")
    SysApprovalTokenModel getActiveTokenByFlowId(@Param("flowId") Long flowId);
    
    /**
     * 插入审批token
     * @param tokenModel token模型
     * @return 插入的记录数
     */
    @Insert("insert into sys_approval_token(flowId, token, expireTime, used) values(#{flowId}, #{token}, #{expireTime}, #{used})")
    int insertApprovalToken(SysApprovalTokenModel tokenModel);
    
    /**
     * 更新审批状态
     */
    @Update("update sys_approval_request set status = #{status}, " +
            "updatedAt = #{updatedAt} where approvalId = #{approvalId}")
    int updateApprovalStatus(SysApprovalRequestModel requestModel);
    
    /**
     * 更新审批流状态
     */
    @Update("update sys_approval_flow set status = #{status}, updatedAt = #{updatedAt}, approvalReason = #{approvalReason} where flowId = #{flowId}")
    int updateApprovalFlow(SysApprovalFlowModel flowModel);


    Map<String, Object> getStatusByAprroval1Id(Long aprrovalId);

    Map<String, Object> getStatusByAprroval2Id(Long aprrovalId);

    /**
     * 根据申请ID获取二级审批流
     * @param approvalId 申请ID
     * @return 二级审批流
     */
    @Select("select * from sys_approval_flow where approvalId = #{approvalId} and stage = 2")
    SysApprovalFlowModel getSecondStageFlowByApprovalId(Long approvalId);
    
    /**
     * 根据审批ID获取所有相关的审批流
     * @param approvalId 审批ID
     * @return 审批流列表
     */
    @Select("select * from sys_approval_flow where approvalId = #{approvalId} order by stage asc")
    List<SysApprovalFlowModel> getApprovalFlowsByApprovalId(Long approvalId);

    /**
     * 统计待处理的审批数量
     * @param approverId 审批人ID
     * @return 待处理审批数量
     */
    @Select("select count(*) from sys_approval_flow where approverId = #{approverId} and status = '审批中'")
    int countPendingApprovalsByApproverId(Long approverId);
    
    /**
     * 统计已处理的审批数量
     * @param approverId 审批人ID
     * @return 已处理审批数量
     */
    @Select("select count(*) from sys_approval_flow where approverId = #{approverId} and status != '审批中'")
    int countProcessedApprovalsByApproverId(Long approverId);
}
