package ink.usr.admin.controller;

import com.github.pagehelper.Page;
import ink.usr.admin.dao.DTO.SysApplyDTO;
import ink.usr.admin.dao.VO.SysApprovalRequestListVO;
import ink.usr.admin.dao.VO.SysApproversVO;
import ink.usr.admin.service.*;
import ink.usr.common.core.constants.Constants;
import ink.usr.common.core.domain.Dict;
import ink.usr.common.core.domain.Res;
import ink.usr.common.core.utils.DateUtil;
import ink.usr.common.core.utils.PageUtil;
import ink.usr.common.core.utils.ServletUtil;
import ink.usr.common.model.mysql.SysApprovalFlowModel;
import ink.usr.common.model.mysql.SysApprovalRequestModel;
import ink.usr.common.model.mysql.SysApproverModel;
import ink.usr.framework.shiro.domain.ShiroUserInfo;
import ink.usr.framework.shiro.utils.ShiroUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
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

    /**
     * 根据id获取对应的设备申请列表
     * @return
     */
    @RequestMapping("/getApplyList")
    public Res getApplyList(){
        Page<Object> pages = PageUtil.startPage();
        ShiroUserInfo shiroUserInfo = ShiroUtil.getShiroUserInfo();
        Long userId = shiroUserInfo.getUserId();
        List<SysApprovalRequestModel> applyList = sysApplyService.getApplyList(userId);

        Dict result = Dict.create()
                .set("list", applyList)
                .set("total", pages.getTotal());
        return Res.success(result);
    }

    /**
     * 提交设备申请，并生成审批流程
     * @param sysApprovalRequestModel
     * @return
     */
    @RequestMapping("/submitApply")
    public Res submitApply(@RequestBody SysApprovalRequestModel sysApprovalRequestModel){

        //  1.先创建一个request,并创建一个属于部门leader的一级工作流与二级工作流,获得带有唯一标识的url
        String url = sysApplyService.addApply(sysApprovalRequestModel);
        //  2.发送带有唯一标识的邮件
        //  3.再创建一个属于IT部门审批者的二级工作流
        //  4.再通过邮件发送链接给对应的审批者进行审批(一级工作流审批者),一级通过后触发邮件给二级审批者
        return Res.success();
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
        for(SysApprovalFlowModel singleOfList : sysApprovalFlowList){
            SysApprovalRequestListVO objects = new SysApprovalRequestListVO();
            Long approvalId = singleOfList.getApprovalId();
            SysApprovalRequestModel sysApprovalRequestModel = sysApprovalRequestService.getByApprovalId(approvalId);
            // 找到申请人姓名并返回
            String userName = sysUserService.getNameByUserId(sysApprovalRequestModel.getApplicant());
            objects.setUserName(userName);
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
            SysApprovalRequestModel requestModel = sysApprovalRequestService.getByApprovalId(flowModel.getApprovalId());
            if (requestModel == null) {
                return Res.error("未找到申请详情");
            }
            
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
//    @RequestMapping("/submitTempApproval")
//    public Res submitTempApproval(@RequestBody Dict params) {
//        Long flowId = params.getLong("flowId");
//        String token = params.getStr("token");
//        Long id = params.getLong("id");
//        String status = params.getStr("status");
//        String comment = params.getStr("comment");
//
//        if (flowId == null || token == null || token.isEmpty() || id == null || status == null) {
//            return Res.error("审批参数不完整");
//        }
//
//        try {
//            // 验证token是否有效
//            boolean isValidToken = sysApprovalFlowService.validateApprovalToken(flowId, token);
//            if (!isValidToken) {
//                return Res.error("凭证为空或凭证过期，请重新登录！");
//            }
//
//            // 更新审批状态
//            boolean result = sysApprovalFlowService.updateApprovalStatus(flowId, id, status, comment);
//            if (!result) {
//                return Res.error("审批操作失败");
//            }
//
//            return Res.success("审批操作成功");
//        } catch (Exception e) {
//            log.error("提交临时审批结果失败", e);
//            return Res.error("提交审批结果失败：" + e.getMessage());
//        }
//    }
}
