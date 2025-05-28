package ink.usr.admin.service.Impl;

import ink.usr.admin.dao.VO.SysApprovalFlowVO;
import ink.usr.admin.dao.VO.SysApproversVO;
import ink.usr.admin.mapper.*;
import ink.usr.admin.service.SysApplyService;
import ink.usr.admin.service.SysApprovalFlowService;
import ink.usr.common.model.mysql.SysApprovalFlowModel;
import ink.usr.common.model.mysql.SysApprovalRequestModel;
import ink.usr.common.model.mysql.SysApprovalTokenModel;
import ink.usr.common.model.mysql.SysControlAssignModel;
import ink.usr.framework.shiro.domain.ShiroUserInfo;
import ink.usr.framework.shiro.utils.ShiroUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ink.usr.admin.config.EmailConfig;
import ink.usr.admin.service.SysUserService;
import ink.usr.admin.service.SysApproverService;
import ink.usr.common.model.mysql.SysUserModel;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
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

    @Autowired
    private SysControlAssignMapper sysControlAssignMapper;
    
    @Autowired
    private EmailConfig emailConfig;
    
    @Autowired
    private SysUserService sysUserService;
    
    @Autowired
    private SysApproverService sysApproverService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public List<SysApprovalFlowVO> getApprovalFlowListByApproverId(Long approverId, Long approvalType) {
        List<SysApprovalFlowModel> FlowList = null;
        List newList = new ArrayList<SysApprovalFlowVO>();
        if(approvalType==0){
            // approverId -> list
            FlowList = sysApprovalFlowMapper.getApprovalFlowListByApproverId(approverId);

            // 新增逻辑
            // 1.通过approverId查看role是不是ITapprover
            // 2.如果role为ITApprover，则一级审批流是"审批中"的二级审批流需要添加标识0，其他情况不管（默认标识是1）
            // approverId ->
            String role = sysApproverMapper.getApproverRoleByApproverId(approverId);

            if("ITApprover".equals(role)){
                for(SysApprovalFlowModel single : FlowList){
                    SysApprovalFlowVO sysApprovalFlowVO = new SysApprovalFlowVO();
                    BeanUtils.copyProperties(single, sysApprovalFlowVO);
                    if(single.getStage() == 2){
                        String status = sysApprovalFlowMapper.getStatusCaseInApprover2(single.getApprovalId());
                        if("审批中".equals(status)){
                            sysApprovalFlowVO.setStatus1Signal(0);
                        }
                    }
                    newList.add(sysApprovalFlowVO);
                }
            } else {
                for(SysApprovalFlowModel single : FlowList){
                    SysApprovalFlowVO sysApprovalFlowVO = new SysApprovalFlowVO();
                    BeanUtils.copyProperties(single, sysApprovalFlowVO);
                    newList.add(sysApprovalFlowVO);
                }
            }
            return newList;
            // 新增逻辑
        } else {
            // approverId -> list
            FlowList = sysApprovalFlowMapper.getApprovalFlowListHistoryByApproverId(approverId);
            for(SysApprovalFlowModel single : FlowList){
                SysApprovalFlowVO sysApprovalFlowVO = new SysApprovalFlowVO();
                BeanUtils.copyProperties(single, sysApprovalFlowVO);
                newList.add(sysApprovalFlowVO);
            }
        }
        return newList;
    }

    @Override
    public SysApproversVO getApproversByAprrovalId(Long aprrovalId) {
        String approver1 = sysApprovalFlowMapper.getApproversByAprroval1Id(aprrovalId);
        String approver2 = sysApprovalFlowMapper.getApproversByAprroval2Id(aprrovalId);
        Map<String, Object> flow1 = sysApprovalFlowMapper.getStatusByAprroval1Id(aprrovalId);
        Map<String, Object> flow2 = sysApprovalFlowMapper.getStatusByAprroval2Id(aprrovalId);
        String username = sysApprovalFlowMapper.getUserNameByAprrovalId(aprrovalId);
        SysApproversVO sysApproversVO = new SysApproversVO();
        sysApproversVO.setApprover1(approver1);
        sysApproversVO.setApprover2(approver2);
        sysApproversVO.setUsername(username);
        
        // Extract status and additional fields from flow1
        if (flow1 != null) {
            sysApproversVO.setStatus1((String) flow1.get("status"));
            sysApproversVO.setApprovalReason1((String) flow1.get("approvalReason"));
            sysApproversVO.setUpdatedAt1((String) flow1.get("updatedAt"));
        }
        
        // Extract status and additional fields from flow2
        if (flow2 != null) {
            sysApproversVO.setStatus2((String) flow2.get("status"));
            sysApproversVO.setApprovalReason2((String) flow2.get("approvalReason"));
            sysApproversVO.setUpdatedAt2((String) flow2.get("updatedAt"));
        }

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
    public boolean updateApprovalStatus(Long flowId, Long requestId, String status, String reason) {
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
            flowModel.setApprovalReason(reason);
            int flowResult = sysApprovalFlowMapper.updateApprovalFlow(flowModel);

            // 更新token状态
            int tokenResult = sysTokenMapper.updateTokenByFlowId(flowId);

            // 如果审批不通过，发送拒绝邮件给申请人
            if ("审批不通过".equals(status)) {
                try {
                    // 获取申请详情
                    SysApprovalRequestModel approvalRequestModel = applyMapper.getByApprovalId(requestId);
                    if (approvalRequestModel != null) {
                        // 获取申请人信息
                        String applicantName = sysUserService.getUserNickNameByUserId(approvalRequestModel.getApplicant());
                        applicantName = applicantName != null ? applicantName : "未知申请人";
                        
                        // 获取申请人邮箱
                        String applicantUserName = sysUserService.getNameByUserId(approvalRequestModel.getApplicant());
                        SysUserModel applicantUserInfo = sysUserService.getUserInfoByUserName(applicantUserName);
                        String applicantEmail = applicantUserInfo != null ? applicantUserInfo.getEmail() : null;
                        
                        if (applicantEmail != null && !applicantEmail.isEmpty()) {
                            // 构建邮件内容
                            String emailContent = emailConfig.buildRejectionEmailContent(
                                    applicantName,
                                    approvalRequestModel.getDeviceCategory(),
                                    approvalRequestModel.getCreatedAt(),
                                    reason,
                                    approveTime
                            );
                            
                            String emailSubject = emailConfig.buildRejectionEmailSubject();
                            
                            // 发送邮件
                            emailConfig.sendMail(applicantEmail, emailSubject, emailContent);
                        }
                    }
                } catch (Exception e) {
                    // 记录日志但不影响主流程
                    log.error("发送拒绝邮件失败", e);
                }
            }

            // 如果审批流的stage为1，需要发送邮件给stage为2的审批人
            SysApprovalFlowModel approvalFlow = sysApprovalFlowMapper.getApprovalFlowById(flowId);
            int stage = approvalFlow.getStage();
            if(stage==1){
                // 如果为审批不通过，直接修改request和二级审批流
                if("审批不通过".equals(status)){
                    // 1.修改request
                    SysApprovalRequestModel requestModel = new SysApprovalRequestModel();
                    requestModel.setApprovalId(requestId);
                    requestModel.setStatus(status);
                    requestModel.setUpdatedAt(approveTime);
                    result = sysApprovalFlowMapper.updateApprovalStatus(requestModel);
                    
                    // 2.找到对应的二级审批流并将其状态也设置为审批不通过
                    SysApprovalFlowModel secondStageFlow = sysApprovalFlowMapper.getSecondStageFlowByApprovalId(requestId);
                    if (secondStageFlow != null) {
                        secondStageFlow.setStatus("审批不通过");
                        secondStageFlow.setUpdatedAt(approveTime);
                        secondStageFlow.setApprovalReason(reason);
                        sysApprovalFlowMapper.updateApprovalFlow(secondStageFlow);
                        
                        // 3.更新二级审批流的token状态
                        sysTokenMapper.updateTokenBySecondStageFlow(requestId);
                    }
                    
                    return result > 0 && flowResult > 0 && tokenResult>0;
                }

                // 发邮件给stage为2的审批人(IT)
                // 1.获得当前审批请求的信息
                SysApprovalRequestModel approvalRequestModel = applyMapper.getByApprovalId(requestId);
                
                // 2.找到对应的二级审批流
                SysApprovalFlowModel secondStageFlow = sysApprovalFlowMapper.getSecondStageFlowByApprovalId(requestId);
                if (secondStageFlow != null) {
                    // 获取二级审批人ID
                    Long approver2Id = secondStageFlow.getApproverId();
                    
                    // 获取二级审批人的用户ID
                    Long userApprover2Id = sysApproverService.getApproverInfoByApproverId(approver2Id);
                    
                    // 获取二级审批人的邮箱
                    String approver2Email = sysUserService.getUserInfoByUserName(sysUserService.getNameByUserId(userApprover2Id)).getEmail();
                    
                    // 获取申请人姓名
                    String applicantName = sysUserService.getUserNickNameByUserId(approvalRequestModel.getApplicant());
                    applicantName = applicantName != null ? applicantName : "未知申请人";
                    
                    // 获取申请人部门信息
                    String applicantUserName = sysUserService.getNameByUserId(approvalRequestModel.getApplicant());
                    SysUserModel applicantUserInfo = sysUserService.getUserInfoByUserName(applicantUserName);
                    String applicantDepartment = applicantUserInfo != null ? applicantUserInfo.getDepartment() : "";
                    
                    // 获取责任人姓名
                    String responsibilityName = sysUserService.getUserNickNameByUserId(approvalRequestModel.getResponsibility());
                    
                    // 生成审批URL（包含flowId和token）
                    // 确保二级审批流有可用的token
                    String approvalUrl = generateSecondStageApprovalUrl(secondStageFlow.getFlowId());
                    
                    // 构建邮件内容和主题
                    String emailContent = emailConfig.buildApplyEmailContent(
                            applicantName,
                            applicantDepartment,
                            approvalRequestModel.getDeviceCategory(),
                            approvalRequestModel.getDeviceType(),
                            approvalRequestModel.getCostCenter(),
                            approvalRequestModel.getCompany(),
                            responsibilityName,
                            approvalRequestModel.getDeviceSituation(),
                            approvalRequestModel.getCompanySystem(),
                            approvalRequestModel.getReason(),
                            approvalRequestModel.getCiName(),
                            approvalUrl,
                            approvalRequestModel.getCreatedAt()
                    );
                    
                    String emailSubject = emailConfig.buildApplyEmailSubject(applicantName, applicantDepartment, approvalRequestModel.getDeviceCategory());
                    
                    // 发送邮件
                    emailConfig.sendMail(approver2Email, emailSubject, emailContent);
                }
            }

            if(stage==2){
                // 更新审批请求状态
                SysApprovalRequestModel requestModel = new SysApprovalRequestModel();
                requestModel.setApprovalId(requestId);
                requestModel.setStatus(status);
                requestModel.setUpdatedAt(approveTime);
                result = sysApprovalFlowMapper.updateApprovalStatus(requestModel);

                if("审批通过".equals(status)){
                    // 获取订单数据
                    SysApprovalRequestModel approvalRequestModel = applyMapper.getByApprovalId(requestId);
                    // 构造分配数据
                    SysControlAssignModel sysControlAssignModel = new SysControlAssignModel();
                    sysControlAssignModel.setDeviceType(approvalRequestModel.getDeviceType());
                    sysControlAssignModel.setDeviceSituation(approvalRequestModel.getDeviceSituation());
                    sysControlAssignModel.setApprovalId(requestId);
                    sysControlAssignModel.setCompany(approvalRequestModel.getCompany());
                    sysControlAssignModel.setApplicant(approvalRequestModel.getApplicant());
                    sysControlAssignModel.setAssignStatus("分配中");
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    sysControlAssignModel.setStartTime(df.format(LocalDateTime.now()));
                    sysControlAssignMapper.addAssignInfo(sysControlAssignModel);
                    
                    // 发送审批通过邮件给申请人
                    try {
                        // 获取申请人信息
                        String applicantName = sysUserService.getUserNickNameByUserId(approvalRequestModel.getApplicant());
                        applicantName = applicantName != null ? applicantName : "未知申请人";
                        
                        // 获取申请人邮箱
                        String applicantUserName = sysUserService.getNameByUserId(approvalRequestModel.getApplicant());
                        SysUserModel applicantUserInfo = sysUserService.getUserInfoByUserName(applicantUserName);
                        String applicantEmail = applicantUserInfo != null ? applicantUserInfo.getEmail() : null;
                        String applicantDepartment = applicantUserInfo != null ? applicantUserInfo.getDepartment() : "";
                        
                        if (applicantEmail != null && !applicantEmail.isEmpty()) {
                            // 构建邮件内容
                            String emailContent = emailConfig.buildApprovalPassedEmailContent(
                                    applicantName,
                                    applicantDepartment,
                                    approveTime
                            );
                            
                            String emailSubject = emailConfig.buildApprovalPassedEmailSubject();
                            
                            // 发送邮件
                            emailConfig.sendMail(applicantEmail, emailSubject, emailContent);
                        }
                    } catch (Exception e) {
                        // 记录日志但不影响主流程
                        log.error("发送审批通过邮件失败", e);
                    }
                }

                return result > 0 && flowResult > 0 && tokenResult>0;
            }
            return flowResult > 0 && tokenResult>0;
        } catch (Exception e) {
            throw new RuntimeException("更新审批状态失败", e);
        }
    }

    @Override
    public int getApprovalFlowCountByApproverId(Long approverId, Long approvalType) {
        try {
            if (approvalType == 0) {
                // 待处理审批数量
                return sysApprovalFlowMapper.countPendingApprovalsByApproverId(approverId);
            } else {
                // 已处理审批数量
                return sysApprovalFlowMapper.countProcessedApprovalsByApproverId(approverId);
            }
        } catch (Exception e) {
            // 发生异常时返回0
            return 0;
        }
    }

    /**
     * 生成二级审批流的审批URL
     * @param flowId 审批流ID
     * @return 审批URL
     */
    private String generateSecondStageApprovalUrl(Long flowId) {
        // 从数据库查询现有有效token
        String token;
        try {
            // 检查是否已有未使用的token
            SysApprovalTokenModel tokenModel = sysApprovalFlowMapper.getActiveTokenByFlowId(flowId);
            
            if (tokenModel != null && tokenModel.getUsed() == 0) {
                token = tokenModel.getToken();
            } else {
                // 生成新token并存储
                token = UUID.randomUUID().toString().replace("-", "");
                
                // 设置过期时间为7天后
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String expireTime = LocalDateTime.now().plusDays(7).format(formatter);
                
                // 创建token模型
                SysApprovalTokenModel newToken = new SysApprovalTokenModel();
                newToken.setFlowId(flowId);
                newToken.setToken(token);
                newToken.setExpireTime(expireTime);
                newToken.setUsed(0);
                
                // 保存到数据库
                sysApprovalFlowMapper.insertApprovalToken(newToken);
            }
        } catch (Exception e) {
            // 出现异常时生成一个临时token，但不保存到数据库
            token = UUID.randomUUID().toString().replace("-", "");
        }
        
        // 返回审批URL，包含flowId和token
        return frontendUrl + "/public-page?flowId=" + flowId + "&token=" + token;
    }
}
