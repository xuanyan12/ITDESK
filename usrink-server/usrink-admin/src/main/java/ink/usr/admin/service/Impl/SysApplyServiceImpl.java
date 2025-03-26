package ink.usr.admin.service.Impl;

import ink.usr.admin.dao.DTO.SysApplyDTO;
import ink.usr.admin.mapper.SysApplyMapper;
import ink.usr.admin.service.SysApplyService;
import ink.usr.common.model.mysql.SysApprovalRequestModel;
import ink.usr.framework.shiro.domain.ShiroUserInfo;
import ink.usr.framework.shiro.utils.ShiroUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
    public void addApply(SysApprovalRequestModel sysApprovalRequestModel) {
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

    }
}
