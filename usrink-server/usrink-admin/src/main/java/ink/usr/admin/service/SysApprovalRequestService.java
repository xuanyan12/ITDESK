package ink.usr.admin.service;

import ink.usr.admin.dao.VO.SysApprovalRequestListVO;
import ink.usr.common.model.mysql.SysApprovalRequestModel;

public interface SysApprovalRequestService {
    SysApprovalRequestModel getByApprovalId(Long approvalId);

    SysApprovalRequestListVO getInfoByApprovalId(Long approvalId);
}
