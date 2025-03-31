package ink.usr.admin.service;

import ink.usr.admin.dao.DTO.SysApplyDTO;
import ink.usr.common.core.domain.Res;
import ink.usr.common.model.mysql.SysApprovalRequestModel;

import java.util.List;

public interface SysApplyService {

    List<SysApprovalRequestModel> getApplyList(Long userId);

    String addApply(SysApprovalRequestModel sysApprovalRequestModel);
}
