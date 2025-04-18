package ink.usr.admin.service.Impl;

import ink.usr.admin.mapper.SysApplyMapper;
import ink.usr.admin.service.SysApprovalRequestService;
import ink.usr.common.model.mysql.SysApprovalRequestModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysApprovalRequestServiceImpl implements SysApprovalRequestService {

    @Autowired
    private SysApplyMapper sysApplyMapper;

    @Override
    public SysApprovalRequestModel getByApprovalId(Long approvalId) {
        SysApprovalRequestModel sysApprovalRequestList = sysApplyMapper.getByApprovalId(approvalId);
        return sysApprovalRequestList;
    }

}
