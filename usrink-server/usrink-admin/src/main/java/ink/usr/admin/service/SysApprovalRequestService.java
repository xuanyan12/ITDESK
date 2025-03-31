package ink.usr.admin.service;

import ink.usr.common.model.mysql.SysApprovalRequestModel;

public interface SysApprovalRequestService {
    SysApprovalRequestModel getByApprovalId(Long approvalId);
}
