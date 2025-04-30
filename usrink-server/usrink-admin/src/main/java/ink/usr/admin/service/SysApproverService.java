package ink.usr.admin.service;

import java.util.List;
import java.util.Map;

public interface SysApproverService {

    Long getApproverId(Long userId, String costCenter);

    List<Long> getApproverIdList(Long userId);

    Long getApproverIdUseCostCenter(Long userId, String costCenter);

    Long getApproverInfoByApproverId(Long approverId);
    
    /**
     * 获取带有成本中心名称的审批人列表
     * @param userId 用户ID
     * @return 包含审批人ID和成本中心名称的列表
     */
    List<Map<String, Object>> getApproverListWithCostCenter(Long userId);
}
