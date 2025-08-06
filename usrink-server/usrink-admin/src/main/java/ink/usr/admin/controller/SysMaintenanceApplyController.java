package ink.usr.admin.controller;

import com.github.pagehelper.Page;
import ink.usr.admin.dao.DTO.SysMaintenanceApplyRequestDTO;
import ink.usr.admin.dao.DTO.SysApprovalDTO;
import ink.usr.admin.dao.VO.SysApplyListVO;
import ink.usr.admin.dao.VO.SysMaintenanceApplyVO;
import ink.usr.admin.mapper.SysApproverMapper;
import ink.usr.admin.service.Impl.SysMaintenanceApplyServiceImpl;
import ink.usr.admin.service.SysApproverService;
import ink.usr.admin.service.SysUserService;
import ink.usr.admin.service.SysApprovalFlowService;
import ink.usr.common.core.domain.Dict;
import ink.usr.common.core.domain.Res;
import ink.usr.common.core.utils.PageUtil;
import ink.usr.common.model.mysql.SysApprovalFlowModel;
import ink.usr.common.model.mysql.SysMaintenanceApplyModel;
import ink.usr.common.model.mysql.SysApproverModel;
import ink.usr.common.model.mysql.SysUserModel;
import ink.usr.framework.shiro.domain.ShiroUserInfo;
import ink.usr.framework.shiro.utils.ShiroUtil;
import ink.usr.admin.config.EmailConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/sysMaintenance")
public class SysMaintenanceApplyController {
    
    @Autowired
    private SysMaintenanceApplyServiceImpl sysMaintenanceApplyService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysApprovalFlowService sysApprovalFlowService;

    @Autowired
    private SysApproverService sysApproverService;

    @Autowired
    private SysApproverMapper sysApproverMapper;
    
    @Autowired
    private EmailConfig emailConfig;

    /**
     * 根据用户ID获取对应的维修申请列表
     * @return 维修申请列表
     */
    @RequestMapping("/getMaintenanceList")
    public Res getMaintenanceList() {
        try {
            Page<Object> pages = PageUtil.startPage();
            ShiroUserInfo shiroUserInfo = ShiroUtil.getShiroUserInfo();

            // 检查用户信息是否可用
            if (shiroUserInfo == null) {
                return Res.error("用户未登录或会话已过期");
            }

            Long userId = shiroUserInfo.getUserId();

            // 检查用户ID是否有效
            if (userId == null) {
                return Res.error("无法获取用户ID");
            }
            
            List<SysApplyListVO> applyList = sysMaintenanceApplyService.getApplyList(userId);

            Dict result = Dict.create()
                    .set("list", applyList)
                    .set("total", pages.getTotal());
            return Res.success(result);
        } catch (Exception e) {
            log.error("获取维修申请列表失败", e);
            return Res.error("获取维修申请列表失败: " + e.getMessage());
        }
    }

    /**
     * 提交维修申请
     * @param sysMaintenanceApplyRequestDTO 维修申请请求DTO
     * @return 提交结果
     */
    @RequestMapping("/submitMaintenance")
    public Res submitMaintenance(@RequestBody SysMaintenanceApplyRequestDTO sysMaintenanceApplyRequestDTO) {
        try {
            // 提交维修申请
            String result = sysMaintenanceApplyService.addApply(sysMaintenanceApplyRequestDTO);
            
            // 检查是否为自动审批通过的情况
            if (result.contains("自动审批通过")) {
                // 发送审批通过邮件给申请人
                try {
                    // 获取申请人姓名
                    String applicantName = sysUserService.getUserNickNameByUserId(sysUserService.getUserIdByUserName(sysMaintenanceApplyRequestDTO.getUserName()));
                    applicantName = applicantName != null ? applicantName : "未知申请人";
                    
                    // 获取申请人邮箱和部门
                    String applicantUserName = sysMaintenanceApplyRequestDTO.getUserName();
                    SysUserModel applicantUserInfo = sysUserService.getUserInfoByUserName(applicantUserName);
                    String applicantEmail = applicantUserInfo != null ? applicantUserInfo.getEmail() : null;
                    String applicantDepartment = applicantUserInfo != null ? applicantUserInfo.getDepartment() : "";
                    
                    if (applicantEmail != null && !applicantEmail.isEmpty()) {
                        // 获取当前时间
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        String approvalTime = LocalDateTime.now().format(formatter);
                        
                        // 构建维修申请审批通过邮件内容
                        String emailContent = emailConfig.buildMaintenanceApprovalPassedEmailContent(
                                applicantName,
                                applicantDepartment,
                                sysMaintenanceApplyRequestDTO.getFixCategory(),
                                approvalTime
                        );
                        
                        String emailSubject = emailConfig.buildMaintenanceApprovalPassedEmailSubject();
                        
                        // 发送邮件
                        emailConfig.sendMail(applicantEmail, emailSubject, emailContent);
                    }
                } catch (Exception e) {
                    // 记录日志但不影响主流程
                    log.error("发送自动审批通过邮件失败", e);
                }
                
                return Res.success(result);
            }
            
            // 需要审批的情况：发送邮件给一级审批人，同时发送申请成功邮件给申请人
            if (result.contains("成功") && result.contains("审批")) {
                try {
                    // 获取申请人姓名
                    String applicantName = sysUserService.getUserNickNameByUserId(sysUserService.getUserIdByUserName(sysMaintenanceApplyRequestDTO.getUserName()));
                    applicantName = applicantName != null ? applicantName : "未知申请人";


                    
                    // 获取申请人部门信息和邮箱
                    SysUserModel applicantUserInfo = sysUserService.getUserInfoByUserName(sysMaintenanceApplyRequestDTO.getUserName());
                    String applicantDepartment = applicantUserInfo != null ? applicantUserInfo.getDepartment() : "";
                    String applicantEmail = applicantUserInfo != null ? applicantUserInfo.getEmail() : null;
                    
                    // 获取申请时间（当前时间）
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    String applyTime = LocalDateTime.now().format(formatter);
                    
                    // 1. 先发送申请成功邮件给申请人
                    if (applicantEmail != null && !applicantEmail.isEmpty()) {
                        String submitSuccessEmailContent = emailConfig.buildMaintenanceSubmitSuccessEmailContent(
                                applicantName,
                                applicantDepartment,
                                sysMaintenanceApplyRequestDTO.getFixCategory(),
                                applyTime
                        );
                        String submitSuccessEmailSubject = emailConfig.buildMaintenanceSubmitSuccessEmailSubject(
                                applicantName,
                                sysMaintenanceApplyRequestDTO.getFixCategory()
                        );
                        emailConfig.sendMail(applicantEmail, submitSuccessEmailSubject, submitSuccessEmailContent);
                    }
                    
                    // 2. 发邮件给一级审批人
                    // 获取一级审批人id
                    Long approverId = sysApproverService.getApproverIdUseCostCenter(sysUserService.getUserIdByUserName(sysMaintenanceApplyRequestDTO.getUserName()), sysMaintenanceApplyRequestDTO.getCostCenter());
                    // 根据审批人id获取邮箱（直接从sys_approver表获取）
                    SysApproverModel approverModel = sysApproverService.getApproverModelByApproverId(approverId);
                    String approverEmail = approverModel != null ? approverModel.getEmail() : null;
                    
                    // 从服务返回值中提取审批URL
                    String approvalUrl = "http://localhost:3000/approval"; // 默认值
                    if (result.contains("APPROVAL_URL:")) {
                        String[] parts = result.split("\\|APPROVAL_URL:");
                        if (parts.length > 1) {
                            approvalUrl = parts[1];
                        }
                    }
                    
                    // 使用维修申请专用的邮件模板
                    String emailContent = emailConfig.buildMaintenanceApplyEmailContent(
                            applicantName,
                            applicantDepartment,
                            sysMaintenanceApplyRequestDTO.getFixCategory(),
                            sysMaintenanceApplyRequestDTO.getProblemDescription(),
                            sysMaintenanceApplyRequestDTO.getComputerName(),
                            approvalUrl,
                            applyTime
                    );
                    
                    String emailSubject = emailConfig.buildMaintenanceApplyEmailSubject(applicantName, applicantDepartment);
                    
                    // 发送邮件给审批人
                    if (approverEmail != null && !approverEmail.isEmpty()) {
                        emailConfig.sendMail(approverEmail, emailSubject, emailContent);
                    } else {
                        log.warn("审批人邮箱为空，无法发送邮件。审批人ID: {}", approverId);
                    }
                } catch (Exception e) {
                    // 记录日志但不影响主流程
                    log.error("发送维修申请邮件失败", e);
                }
            }
            
            if (result.contains("成功")) {
                return Res.success(result);
            } else {
                return Res.error(result);
            }
        } catch (Exception e) {
            log.error("维修申请提交失败", e);
            return Res.error("维修申请提交失败: " + e.getMessage());
        }
    }

    /**
     * 根据用户名获取用户信息
     * @param userName 用户名
     * @return 用户信息
     */
    @RequestMapping("/getUserInfoByUserName")
    public Res getUserInfoByUserName(@RequestParam("userName") String userName) {
        try {
            if (userName == null || userName.trim().isEmpty()) {
                return Res.error("用户名不能为空");
            }

            // 获取用户信息
            SysUserModel userInfo = sysUserService.getUserInfoByUserName(userName);
            if (userInfo != null) {
                return Res.success(userInfo);
            } else {
                return Res.error("用户不存在");
            }
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Res.error("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 通过用户名或姓名模糊查询用户信息
     * @param query 查询条件（可以是姓名或NT账号的部分）
     * @return 用户列表
     */
    @RequestMapping("/searchUsers")
    public Res searchUsers(@RequestParam("query") String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return Res.error("查询条件不能为空");
            }

            List<SysUserModel> userList = sysUserService.searchUsersByNameOrNick(query);

            Dict result = Dict.create().set("list", userList);
            return Res.success(result);
        } catch (Exception e) {
            log.error("查询用户信息失败", e);
            return Res.error("查询用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有唯一的部门和成本中心，用于下拉菜单
     * @return 部门和成本中心列表
     */
    @RequestMapping("/getUserDepartmentsAndCostCenters")
    public Res getUserDepartmentsAndCostCenters() {
        try {
            List<String> departments = sysUserService.getAllDistinctDepartments();
            List<String> costCenters = sysUserService.getAllDistinctCostCenters();

            Dict result = Dict.create()
                    .set("departments", departments)
                    .set("costCenters", costCenters);

            return Res.success(result);
        } catch (Exception e) {
            log.error("获取部门和成本中心列表失败", e);
            return Res.error("获取部门和成本中心列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询维修申请
     * @param maintenanceId 维修申请ID
     * @return 维修申请信息
     */
    @RequestMapping("/getApplyById")
    public Res getApplyById(@RequestParam("maintenanceId") Long maintenanceId) {
        try {
            SysMaintenanceApplyVO applyVO = sysMaintenanceApplyService.getApplyById(maintenanceId);
            if (applyVO != null) {
                return Res.success(applyVO);
            } else {
                return Res.error("维修申请不存在");
            }
        } catch (Exception e) {
            log.error("查询维修申请失败", e);
            return Res.error("查询维修申请失败: " + e.getMessage());
        }
    }

    /**
     * 根据申请人查询维修申请
     * @return 维修申请列表
     */
    @RequestMapping("/getAppliesByApplicant")
    public Res getAppliesByApplicant() {
        try {
            ShiroUserInfo shiroUserInfo = ShiroUtil.getShiroUserInfo();
            if (shiroUserInfo == null) {
                return Res.error("用户未登录或会话已过期");
            }
            
            Long applicant = shiroUserInfo.getUserId();
            Page<Object> pages = PageUtil.startPage();
            List<SysMaintenanceApplyVO> applyList = sysMaintenanceApplyService.getAppliesByApplicant(applicant);
            
            Dict result = Dict.create()
                    .set("list", applyList)
                    .set("total", pages.getTotal());
            return Res.success(result);
        } catch (Exception e) {
            log.error("根据申请人查询维修申请失败", e);
            return Res.error("查询维修申请失败: " + e.getMessage());
        }
    }

    /**
     * 查询所有维修申请
     * @return 维修申请列表
     */
    @RequestMapping("/getAllApplies")
    public Res getAllApplies() {
        try {
            Page<Object> pages = PageUtil.startPage();
            List<SysMaintenanceApplyVO> applyList = sysMaintenanceApplyService.getAllApplies();
            
            Dict result = Dict.create()
                    .set("list", applyList)
                    .set("total", pages.getTotal());
            return Res.success(result);
        } catch (Exception e) {
            log.error("查询所有维修申请失败", e);
            return Res.error("查询维修申请失败: " + e.getMessage());
        }
    }

    /**
     * 根据状态查询维修申请
     * @param applyStatus 申请状态
     * @return 维修申请列表
     */
    @RequestMapping("/getAppliesByStatus")
    public Res getAppliesByStatus(@RequestParam("applyStatus") String applyStatus) {
        try {
            Page<Object> pages = PageUtil.startPage();
            List<SysMaintenanceApplyVO> applyList = sysMaintenanceApplyService.getAppliesByStatus(applyStatus);
            
            Dict result = Dict.create()
                    .set("list", applyList)
                    .set("total", pages.getTotal());
            return Res.success(result);
        } catch (Exception e) {
            log.error("根据状态查询维修申请失败", e);
            return Res.error("查询维修申请失败: " + e.getMessage());
        }
    }

    /**
     * 更新维修申请
     * @param applyModel 维修申请模型
     * @return 更新结果
     */
    @RequestMapping("/updateApply")
    public Res updateApply(@RequestBody SysMaintenanceApplyModel applyModel) {
        try {
            boolean result = sysMaintenanceApplyService.updateApply(applyModel);
            if (result) {
                return Res.success("维修申请更新成功");
            } else {
                return Res.error("维修申请更新失败");
            }
        } catch (Exception e) {
            log.error("更新维修申请失败", e);
            return Res.error("更新维修申请失败: " + e.getMessage());
        }
    }

    /**
     * 更新申请状态
     * @param maintenanceId 维修申请ID
     * @param applyStatus 申请状态
     * @return 更新结果
     */
    @RequestMapping("/updateApplyStatus")
    public Res updateApplyStatus(@RequestParam("maintenanceId") Long maintenanceId,
                                @RequestParam("applyStatus") String applyStatus) {
        try {
            boolean result = sysMaintenanceApplyService.updateApplyStatus(maintenanceId, applyStatus);
            if (result) {
                return Res.success("申请状态更新成功");
            } else {
                return Res.error("申请状态更新失败");
            }
        } catch (Exception e) {
            log.error("更新申请状态失败", e);
            return Res.error("更新申请状态失败: " + e.getMessage());
        }
    }

    /**
     * 删除维修申请
     * @param maintenanceId 维修申请ID
     * @return 删除结果
     */
    @RequestMapping("/deleteApply")
    public Res deleteApply(@RequestParam("maintenanceId") Long maintenanceId) {
        try {
            boolean result = sysMaintenanceApplyService.deleteApply(maintenanceId);
            if (result) {
                return Res.success("维修申请删除成功");
            } else {
                return Res.error("维修申请删除失败");
            }
        } catch (Exception e) {
            log.error("删除维修申请失败", e);
            return Res.error("删除维修申请失败: " + e.getMessage());
        }
    }

    /**
     * 获取维修进度
     * @param maintenanceId 维修申请ID
     * @return 维修进度信息
     */
    @RequestMapping("/getMaintenanceProgress")
    public Res getMaintenanceProgress(@RequestParam("maintenanceId") Long maintenanceId) {
        try {
            // 获取维修申请信息
            SysMaintenanceApplyVO applyVO = sysMaintenanceApplyService.getApplyById(maintenanceId);
            if (applyVO == null) {
                return Res.error("维修申请不存在");
            }
            
            // 获取审批流程信息
            List<SysApprovalFlowModel> approvalFlows = sysApprovalFlowService.getApprovalFlowsByApprovalId(maintenanceId);
            
            // 构建审批进度信息
            Dict result = Dict.create()
                    .set("maintenanceId", maintenanceId)
                    .set("username", applyVO.getApplicantName());
            
            // 处理审批流程
            if (approvalFlows != null && !approvalFlows.isEmpty()) {
                for (SysApprovalFlowModel flow : approvalFlows) {
                    if (flow.getStage() == 1) {
                        // 一级审批（成本负责人）
                        String approverName = getApproverNameByApproverId(flow.getApproverId());
                        result.set("approver1", approverName != null ? approverName : "成本负责人")
                              .set("status1", flow.getStatus())
                              .set("updatedAt1", flow.getUpdatedAt())
                              .set("approvalReason1", flow.getApprovalReason());
                    } else if (flow.getStage() == 2) {
                        // 二级审批（IT负责人）
                        String approverName = getApproverNameByApproverId(flow.getApproverId());
                        result.set("approver2", approverName != null ? approverName : "IT负责人")
                              .set("status2", flow.getStatus())
                              .set("updatedAt2", flow.getUpdatedAt())
                              .set("approvalReason2", flow.getApprovalReason());
                    }
                }
            } else {
                // 如果没有审批流程，说明是质量问题维修，自动审批通过
                result.set("approver1", "系统自动审批")
                      .set("status1", "审批通过")
                      .set("updatedAt1", applyVO.getCreateTime())
                      .set("approvalReason1", "质量问题维修自动通过")
                      .set("approver2", "系统自动审批")
                      .set("status2", "审批通过")
                      .set("updatedAt2", applyVO.getCreateTime())
                      .set("approvalReason2", "质量问题维修自动通过");
            }
            
            return Res.success(result);
        } catch (Exception e) {
            log.error("获取维修进度失败", e);
            return Res.error("获取维修进度失败: " + e.getMessage());
        }
    }

    /**
     * 获取维修类别列表
     * @return 维修类别列表
     */
    @RequestMapping("/getFixCategories")
    public Res getFixCategories() {
        try {
            Dict result = Dict.create()
                    .set("categories", new Object[][]{
                            {"qualityIssueRepair", "质量问题维修"},
                            {"humanIssueRepair", "人为问题维修"}
                    });
            return Res.success(result);
        } catch (Exception e) {
            log.error("获取维修类别列表失败", e);
            return Res.error("获取维修类别列表失败: " + e.getMessage());
        }
    }

    /**
     * 处理维修申请审批
     * @param approvalDTO 审批DTO
     * @return 审批结果
     */
    @RequestMapping("/processApproval")
    public Res processApproval(@RequestBody SysApprovalDTO approvalDTO) {
        Long flowId = approvalDTO.getFlowId();
        Long id = approvalDTO.getId();
        String status = approvalDTO.getStatus();
        String reason = approvalDTO.getReason();

        if (flowId == null || id == null || status == null || status.isEmpty()) {
            return Res.error("审批参数不完整");
        }
        
        try {
            log.info("处理维修审批 - flowId: {}, id: {}, status: {}, reason: {}", flowId, id, status, reason);
            // 更新审批状态
            boolean result = sysApprovalFlowService.updateApprovalStatus(flowId, id, status, reason);
            log.info("审批结果: {}", result);
            if (!result) {
                log.warn("审批操作返回false，但可能后端已成功处理");
                return Res.error("审批失败");
            }
            
            return Res.success("审批成功");
        } catch (Exception e) {
            log.error("处理维修申请审批失败", e);
            return Res.error("处理审批失败：" + e.getMessage());
        }
    }

    /**
     * 根据审批人ID获取审批人姓名
     * @param approverId 审批人ID
     * @return 审批人姓名
     */
    private String getApproverNameByApproverId(Long approverId) {
        try {
            // 先通过approverId查询sys_approver表获取审批人信息
            SysApproverModel approver = sysApproverMapper.getApproverInfoByApproverId(approverId);
            if (approver != null && approver.getUserId() != null) {
                // 再通过userId查询用户表获取用户昵称
                String userNickName = sysUserService.getUserNickNameByUserId(approver.getUserId());
                if (userNickName != null && !userNickName.trim().isEmpty()) {
                    return userNickName;
                }
                // 如果用户昵称为空，返回审批人姓名（清理HTML标签）
                String approverName = approver.getName();
                if (approverName != null && !approverName.trim().isEmpty()) {
                    return approverName.replace("[Blue color]", "").trim();
                }
            }
            return null;
        } catch (Exception e) {
            log.error("获取审批人姓名失败，approverId: {}", approverId, e);
            return null;
        }
    }
    

}
