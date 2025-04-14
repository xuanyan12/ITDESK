package ink.usr.admin.service.Impl;

import ink.usr.admin.mapper.SysApplyMapper;
import ink.usr.admin.service.SysApplyService;
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
public class SysApplyServiceImpl implements SysApplyService {

    @Autowired
    private SysApplyMapper sysApplyMapper;

    @Override
    public List<SysApprovalRequestModel> getApplyList(Long userId) {
        List<SysApprovalRequestModel> applyList = sysApplyMapper.getApplyList(userId);
        return applyList;
    }

    @Override
    @Transactional
    public String addApply(SysApprovalRequestModel sysApprovalRequestModel) {
        //  1.新建设备申请订单
        //  1.1 从shiro中获取用户id
        ShiroUserInfo shiroUserInfo = ShiroUtil.getShiroUserInfo();
        Long userId = shiroUserInfo.getUserId();
        sysApprovalRequestModel.setApplicant(userId);
        //  1.2 设置审批流有效期，默认值为2天
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timestamp = LocalDateTime.now().plusDays(2).format(formatter);
        String createTime = LocalDateTime.now().format(formatter);
        sysApprovalRequestModel.setTimestamp(timestamp);
        sysApprovalRequestModel.setCreatedAt(createTime);
        sysApprovalRequestModel.setUpdatedAt(createTime);
        sysApplyMapper.addApply(sysApprovalRequestModel);
        //  2.创建一个属于部门leader的一级审批工作流
        //  2.1 获取订单id
        Long approvalId = sysApprovalRequestModel.getApprovalId();
        //  2.2 获取审批人id，审批人id需要根据申请人id去找到对应的部门号，再通过部门号在审批表里找到对应的审批者
        //  2.2.1 根据申请人id获取部门号
        Long department = sysApplyMapper.getDepartmentByApplyUserId(userId);
        //  2.2.2 根据部门号找到审批者id
        Long approverId = sysApplyMapper.getApproverIdByDepartment(department);
        //  2.3 构建一级审批流实体
        SysApprovalFlowModel sysApprovalFlowModel = new SysApprovalFlowModel();
        sysApprovalFlowModel.setApprovalId(approvalId);
        sysApprovalFlowModel.setApproverId(approverId);
        sysApprovalFlowModel.setStage(1);
        sysApprovalFlowModel.setStatus("审批中");
        sysApprovalFlowModel.setCreatedAt(createTime);
        sysApprovalFlowModel.setUpdatedAt(createTime);
        sysApplyMapper.addApplyFlow(sysApprovalFlowModel);
        Long flow1Id = sysApprovalFlowModel.getFlowId();
        //  3.创建一个属于IT Approver的二级审批工作流
        SysApprovalFlowModel sysApprovalFlowModel4ITApprover = new SysApprovalFlowModel();
        sysApprovalFlowModel4ITApprover.setApprovalId(approvalId);
        sysApprovalFlowModel4ITApprover.setApproverId(Long.valueOf(3));
        sysApprovalFlowModel4ITApprover.setStage(2);
        sysApprovalFlowModel4ITApprover.setStatus("审批中");
        sysApprovalFlowModel4ITApprover.setCreatedAt(createTime);
        sysApprovalFlowModel4ITApprover.setUpdatedAt(createTime);
        sysApplyMapper.addApplyFlow(sysApprovalFlowModel4ITApprover);
        //  4.发送邮件给一级审批工作流用户，附带可不登陆直接审批的链接
        //  4.1 生成唯一token并存入
        SysApprovalTokenModel sysApprovalTokenModel = new SysApprovalTokenModel();
        sysApprovalTokenModel.setFlowId(flow1Id);
        String token = UUID.randomUUID().toString().replace("-", "");
        sysApprovalTokenModel.setToken(token);
        sysApprovalTokenModel.setCreatedAt(createTime);
        sysApprovalTokenModel.setExpireTime(timestamp);
        sysApprovalTokenModel.setUpdatedAt(createTime);
        sysApprovalTokenModel.setUsed(0);

        sysApplyMapper.InsertToken(sysApprovalTokenModel);
        //  4.2 发送邮件
        String url = "http://localhost:5173/public-page" + "?flowId=" + flow1Id + "&token=" + token;
        return url;
    }
}
