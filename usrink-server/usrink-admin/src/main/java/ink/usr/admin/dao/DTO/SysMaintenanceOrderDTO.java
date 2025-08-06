package ink.usr.admin.dao.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysMaintenanceOrderDTO {
    // 维修申请ID
    private Long maintenanceId;
    // 订单类型：1-质量问题,3-人为损坏
    private String orderType;
    // 成本中心
    private String costCenter;
    // 申请人
    private String applicant;
    // 电脑名
    private String ciName;
    // 故障情况描述
    private String problemDescription;
    
    // 查询参数
    private String fixCategory;      // 维修类别
    private String orderStatus;      // 维修状态
    private String company;          // 公司
    
    // 分页参数
    private Integer pageNum = 1;     // 页码
    private Integer pageSize = 10;   // 每页大小
} 