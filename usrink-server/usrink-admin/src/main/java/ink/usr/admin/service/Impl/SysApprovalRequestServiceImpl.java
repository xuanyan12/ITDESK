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
        // 优化：使用一次JOIN查询替代多次查询
        SysApprovalRequestListVO sysApprovalRequestListVO = sysApplyMapper.getApprovalInfoWithUserNames(approvalId);

        if (sysApprovalRequestListVO != null) {
            // 处理状态值格式化
            if (sysApprovalRequestListVO.getStatus() != null) {
                if ("已通过".equals(sysApprovalRequestListVO.getStatus())) {
                    sysApprovalRequestListVO.setStatus("审批通过");
                } else if ("已驳回".equals(sysApprovalRequestListVO.getStatus())) {
                    sysApprovalRequestListVO.setStatus("审批不通过");
                }
            }
        }

        return sysApprovalRequestListVO;
    }


}
