package ink.usr.admin.service;

import ink.usr.admin.dao.VO.SysApproversVO;
import ink.usr.common.model.mysql.SysApprovalFlowModel;

import java.util.List;

public interface SysApprovalFlowService {


    List<SysApprovalFlowModel> getApprovalFlowListByApproverId(Long approverId);

    SysApproversVO getApproversByAprrovalId(Long aprrovalId);
}
