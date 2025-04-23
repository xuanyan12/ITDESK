package ink.usr.admin.controller;

import com.github.pagehelper.Page;
import ink.usr.admin.config.EmailConfig;
import ink.usr.admin.dao.DTO.SysApplyRequestDTO;
import ink.usr.admin.dao.DTO.TempApprovalDTO;
import ink.usr.admin.dao.VO.SysApplyListVO;
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
            String url = sysApplyService.addApply(sysApplyRequestDTO);
            
            // 获取申请人姓名
            String applicantName = sysUserService.getUserNickNameByUserId(sysApplyRequestDTO.getApplicant());
            applicantName = applicantName != null ? applicantName : "未知申请人";
            
            // 构建邮件内容，包含所有申请信息
            StringBuilder emailContent = new StringBuilder();
            emailContent.append("设备申请审批\n\n");
            emailContent.append("申请人: ").append(applicantName).append("\n");
            emailContent.append("申请类别: ").append(sysApplyRequestDTO.getDeviceCategory()).append("\n");
            emailContent.append("电脑类型: ").append(sysApplyRequestDTO.getDeviceType()).append("\n");
            emailContent.append("成本中心: ").append(sysApplyRequestDTO.getCostCenter()).append("\n");
            emailContent.append("所属公司: ").append(sysApplyRequestDTO.getCompany()).append("\n");
            emailContent.append("责任人: ").append(sysUserService.getUserInfoByUserName(sysApplyRequestDTO.getResponsibilityName()).getUserNick()).append("\n");
            emailContent.append("电脑情形: ").append(sysApplyRequestDTO.getDeviceSituation()).append("\n");
            emailContent.append("公司系统: ").append(sysApplyRequestDTO.getCompanySystem()).append("\n");
            emailContent.append("申请理由: ").append(sysApplyRequestDTO.getReason()).append("\n");
            
            if (sysApplyRequestDTO.getCiName() != null && !sysApplyRequestDTO.getCiName().isEmpty()) {
                emailContent.append("需要更换的电脑: ").append(sysApplyRequestDTO.getCiName()).append("\n");
            }
            
            emailContent.append("\n请点击以下链接进行审批: \n").append(url);

            // 发邮件给一级审批人
            // 1.获取一级审批人id
            Long approverId = sysApproverService.getApproverId(sysApplyRequestDTO.getApplicant());
            // 2.根据审批人id获取邮箱
            String email = sysUserService.getUserInfoByUserName(sysUserService.getNameByUserId(approverId)).getEmail();
            String approverEmail = email;
            
            // 组装邮件主题
            String emailSubject = String.format("设备申请审批 - %s - %s", applicantName, sysApplyRequestDTO.getDeviceCategory());
            
            // 发送邮件
            emailConfig.sendMail(approverEmail, emailSubject, emailContent.toString());
            
            return Res.success("申请提交成功，已发送审批邮件");
        } catch (Exception e) {
            log.error("申请提交失败", e);
            return Res.error("申请提交失败: " + e.getMessage());
        }
    }

    /**
     * 根据用户id找到该用户所有的审批流
     * @return
     */
    @RequestMapping("/getApprovalListById")
    public Res getApprovalListById(){
        ShiroUserInfo shiroUserInfo = ShiroUtil.getShiroUserInfo();
        Long userId = shiroUserInfo.getUserId();
        // 通过userId获得approverId
        Long approverId = sysApproverService.getApproverId(userId);
        // 通过approverId找到该用户所有的审批流
        Page<Object> pages = PageUtil.startPage();
        List<SysApprovalFlowModel> sysApprovalFlowList =  sysApprovalFlowService.getApprovalFlowListByApproverId(approverId);

        // 遍历每一条flowList，找到每一条的approvalRequest的内容，并拼接为新的List
        List<SysApprovalRequestListVO> newList = new ArrayList<>();
        SysApprovalRequestModel sysApprovalRequestModel = null;
        for(SysApprovalFlowModel singleOfList : sysApprovalFlowList){
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

        if (flowId == null || token == null || token.isEmpty() || id == null || status == null) {
            return Res.error("审批参数不完整");
        }

        // 验证状态值是否合法
        if (!status.equals("审批通过") && !status.equals("审批不通过")) {
            return Res.error("审批状态值不合法");
        }

        try {
            // 验证token是否有效
            boolean isValidToken = sysApprovalFlowService.validateApprovalToken(flowId, token);
            if (!isValidToken) {
                return Res.error("凭证为空或凭证过期，请重新登录！");
            }

            // 将前端传来的状态值转换为数据库中使用的状态值
            String dbStatus = status.equals("审批通过") ? "审批通过" : "审批不通过";

            // 更新审批状态
            boolean result = sysApprovalFlowService.updateApprovalStatus(flowId, id, dbStatus);
            if (!result) {
                return Res.error("审批操作失败");
            }

            return Res.success("审批操作成功");
        } catch (Exception e) {
            log.error("提交临时审批结果失败", e);
            return Res.error("提交审批结果失败：" + e.getMessage());
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
}
