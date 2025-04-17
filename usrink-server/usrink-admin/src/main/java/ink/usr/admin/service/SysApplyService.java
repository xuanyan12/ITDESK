package ink.usr.admin.service;

import ink.usr.admin.dao.DTO.SysApplyRequestDTO;
import ink.usr.admin.dao.VO.SysApplyListVO;
import ink.usr.common.model.mysql.SysApprovalRequestModel;

import java.util.List;

public interface SysApplyService {

    List<SysApplyListVO> getApplyList(Long userId);

    String addApply(SysApplyRequestDTO sysApplyRequestDTO);
}
