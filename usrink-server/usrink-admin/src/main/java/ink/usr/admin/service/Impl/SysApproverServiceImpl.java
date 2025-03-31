package ink.usr.admin.service.Impl;

import ink.usr.admin.mapper.SysApplyMapper;
import ink.usr.admin.mapper.SysApproverMapper;
import ink.usr.admin.service.SysApplyService;
import ink.usr.admin.service.SysApproverService;
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
public class SysApproverServiceImpl implements SysApproverService {

    @Autowired
    private SysApproverMapper sysApproverMapper;

    @Override
    public Long getApproverId(Long userId) {
        Long ApproverId = sysApproverMapper.getApproverIdByUserId(userId);
        return ApproverId;
    }
}
