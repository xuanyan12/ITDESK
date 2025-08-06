package ink.usr.admin.service.Impl;

import ink.usr.admin.dao.VO.SysApprovalFlowVO;
import ink.usr.admin.dao.VO.SysApproversVO;
import ink.usr.admin.mapper.*;

import ink.usr.admin.service.SysApprovalFlowService;
import ink.usr.admin.service.SysMaintenanceOrderService;
import ink.usr.common.model.mysql.SysApprovalFlowModel;
import ink.usr.common.model.mysql.SysApprovalRequestModel;
import ink.usr.common.model.mysql.SysApprovalTokenModel;
import ink.usr.common.model.mysql.SysControlAssignModel;
import ink.usr.admin.dao.DTO.SysMaintenanceOrderDTO;
import ink.usr.common.model.mysql.SysMaintenanceApplyModel;



import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ink.usr.admin.config.EmailConfig;
import ink.usr.admin.service.SysUserService;
import ink.usr.admin.service.SysApproverService;
import ink.usr.common.model.mysql.SysUserModel;
import ink.usr.common.model.mysql.SysApproverModel;
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

    @Autowired
    private SysMaintenanceOrderService sysMaintenanceOrderService;

    @Autowired
    private SysMaintenanceApplyMapper sysMaintenanceApplyMapper;

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
                        log.info("二级审批流检查 - approvalId: {}, stage1Status: {}, 当前flowStatus: {}", 
                            single.getApprovalId(), status, single.getStatus());
                        // 修复逻辑：当第一级审批通过时，第二级审批人才能看到并审批
                        if("审批通过".equals(status)){
                            sysApprovalFlowVO.setStatus1Signal(1); // 设置为1表示可以审批
                            log.info("设置status1Signal=1，使二级审批人能够审批此流程");
                        } else {
                            sysApprovalFlowVO.setStatus1Signal(0); // 设置为0表示流程未到达
                            log.info("设置status1Signal=0，流程未到达第二级审批");
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
            log.info("updateApprovalStatus调用 - flowId: {}, requestId: {}, status: {}, reason: {}", flowId, requestId, status, reason);
            
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
                    } else {
                        // 这可能是维修申请的拒绝，查找维修申请信息
                        SysMaintenanceApplyModel maintenanceApplyModel = sysMaintenanceApplyMapper.selectApplyById(requestId);
                        if (maintenanceApplyModel != null) {
                            // 获取申请人信息
                            String applicantName = sysUserService.getUserNickNameByUserId(maintenanceApplyModel.getApplicant());
                            applicantName = applicantName != null ? applicantName : "未知申请人";
                            
                            // 获取申请人邮箱
                            String applicantUserName = sysUserService.getNameByUserId(maintenanceApplyModel.getApplicant());
                            SysUserModel applicantUserInfo = sysUserService.getUserInfoByUserName(applicantUserName);
                            String applicantEmail = applicantUserInfo != null ? applicantUserInfo.getEmail() : null;
                            
                            if (applicantEmail != null && !applicantEmail.isEmpty()) {
                                // 构建维修申请拒绝邮件内容
                                String emailContent = emailConfig.buildMaintenanceRejectionEmailContent(
                                        applicantName,
                                        maintenanceApplyModel.getFixCategory(),
                                        maintenanceApplyModel.getCreateTime(),
                                        reason,
                                        approveTime
                                );
                                
                                String emailSubject = emailConfig.buildMaintenanceRejectionEmailSubject();
                                
                                // 发送邮件
                                emailConfig.sendMail(applicantEmail, emailSubject, emailContent);
                            }
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
                    // 1.判断是电脑申请还是维修申请，并相应更新状态
                    SysApprovalRequestModel approvalRequestModel = applyMapper.getByApprovalId(requestId);
                    SysMaintenanceApplyModel maintenanceApplyModel = sysMaintenanceApplyMapper.selectApplyById(requestId);
                    
                    if (approvalRequestModel != null) {
                        // 这是电脑申请，更新电脑申请状态
                        SysApprovalRequestModel requestModel = new SysApprovalRequestModel();
                        requestModel.setApprovalId(requestId);
                        requestModel.setStatus(status);
                        requestModel.setUpdatedAt(approveTime);
                        result = sysApprovalFlowMapper.updateApprovalStatus(requestModel);
                    } else if (maintenanceApplyModel != null) {
                        // 这是维修申请，更新维修申请状态
                        log.info("stage=1 维修审批不通过，更新维修申请状态，maintenanceId: {}, status: {}", requestId, status);
                        int updateResult = sysMaintenanceApplyMapper.updateApplyStatus(requestId, status);
                        result = updateResult > 0 ? 1 : 0;
                        log.info("stage=1 维修申请状态更新结果: updateResult={}, result={}", updateResult, result);
                    } else {
                        log.warn("未找到approvalId为{}的申请记录", requestId);
                        result = 0;
                    }
                    
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
                    
                    // 对于审批不通过，只要申请状态和审批流状态更新成功即可，token更新失败不影响整体结果
                    boolean stage1RejectResult = result > 0 && flowResult > 0;
                    log.info("stage=1 审批不通过最终结果 - result: {}, flowResult: {}, tokenResult: {}, 最终返回: {}", 
                        result, flowResult, tokenResult, stage1RejectResult);
                    return stage1RejectResult;
                }

                // 发邮件给stage为2的审批人(IT)
                // 1.获得当前审批请求的信息
                SysApprovalRequestModel approvalRequestModel = applyMapper.getByApprovalId(requestId);
                SysMaintenanceApplyModel maintenanceApplyModel = sysMaintenanceApplyMapper.selectApplyById(requestId);
                
                log.info("邮件发送调试 - requestId: {}, approvalRequestModel: {}, maintenanceApplyModel: {}", 
                    requestId, approvalRequestModel != null, maintenanceApplyModel != null);
                
                if (approvalRequestModel == null && maintenanceApplyModel == null) {
                    log.warn("未找到approvalId为{}的申请记录，无法发送邮件给二级审批人", requestId);
                    // 对于第一级审批，即使找不到申请记录，只要流程更新成功就应该返回true
                    return flowResult > 0;
                }
                
                // 2.找到对应的二级审批流
                SysApprovalFlowModel secondStageFlow = sysApprovalFlowMapper.getSecondStageFlowByApprovalId(requestId);
                if (secondStageFlow != null) {
                    // 获取二级审批人ID
                    Long approver2Id = secondStageFlow.getApproverId();
                    
                    // 获取二级审批人的邮箱（直接从sys_approver表获取）
                    SysApproverModel approver2Model = sysApproverService.getApproverModelByApproverId(approver2Id);
                    String approver2Email = approver2Model != null ? approver2Model.getEmail() : null;
                    
                    // 获取申请人信息
                    Long applicantId = null;
                    String applicantName = "未知申请人";
                    String applicantDepartment = "";
                    String responsibilityName = "未知责任人";
                    
                    if (approvalRequestModel != null) {
                        // 这是电脑申请
                        applicantId = approvalRequestModel.getApplicant();
                        applicantName = sysUserService.getUserNickNameByUserId(applicantId);
                        applicantName = applicantName != null ? applicantName : "未知申请人";
                        
                        String applicantUserName = sysUserService.getNameByUserId(applicantId);
                        SysUserModel applicantUserInfo = sysUserService.getUserInfoByUserName(applicantUserName);
                        applicantDepartment = applicantUserInfo != null ? applicantUserInfo.getDepartment() : "";
                        
                        responsibilityName = sysUserService.getUserNickNameByUserId(approvalRequestModel.getResponsibility());
                    } else if (maintenanceApplyModel != null) {
                        // 这是维修申请
                        applicantId = maintenanceApplyModel.getApplicant();
                        applicantName = sysUserService.getUserNickNameByUserId(applicantId);
                        applicantName = applicantName != null ? applicantName : "未知申请人";
                        
                        String applicantUserName = sysUserService.getNameByUserId(applicantId);
                        SysUserModel applicantUserInfo = sysUserService.getUserInfoByUserName(applicantUserName);
                        applicantDepartment = applicantUserInfo != null ? applicantUserInfo.getDepartment() : "";
                        
                        responsibilityName = maintenanceApplyModel.getResponsiblility();
                    }
                    
                    // 生成审批URL（包含flowId和token）
                    // 确保二级审批流有可用的token
                    String approvalUrl = generateSecondStageApprovalUrl(secondStageFlow.getFlowId());
                    
                    // 构建邮件内容和主题
                    String deviceCategory = "维修申请";
                    String deviceType = "维修";
                    String costCenter = "";
                    String company = "";
                    String deviceSituation = "";
                    String companySystem = "";
                    String reasonText = "";
                    String ciName = "";
                    String createdAt = "";
                    
                    if (approvalRequestModel != null) {
                        // 这是电脑申请
                        deviceCategory = approvalRequestModel.getDeviceCategory();
                        deviceType = approvalRequestModel.getDeviceType();
                        costCenter = approvalRequestModel.getCostCenter();
                        company = approvalRequestModel.getCompany();
                        deviceSituation = approvalRequestModel.getDeviceSituation();
                        companySystem = approvalRequestModel.getCompanySystem();
                        reasonText = approvalRequestModel.getReason();
                        ciName = approvalRequestModel.getCiName();
                        createdAt = approvalRequestModel.getCreatedAt();
                    } else if (maintenanceApplyModel != null) {
                        // 这是维修申请
                        deviceCategory = "维修申请";
                        deviceType = determineFixCategoryName(maintenanceApplyModel.getFixCategory());
                        costCenter = maintenanceApplyModel.getCostCenter();
                        company = maintenanceApplyModel.getCompany();
                        deviceSituation = maintenanceApplyModel.getProblemDescription();
                        companySystem = "";
                        reasonText = maintenanceApplyModel.getProblemDescription();
                        ciName = maintenanceApplyModel.getCiName();
                        createdAt = maintenanceApplyModel.getCreateTime();
                    }
                    
                    String emailContent;
                    String emailSubject;
                    
                    log.info("邮件模板选择 - maintenanceApplyModel != null: {}", maintenanceApplyModel != null);
                    
                    if (maintenanceApplyModel != null) {
                        // 维修申请使用专门的邮件模板
                        log.info("使用维修申请邮件模板 - fixCategory: {}, problemDescription: {}", 
                            maintenanceApplyModel.getFixCategory(), maintenanceApplyModel.getProblemDescription());
                        emailContent = emailConfig.buildMaintenanceApplyEmailContent(
                                applicantName,
                                applicantDepartment,
                                maintenanceApplyModel.getFixCategory(),
                                maintenanceApplyModel.getProblemDescription(),
                                ciName,
                                approvalUrl,
                                createdAt
                        );
                        emailSubject = emailConfig.buildMaintenanceApplyEmailSubject(applicantName, applicantDepartment);
                    } else {
                        // 电脑申请使用原有的邮件模板
                        log.info("使用电脑申请邮件模板 - deviceCategory: {}, deviceType: {}", deviceCategory, deviceType);
                        emailContent = emailConfig.buildApplyEmailContent(
                                applicantName,
                                applicantDepartment,
                                deviceCategory,
                                deviceType,
                                costCenter,
                                company,
                                responsibilityName,
                                deviceSituation,
                                companySystem,
                                reasonText,
                                ciName,
                                approvalUrl,
                                createdAt
                        );
                        emailSubject = emailConfig.buildApplyEmailSubject(applicantName, applicantDepartment, deviceCategory);
                    }
                    
                    // 发送邮件
                    if (approver2Email != null && !approver2Email.isEmpty()) {
                        emailConfig.sendMail(approver2Email, emailSubject, emailContent);
                    } else {
                        log.warn("二级审批人邮箱为空，无法发送邮件。审批人ID: {}", approver2Id);
                    }
                }
            }

            if(stage==2){
                // 判断是电脑申请还是维修申请
                SysApprovalRequestModel approvalRequestModel = applyMapper.getByApprovalId(requestId);
                SysMaintenanceApplyModel maintenanceApplyModel = sysMaintenanceApplyMapper.selectApplyById(requestId);
                
                if (approvalRequestModel != null) {
                    // 这是电脑申请，更新审批请求状态
                    SysApprovalRequestModel requestModel = new SysApprovalRequestModel();
                    requestModel.setApprovalId(requestId);
                    requestModel.setStatus(status);
                    requestModel.setUpdatedAt(approveTime);
                    result = sysApprovalFlowMapper.updateApprovalStatus(requestModel);
                } else if (maintenanceApplyModel != null) {
                    // 这是维修申请，更新维修申请状态
                    log.info("更新维修申请状态，maintenanceId: {}, status: {}", requestId, status);
                    int updateResult = sysMaintenanceApplyMapper.updateApplyStatus(requestId, status);
                    result = updateResult > 0 ? 1 : 0; // 转换为与电脑申请一致的结果格式
                    log.info("维修申请状态更新结果: updateResult={}, result={}", updateResult, result);
                } else {
                    // 既不是电脑申请也不是维修申请，记录警告
                    log.warn("未找到approvalId为{}的申请记录", requestId);
                    result = 0;
                }

                if("审批通过".equals(status)){
                    // 使用上面已经查询的结果
                    
                    if (approvalRequestModel != null) {
                        // 这是电脑申请，创建分配记录
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
                    } else if (maintenanceApplyModel != null) {
                        // 这是维修申请，创建维修订单
                        try {
                            // 创建维修订单DTO
                            SysMaintenanceOrderDTO orderDTO = new SysMaintenanceOrderDTO();
                            orderDTO.setMaintenanceId(requestId);
                            orderDTO.setOrderType(determineOrderType(maintenanceApplyModel.getFixCategory()));
                            orderDTO.setCostCenter(maintenanceApplyModel.getCostCenter());
                            orderDTO.setApplicant(String.valueOf(maintenanceApplyModel.getApplicant())); // 修正：使用申请人ID而不是用户名
                            orderDTO.setCiName(maintenanceApplyModel.getCiName());
                            orderDTO.setProblemDescription(maintenanceApplyModel.getProblemDescription());
                            
                            // 创建维修订单
                            String orderResult = sysMaintenanceOrderService.createOrder(orderDTO);
                            if (!orderResult.contains("成功")) {
                                log.error("维修订单创建失败: {}", orderResult);
                            }
                            
                            // 更新维修申请状态
                            sysMaintenanceApplyMapper.updateApplyStatus(requestId, "审批通过");
                        } catch (Exception e) {
                            log.error("创建维修订单失败", e);
                        }
                    }
                    
                    // 发送审批通过邮件给申请人
                    try {
                        Long applicantId = null;
                        if (approvalRequestModel != null) {
                            applicantId = approvalRequestModel.getApplicant();
                        } else if (maintenanceApplyModel != null) {
                            applicantId = maintenanceApplyModel.getApplicant();
                        }
                        
                        if (applicantId != null) {
                            // 获取申请人信息
                            String applicantName = sysUserService.getUserNickNameByUserId(applicantId);
                            applicantName = applicantName != null ? applicantName : "未知申请人";
                            
                            // 获取申请人邮箱
                            String applicantUserName = sysUserService.getNameByUserId(applicantId);
                            SysUserModel applicantUserInfo = sysUserService.getUserInfoByUserName(applicantUserName);
                            String applicantEmail = applicantUserInfo != null ? applicantUserInfo.getEmail() : null;
                            String applicantDepartment = applicantUserInfo != null ? applicantUserInfo.getDepartment() : "";
                            
                            if (applicantEmail != null && !applicantEmail.isEmpty()) {
                                String emailContent;
                                String emailSubject;
                                
                                if (maintenanceApplyModel != null) {
                                    // 维修申请审批通过
                                    emailContent = emailConfig.buildMaintenanceApprovalPassedEmailContent(
                                            applicantName,
                                            applicantDepartment,
                                            maintenanceApplyModel.getFixCategory(),
                                            approveTime
                                    );
                                    emailSubject = emailConfig.buildMaintenanceApprovalPassedEmailSubject();
                                } else {
                                    // 电脑申请审批通过
                                    emailContent = emailConfig.buildApprovalPassedEmailContent(
                                            applicantName,
                                            applicantDepartment,
                                            approveTime
                                    );
                                    emailSubject = emailConfig.buildApprovalPassedEmailSubject();
                                }
                                
                                // 发送邮件
                                emailConfig.sendMail(applicantEmail, emailSubject, emailContent);
                            }
                        }
                    } catch (Exception e) {
                        // 记录日志但不影响主流程
                        log.error("发送审批通过邮件失败", e);
                    }
                }

                log.info("stage 2 审批结果 - result: {}, flowResult: {}, tokenResult: {}", result, flowResult, tokenResult);
                // 对于stage 2审批，只要申请状态更新成功(result>0)和审批流状态更新成功(flowResult>0)即可
                // token更新失败不影响审批结果，因为token主要用于邮件链接，审批完成后不再需要
                boolean stage2Result = result > 0 && flowResult > 0;
                log.info("stage 2 最终返回结果: {}", stage2Result);
                return stage2Result;
            }
            log.info("非 stage 2 审批结果 - flowResult: {}, tokenResult: {}", flowResult, tokenResult);
            // 对于非 stage 2 的审批，只要 flowResult > 0 即可，tokenResult 可能为 0（没有对应的 token 记录）
            boolean finalResult = flowResult > 0;
            log.info("updateApprovalStatus最终返回: {}", finalResult);
            return finalResult;
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

    @Override
    public List<SysApprovalFlowModel> getApprovalFlowsByApprovalId(Long approvalId) {
        try {
            return sysApprovalFlowMapper.getApprovalFlowsByApprovalId(approvalId);
        } catch (Exception e) {
            log.error("根据审批ID获取审批流失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 生成二级审批流的审批URL
     * @param flowId 审批流ID
     * @return 审批URL
     */
    /**
     * 根据维修类别确定订单类型
     * @param fixCategory 维修类别
     * @return 订单类型
     */
    private String determineOrderType(String fixCategory) {
        switch (fixCategory) {
            case "qualityIssueRepair":
            case "质量问题维修":
                return "1";
            case "humanIssueRepair":
            case "人为问题维修":
                return "3";
            default:
                return "1";
        }
    }
    
    /**
     * 根据维修类别确定维修类型名称
     * @param fixCategory 维修类别
     * @return 维修类型名称
     */
    private String determineFixCategoryName(String fixCategory) {
        switch (fixCategory) {
            case "qualityIssueRepair":
            case "质量问题维修":
                return "质量问题维修";
            case "humanIssueRepair":
            case "人为问题维修":
                return "人为问题维修";
            default:
                return "维修";
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
