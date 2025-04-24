package ink.usr.admin.service.Impl;

import ink.usr.admin.dao.VO.SysApproversVO;
import ink.usr.admin.mapper.SysApplyMapper;
import ink.usr.admin.mapper.SysApprovalFlowMapper;
import ink.usr.admin.mapper.SysApproverMapper;
import ink.usr.admin.mapper.SysTokenMapper;
import ink.usr.admin.service.SysApplyService;
import ink.usr.admin.service.SysApprovalFlowService;
import ink.usr.common.model.mysql.SysApprovalFlowModel;
import ink.usr.common.model.mysql.SysApprovalRequestModel;
import ink.usr.common.model.mysql.SysApprovalTokenModel;
import ink.usr.framework.shiro.domain.ShiroUserInfo;
import ink.usr.framework.shiro.utils.ShiroUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SysApprovalFlowServiceImpl implements SysApprovalFlowService {

    @Autowired
    private SysApprovalFlowMapper sysApprovalFlowMapper;

    @Autowired
    private SysApplyMapper applyMapper;

    @Autowired
    private SysTokenMapper sysTokenMapper;

    @Autowired
    private SysApproverMapper sysApproverMapper;

    @Override
    public List<SysApprovalFlowModel> getApprovalFlowListByApproverId(Long approverId, Long approvalType) {
        List<SysApprovalFlowModel> FlowList = null;
        List newList = new ArrayList<SysApprovalFlowModel>();
        if(approvalType==0){
            FlowList = sysApprovalFlowMapper.getApprovalFlowListByApproverId(approverId);

            // 新增逻辑
            // 1.通过approverId查看role是不是ITapprover
            // 2.如果role为ITApprover，则仅仅返回一级审批流不是“待审批”的二级审批流
            String role = sysApproverMapper.getApproverRoleByApproverId(approverId);
            if(role.equals("ITApprover")){
                for(SysApprovalFlowModel single : FlowList){
                    if(single.getStage() == 2){
                        String status = sysApprovalFlowMapper.getStatusCaseInApprover2(single.getApprovalId());
                        if(!"待审批".equals(status)){
                            newList.add(single);
                        }
                    }
                }
                return newList;
            }
            // 新增逻辑
        } else {
            FlowList = sysApprovalFlowMapper.getApprovalFlowListHistoryByApproverId(approverId);
        }
        return FlowList;
    }

    @Override
    public SysApproversVO getApproversByAprrovalId(Long aprrovalId) {
        String approver1 = sysApprovalFlowMapper.getApproversByAprroval1Id(aprrovalId);
        String approver2 = sysApprovalFlowMapper.getApproversByAprroval2Id(aprrovalId);
        String status1 = sysApprovalFlowMapper.getStatusByAprroval1Id(aprrovalId);
        String status2 = sysApprovalFlowMapper.getStatusByAprroval2Id(aprrovalId);
        String username = sysApprovalFlowMapper.getUserNameByAprrovalId(aprrovalId);
        SysApproversVO sysApproversVO = new SysApproversVO();
        sysApproversVO.setApprover1(approver1);
        sysApproversVO.setApprover2(approver2);
        sysApproversVO.setUsername(username);
        sysApproversVO.setStatus1(status1);
        sysApproversVO.setStatus2(status2);

        return sysApproversVO;
    }

    /**
     * 判断token是否失效
     * @param flowId 审批流ID
     * @param token 临时token
     * @return
     */
    @Override
    public boolean validateApprovalToken(Long flowId, String token) {
        if (flowId == null || token == null || token.isEmpty()) {
            return false;
        }
        
        try {
            // 查询数据库中是否存在对应的token
            SysApprovalTokenModel tokenModel = sysApprovalFlowMapper.getApprovalToken(flowId, token);
            if (tokenModel == null) {
                return false;
            }
            
            // 检查token是否过期
            if (tokenModel.getExpireTime() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime expireTime = LocalDateTime.parse(tokenModel.getExpireTime(), formatter);
                if (LocalDateTime.now().isAfter(expireTime)) {
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public SysApprovalFlowModel getApprovalFlowById(Long flowId) {
        return sysApprovalFlowMapper.getApprovalFlowById(flowId);
    }
    
    @Override
    @Transactional
    public boolean updateApprovalStatus(Long flowId, Long requestId, String status) {
        try {
            // 设置审批时间
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String approveTime = LocalDateTime.now().format(formatter);

            // result定义
            int result = 0;
            
            // 更新审批流状态
            SysApprovalFlowModel flowModel = new SysApprovalFlowModel();
            flowModel.setFlowId(flowId);
            flowModel.setStatus(status);
            flowModel.setUpdatedAt(approveTime);
            int flowResult = sysApprovalFlowMapper.updateApprovalFlow(flowModel);

            // 更新token状态
            int tokenResult = sysTokenMapper.updateTokenByFlowId(flowId);

            // 如果审批流的stage为1，需要发送邮件给stage为2的审批人
            SysApprovalFlowModel approvalFlow = sysApprovalFlowMapper.getApprovalFlowById(flowId);
            int stage = approvalFlow.getStage();
            if(stage==1){
                // 如果为审批不通过，直接修改request
                if("审批不通过".equals(status)){
                    // 1.修改request
                    SysApprovalRequestModel requestModel = new SysApprovalRequestModel();
                    requestModel.setApprovalId(requestId);
                    requestModel.setStatus(status);
                    requestModel.setUpdatedAt(approveTime);
                    result = sysApprovalFlowMapper.updateApprovalStatus(requestModel);
                    return result > 0 && flowResult > 0 && tokenResult>0;
                }

                // 发邮件给stage为2的审批人(IT)
                // 1.获得当前审批请求的信息
                SysApprovalRequestModel approvalRequestModel = applyMapper.getByApprovalId(requestId);
                // 2.根据1中的信息构造邮件

                // 3.发送邮件
            }

            if(stage==2){
                // 更新审批请求状态
                SysApprovalRequestModel requestModel = new SysApprovalRequestModel();
                requestModel.setApprovalId(requestId);
                requestModel.setStatus(status);
                requestModel.setUpdatedAt(approveTime);
                result = sysApprovalFlowMapper.updateApprovalStatus(requestModel);


                return result > 0 && flowResult > 0 && tokenResult>0;
            }
            return flowResult > 0 && tokenResult>0;
        } catch (Exception e) {
            throw new RuntimeException("更新审批状态失败", e);
        }
    }
}
