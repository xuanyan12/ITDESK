package ink.usr.admin.controller;

import com.github.pagehelper.Page;
import ink.usr.admin.config.EmailConfig;
import ink.usr.admin.dao.DTO.SysApplyRequestDTO;
import ink.usr.admin.dao.DTO.SysApprovalDTO;
import ink.usr.admin.dao.DTO.TempApprovalDTO;
import ink.usr.admin.dao.VO.SysApplyListVO;
import ink.usr.admin.dao.VO.SysApprovalFlowVO;
import ink.usr.admin.dao.VO.SysApprovalRequestListVO;
import ink.usr.admin.dao.VO.SysApproversVO;
import ink.usr.admin.service.*;
import ink.usr.common.core.domain.Dict;
import ink.usr.common.core.domain.Res;
import ink.usr.common.core.utils.PageUtil;
import ink.usr.common.model.mysql.SysApprovalFlowModel;
import ink.usr.common.model.mysql.SysApprovalRequestModel;
import ink.usr.common.model.mysql.SysUserModel;
import ink.usr.framework.shiro.domain.ShiroUserInfo;
import ink.usr.framework.shiro.utils.ShiroUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@Slf4j
@RequestMapping("/sysApply")
public class SysApplyController {

    @Autowired
    private SysApplyService sysApplyService;

    @Autowired
    private SysApproverService sysApproverService;

    @Autowired
    private SysApprovalFlowService sysApprovalFlowService;

    @Autowired
    private SysApprovalRequestService sysApprovalRequestService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private EmailConfig emailConfig;

    /**
     * 根据id获取对应的设备申请列表
     * @return
     */
    @RequestMapping("/getApplyList")
    public Res getApplyList(){
        try {
            Page<Object> pages = PageUtil.startPage();
            ShiroUserInfo shiroUserInfo = ShiroUtil.getShiroUserInfo();
            
            // Check if user info is available
            if (shiroUserInfo == null) {
                return Res.error("用户未登录或会话已过期");
            }
            
            Long userId = shiroUserInfo.getUserId();
            
            // Check if user ID is valid
            if (userId == null) {
                return Res.error("无法获取用户ID");
            }
            List<SysApplyListVO> applyList = sysApplyService.getApplyList(userId);

            Dict result = Dict.create()
                    .set("list", applyList)
                    .set("total", pages.getTotal());
            return Res.success(result);
        } catch (Exception e) {
            log.error("获取申请列表失败", e);
            return Res.error("获取申请列表失败: " + e.getMessage());
        }
    }

    /**
     * 提交设备申请，并生成审批流程
     * @param sysApplyRequestDTO
     * @return
     */
    @RequestMapping("/submitApply")
    public Res submitApply(@RequestBody SysApplyRequestDTO sysApplyRequestDTO){
        try {
            //  1.先创建一个request,并创建一个属于部门leader的一级工作流与二级工作流,获得带有唯一标识的url
            String result = sysApplyService.addApply(sysApplyRequestDTO);
            
            // 检查是否为自动审批通过的情况
            if ("申请已自动审批通过，已进入分配流程".equals(result)) {
                // 发送审批通过邮件给申请人
                try {
                    // 获取申请人姓名
                    String applicantName = sysUserService.getUserNickNameByUserId(sysUserService.getUserIdByUserName(sysApplyRequestDTO.getUserName()));
                    applicantName = applicantName != null ? applicantName : "未知申请人";
                    
                    // 获取申请人邮箱和部门
                    String applicantUserName = sysApplyRequestDTO.getUserName();
                    SysUserModel applicantUserInfo = sysUserService.getUserInfoByUserName(applicantUserName);
                    String applicantEmail = applicantUserInfo != null ? applicantUserInfo.getEmail() : null;
                    String applicantDepartment = applicantUserInfo != null ? applicantUserInfo.getDepartment() : "";
                    
                    if (applicantEmail != null && !applicantEmail.isEmpty()) {
                        // 获取当前时间
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        String approvalTime = LocalDateTime.now().format(formatter);
                        
                        // 构建邮件内容
                        String emailContent = emailConfig.buildApprovalPassedEmailContent(
                                applicantName,
                                applicantDepartment,
                                approvalTime
                        );
                        
                        String emailSubject = emailConfig.buildApprovalPassedEmailSubject();
                        
                        // 发送邮件
                        emailConfig.sendMail(applicantEmail, emailSubject, emailContent);
                    }
                } catch (Exception e) {
                    // 记录日志但不影响主流程
                    log.error("发送自动审批通过邮件失败", e);
                }
                
                return Res.success(result);
            }
            
            // 需要审批的情况：发送邮件给一级审批人
            String url = result;
            
            // 获取申请人姓名
            String applicantName = sysUserService.getUserNickNameByUserId(sysUserService.getUserIdByUserName(sysApplyRequestDTO.getUserName()));
            applicantName = applicantName != null ? applicantName : "未知申请人";

            // 获取责任人姓名
            String responsibilityName = sysUserService.getUserInfoByUserName(sysApplyRequestDTO.getResponsibilityName()).getUserNick();
            
            // 发邮件给一级审批人
            // 1.获取一级审批人id
            Long approverId = sysApproverService.getApproverIdUseCostCenter(sysUserService.getUserIdByUserName(sysApplyRequestDTO.getUserName()), sysApplyRequestDTO.getCostCenter());
            // 2.根据审批人id获取邮箱
            // 通过审批人id获取其userId
            Long userApproverId = sysApproverService.getApproverInfoByApproverId(approverId);
            String approverEmail = sysUserService.getUserInfoByUserName(sysUserService.getNameByUserId(userApproverId)).getEmail();
            
            // 获取申请人部门信息
            SysUserModel applicantUserInfo = sysUserService.getUserInfoByUserName(sysApplyRequestDTO.getUserName());
            String applicantDepartment = applicantUserInfo != null ? applicantUserInfo.getDepartment() : "";
            
            // 获取申请时间（当前时间）
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String applyTime = LocalDateTime.now().format(formatter);
            
            // 使用EmailConfig构建和发送邮件
            String emailContent = emailConfig.buildApplyEmailContent(
                    applicantName,
                    applicantDepartment,
                    sysApplyRequestDTO.getDeviceCategory(),
                    sysApplyRequestDTO.getDeviceType(),
                    sysApplyRequestDTO.getCostCenter(),
                    sysApplyRequestDTO.getCompany(),
                    responsibilityName,
                    sysApplyRequestDTO.getDeviceSituation(),
                    sysApplyRequestDTO.getCompanySystem(),
                    sysApplyRequestDTO.getReason(),
                    sysApplyRequestDTO.getCiName(),
                    url,
                    applyTime
            );
            
            String emailSubject = emailConfig.buildApplyEmailSubject(applicantName, applicantDepartment, sysApplyRequestDTO.getDeviceCategory());
            
            // 发送邮件
            emailConfig.sendMail(approverEmail, emailSubject, emailContent);
            
            return Res.success("申请提交成功，已发送审批邮件");
        } catch (Exception e) {
            log.error("申请提交失败", e);
            return Res.error("申请提交失败: " + e.getMessage());
        }
    }

    /**
     * 根据用户id找到该用户所有的审批流，0代表待办审批，1代表审批历史
     * @return
     */
    @RequestMapping("/getApprovalListById")
    public Res getApprovalListById(@RequestParam("approvalType") Long approvalType){
        ShiroUserInfo shiroUserInfo = ShiroUtil.getShiroUserInfo();
        Long userId = shiroUserInfo.getUserId();
        // 通过userId获得approverId

        String costCenter = sysUserService.getUserInfoByUserName(sysUserService.getNameByUserId(userId)).getCostCenter();
        Long approverId = sysApproverService.getApproverIdUseCostCenter(userId, costCenter);
        // 通过approverId找到该用户所有的审批流
        Page<Object> pages = PageUtil.startPage();
        List<SysApprovalFlowVO> sysApprovalFlowList =  sysApprovalFlowService.getApprovalFlowListByApproverId(approverId, approvalType);

        // 遍历每一条flowList，找到每一条的approvalRequest的内容，并拼接为新的List
        List<SysApprovalRequestListVO> newList = new ArrayList<>();
        SysApprovalRequestModel sysApprovalRequestModel = null;
        for(SysApprovalFlowVO singleOfList : sysApprovalFlowList){
            SysApprovalRequestListVO objects = new SysApprovalRequestListVO();
            Long approvalId = singleOfList.getApprovalId();
            sysApprovalRequestModel = sysApprovalRequestService.getByApprovalId(approvalId);
            // 找到申请人姓名并返回
            String userName = sysUserService.getUserNickNameByUserId(sysApprovalRequestModel.getApplicant());
            String responsibilityName = sysUserService.getUserNickNameByUserId(sysApprovalRequestModel.getResponsibility());
            if(userName != null){
                objects.setUserName(userName);
            }
            objects.setResponsibilityName(responsibilityName);
            objects.setApproverId(singleOfList.getApproverId());
            objects.setFlowId(singleOfList.getFlowId());
            BeanUtils.copyProperties(sysApprovalRequestModel,objects);
            objects.setStatus(singleOfList.getStatus());
            objects.setStatus1Signal(singleOfList.getStatus1Signal());
            objects.setApprovalReason(singleOfList.getApprovalReason());
//            BeanUtils.copyProperties(singleOfList,objects);
            newList.add(objects);
        }

        Dict result = Dict.create()
                .set("list", newList)
                .set("total", pages.getTotal());

        return Res.success(result);
    }

    /**
     * 通过approvalId找到approver
     * @param approvalId
     * @return
     */
    @RequestMapping("/getApproversByAprrovalId")
    public Res getApproversByAprrovalId(@RequestParam("approvalId") Long approvalId){
        SysApproversVO sysApproversVO = sysApprovalFlowService.getApproversByAprrovalId(approvalId);
        Dict result = Dict.create()
                .set("list", sysApproversVO);
        return Res.success(result);
    }

    /**
     * 临时审批获取需要审批的申请内容
     * @return
     */
    @RequestMapping("/tempApproval")
    public Res tempApproval(Long flowId, String token) {
        if (flowId == null || token == null || token.isEmpty()) {
            return Res.error("审批参数不完整");
        }
        
        try {
            // 验证token是否有效
            boolean isValidToken = sysApprovalFlowService.validateApprovalToken(flowId, token);
            if (!isValidToken) {
                return Res.error("凭证为空或凭证过期，请重新登录！");
            }
            
            // 获取审批流信息
            SysApprovalFlowModel flowModel = sysApprovalFlowService.getApprovalFlowById(flowId);
            if (flowModel == null) {
                return Res.error("未找到对应的审批流程");
            }
            
            // 获取申请详情
            SysApprovalRequestListVO requestModel = sysApprovalRequestService.getInfoByApprovalId(flowModel.getApprovalId());
            if (requestModel == null) {
                return Res.error("未找到申请详情");
            }
            requestModel.setStatus(flowModel.getStatus());
            return Res.success(requestModel);
        } catch (Exception e) {
            log.error("获取临时审批信息失败", e);
            return Res.error("获取审批信息失败：" + e.getMessage());
        }
    }
    
    /**
     * 提交临时审批结果
     * @return
     */
    @RequestMapping("/submitTempApproval")
    public Res submitTempApproval(@RequestBody TempApprovalDTO tempApprovalDTO) {
        Long flowId = tempApprovalDTO.getFlowId();
        String token = tempApprovalDTO.getToken();
        Long id = tempApprovalDTO.getId();
        String status = tempApprovalDTO.getStatus();
        String reason = tempApprovalDTO.getReason();

        if (flowId == null || token == null || token.isEmpty() || id == null || status == null || status.isEmpty()) {
            return Res.error("审批参数不完整");
        }
        
        try {
            // 验证token是否有效
            boolean isValidToken = sysApprovalFlowService.validateApprovalToken(flowId, token);
            if (!isValidToken) {
                return Res.error("凭证为空或凭证过期，请重新登录！");
            }
            
            // 更新审批状态
            boolean result = sysApprovalFlowService.updateApprovalStatus(flowId, id, status, reason);
            if (!result) {
                return Res.error("审批失败");
            }
            
            return Res.success("审批成功");
        } catch (Exception e) {
            log.error("提交临时审批失败", e);
            return Res.error("提交审批失败：" + e.getMessage());
        }
    }

    /**
     * 处理审批
     * @param approvalDTO
     * @return
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
            // 更新审批状态
            boolean result = sysApprovalFlowService.updateApprovalStatus(flowId, id, status, reason);
            if (!result) {
                return Res.error("审批失败");
            }
            
            return Res.success("审批成功");
        } catch (Exception e) {
            log.error("处理审批失败", e);
            return Res.error("处理审批失败：" + e.getMessage());
        }
    }

    /**
     * 根据userName找到用户信息
     * @param userName
     * @return
     * @throws Exception
     */
    @RequestMapping("/getUserInfoByUserName")
    public Res getUserInfoByUserName(@RequestParam("userName") String userName) throws Exception {

        SysUserModel sysUserModel = null;
        try {
            sysUserModel = sysUserService.getUserInfoByUserName(userName);
        } catch (Exception e){
            throw new Exception(e);
        }
        return Res.success(sysUserModel);
    }

    /**
     * 获取某个用户的approverId列表
     * @return
     */
    @RequestMapping("/getApproverIdList")
    public Res getApproverIdList(){
        ShiroUserInfo shiroUserInfo = ShiroUtil.getShiroUserInfo();
        List<Long> approverIdList = sysApproverService.getApproverIdList(shiroUserInfo.getUserId());
        Dict result = Dict.create()
                .set("list", approverIdList);
        return Res.success(result);
    }
    
    /**
     * 获取某个用户的审批人列表（包含成本中心名称）
     * @return 审批人列表，包含ID和成本中心名称
     */
    @RequestMapping("/getApproverList")
    public Res getApproverList(){
        ShiroUserInfo shiroUserInfo = ShiroUtil.getShiroUserInfo();
        List<Map<String, Object>> approverList = sysApproverService.getApproverListWithCostCenter(shiroUserInfo.getUserId());
        Dict result = Dict.create()
                .set("list", approverList);
        return Res.success(result);
    }

    /**
     * 获取当前审批人表中包含的所有成本中心列表
     * @return
     */
    @RequestMapping("/getCostCenterList")
    public Res getCostCenterList(){
        List<String> costCenterList = sysApproverService.getCostCenterList();
        Dict result = Dict.create()
                .set("list", costCenterList);
        return Res.success(result);
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
     * 获取当前用户的审批计数信息（待处理和已处理）
     * @return 待处理和已处理的审批数量
     */
    @RequestMapping("/getApprovalCounts")
    public Res getApprovalCounts() {
        try {
            ShiroUserInfo shiroUserInfo = ShiroUtil.getShiroUserInfo();
            if (shiroUserInfo == null) {
                return Res.error("用户未登录或会话已过期");
            }
            
            Long userId = shiroUserInfo.getUserId();
            if (userId == null) {
                return Res.error("无法获取用户ID");
            }
            
            // 获取用户的所有审批人ID列表（可能在多个成本中心担任审批人）
            List<Long> approverIdList = sysApproverService.getApproverIdList(userId);
            
            if (approverIdList == null || approverIdList.isEmpty()) {
                // 用户不是任何成本中心的审批人
                Dict result = Dict.create()
                        .set("isApprover", false);
                return Res.success(result);
            }
            
            // 统计所有成本中心的待处理和已处理审批数量
            int totalPendingCount = 0;
            int totalProcessedCount = 0;
            
            for (Long approverId : approverIdList) {
                // 获取待处理审批数量 (status=0)
                totalPendingCount += sysApprovalFlowService.getApprovalFlowCountByApproverId(approverId, 0L);
                
                // 获取已处理审批数量 (status=1)
                totalProcessedCount += sysApprovalFlowService.getApprovalFlowCountByApproverId(approverId, 1L);
            }
            
            Dict result = Dict.create()
                    .set("isApprover", true)
                    .set("pendingCount", totalPendingCount)
                    .set("processedCount", totalProcessedCount);
            
            return Res.success(result);
        } catch (Exception e) {
            log.error("获取审批计数失败", e);
            return Res.error("获取审批计数失败: " + e.getMessage());
        }
    }
}
