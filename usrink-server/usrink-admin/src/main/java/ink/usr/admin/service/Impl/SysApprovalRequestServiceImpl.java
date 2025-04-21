package ink.usr.admin.service.Impl;

import ink.usr.admin.dao.VO.SysApprovalRequestListVO;
import ink.usr.admin.mapper.SysApplyMapper;
import ink.usr.admin.mapper.SysUserMapper;
import ink.usr.admin.service.SysApprovalRequestService;
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
        
        sysApprovalRequestListVO.setUserName(sysUserMapper.getUserNickNameByUserId(sysApprovalRequestList.getApplicant()));
        sysApprovalRequestListVO.setResponsibilityName(sysUserMapper.getUserNickNameByUserId(sysApprovalRequestList.getResponsibility()));
        return sysApprovalRequestListVO;
    }

}
