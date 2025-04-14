package ink.usr.admin.service.Impl;

import ink.usr.admin.dao.VO.SysApproversVO;
import ink.usr.admin.mapper.SysApplyMapper;
import ink.usr.admin.mapper.SysApprovalFlowMapper;
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
import java.util.List;
import java.util.UUID;

@Service
public class SysApprovalFlowServiceImpl implements SysApprovalFlowService {

    @Autowired
    private SysApprovalFlowMapper sysApprovalFlowMapper;

    @Override
    public List<SysApprovalFlowModel> getApprovalFlowListByApproverId(Long approverId) {
        List<SysApprovalFlowModel> FlowList = sysApprovalFlowMapper.getApprovalFlowListByApproverId(approverId);
        return FlowList;
    }

    @Override
    public SysApproversVO getApproversByAprrovalId(Long aprrovalId) {
        String approver1 = sysApprovalFlowMapper.getApproversByAprroval1Id(aprrovalId);
        String approver2 = sysApprovalFlowMapper.getApproversByAprroval2Id(aprrovalId);
        String username = sysApprovalFlowMapper.getUserNameByAprrovalId(aprrovalId);
        SysApproversVO sysApproversVO = new SysApproversVO();
        sysApproversVO.setApprover1(approver1);
        sysApproversVO.setApprover2(approver2);
        sysApproversVO.setUsername(username);
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
    public boolean updateApprovalStatus(Long flowId, Long requestId, String status, String comment) {
        try {
            // 更新审批状态
            SysApprovalRequestModel requestModel = new SysApprovalRequestModel();
            requestModel.setApprovalId(requestId);
            requestModel.setStatus(status);
            
            // 设置审批时间
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String approveTime = LocalDateTime.now().format(formatter);
            requestModel.setCreatedAt(approveTime);
            requestModel.setUpdatedAt(approveTime);
            
            // 更新审批请求状态
            int result = sysApprovalFlowMapper.updateApprovalStatus(requestModel);
            
            // 更新审批流状态
            SysApprovalFlowModel flowModel = new SysApprovalFlowModel();
            flowModel.setFlowId(flowId);
            flowModel.setStatus(status);
            flowModel.setUpdatedAt(approveTime);
            int flowResult = sysApprovalFlowMapper.updateApprovalFlow(flowModel);
            
            return result > 0 && flowResult > 0;
        } catch (Exception e) {
            throw new RuntimeException("更新审批状态失败", e);
        }
    }
}
