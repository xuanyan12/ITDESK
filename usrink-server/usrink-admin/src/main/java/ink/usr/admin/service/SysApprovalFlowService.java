package ink.usr.admin.service;

import ink.usr.common.model.mysql.SysApprovalFlowModel;

import java.util.List;

public interface SysApprovalFlowService {


    List<SysApprovalFlowModel> getApprovalFlowListByApproverId(Long approverId);
}
