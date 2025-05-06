package ink.usr.admin.service.Impl;

import ink.usr.admin.mapper.SysApplyMapper;
import ink.usr.admin.mapper.SysApproverMapper;
import ink.usr.admin.service.SysApplyService;
import ink.usr.admin.service.SysApproverService;
import ink.usr.common.model.mysql.SysApprovalFlowModel;
import ink.usr.common.model.mysql.SysApprovalRequestModel;
import ink.usr.common.model.mysql.SysApprovalTokenModel;
import ink.usr.common.model.mysql.SysApproverModel;
import ink.usr.framework.shiro.domain.ShiroUserInfo;
import ink.usr.framework.shiro.utils.ShiroUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SysApproverServiceImpl implements SysApproverService {

    @Autowired
    private SysApproverMapper sysApproverMapper;

    @Override
    public Long getApproverId(Long userId, String costCenter) {
        Long ApproverId = sysApproverMapper.getApproverIdByUserId(userId);
        return ApproverId;
    }

    @Override
    public Long getApproverIdUseCostCenter(Long userId, String costCenter) {
        Long ApproverId = sysApproverMapper.getApproverIdUseCostCenter(userId, costCenter);
        return ApproverId;
    }

    @Override
    public List<Long> getApproverIdList(Long userId) {
        List<Long> approverIdListBySingleApproverId = sysApproverMapper.getApproverIdListBySingleApproverId(userId);
        return approverIdListBySingleApproverId;
    }

    public Long getApproverInfoByApproverId(Long approverId) {
        SysApproverModel approverInfoByApproverId = sysApproverMapper.getApproverInfoByApproverId(approverId);
        return approverInfoByApproverId.getUserId();
    }
    
    @Override
    public List<Map<String, Object>> getApproverListWithCostCenter(Long userId) {
        // 获取用户的审批人ID列表
        List<Long> approverIds = getApproverIdList(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 对每个审批人ID，获取其详细信息并构建返回结果
        for (Long approverId : approverIds) {
            SysApproverModel approver = sysApproverMapper.getApproverInfoByApproverId(approverId);
            if (approver != null) {
                Map<String, Object> approverInfo = new HashMap<>();
                approverInfo.put("id", approverId);
                approverInfo.put("costCenterName", approver.getCostCenter()); // 使用成本中心作为名称
                result.add(approverInfo);
            }
        }
        
        return result;
    }

    @Override
    public List<String> getCostCenterList() {
        List<String> costCenterList = sysApproverMapper.getCostCenterList();
        return costCenterList;
    }
}
