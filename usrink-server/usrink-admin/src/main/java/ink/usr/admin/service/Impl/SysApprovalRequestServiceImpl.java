package ink.usr.admin.service.Impl;

import ink.usr.admin.dao.VO.SysApprovalRequestListVO;
import ink.usr.admin.mapper.SysApplyMapper;
import ink.usr.admin.mapper.SysApprovalFlowMapper;
import ink.usr.admin.mapper.SysUserMapper;
import ink.usr.admin.service.SysApprovalRequestService;
import ink.usr.common.model.mysql.SysApprovalFlowModel;
import ink.usr.common.model.mysql.SysApprovalRequestModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysApprovalRequestServiceImpl implements SysApprovalRequestService {

    @Autowired
    private SysApplyMapper sysApplyMapper;
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private SysApprovalFlowMapper sysApprovalFlowMapper;
    @Override
    public SysApprovalRequestModel getByApprovalId(Long approvalId) {
        SysApprovalRequestModel sysApprovalRequestList = sysApplyMapper.getByApprovalId(approvalId);
        return sysApprovalRequestList;
    }

    @Override
    public SysApprovalRequestListVO getInfoByApprovalId(Long approvalId) {
        SysApprovalRequestModel sysApprovalRequestList = sysApplyMapper.getByApprovalId(approvalId);
        SysApprovalRequestListVO sysApprovalRequestListVO = new SysApprovalRequestListVO();
        BeanUtils.copyProperties(sysApprovalRequestList, sysApprovalRequestListVO);
        
        // 处理状态值
        if (sysApprovalRequestListVO.getStatus() != null) {
            // 确保状态值符合前端期望的格式
            if ("已通过".equals(sysApprovalRequestListVO.getStatus())) {
                sysApprovalRequestListVO.setStatus("审批通过");
            } else if ("已驳回".equals(sysApprovalRequestListVO.getStatus())) {
                sysApprovalRequestListVO.setStatus("审批不通过");
            }
        }
        
        // 获取审批流信息和审批理由
        List<SysApprovalFlowModel> approvalFlows = sysApprovalFlowMapper.getApprovalFlowsByApprovalId(approvalId);
        if (approvalFlows != null && !approvalFlows.isEmpty()) {
            // 获取最新的审批流信息（通常是状态为非"审批中"的最后一个审批流）
            for (SysApprovalFlowModel flow : approvalFlows) {
                if (!"审批中".equals(flow.getStatus())) {
                    sysApprovalRequestListVO.setApprovalReason(flow.getApprovalReason());
                    break;
                }
            }
        }
        
        sysApprovalRequestListVO.setUserName(sysUserMapper.getUserNickNameByUserId(sysApprovalRequestList.getApplicant()));
        sysApprovalRequestListVO.setResponsibilityName(sysUserMapper.getUserNickNameByUserId(sysApprovalRequestList.getResponsibility()));
        return sysApprovalRequestListVO;
    }

}
