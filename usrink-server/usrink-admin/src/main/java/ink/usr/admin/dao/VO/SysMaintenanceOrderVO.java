package ink.usr.admin.dao.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysMaintenanceOrderVO {
    // 维修订单ID
    private Long orderId;
    // 维修申请ID
    private Long maintenanceId;
    // 订单编号
    private String orderNumber;
    // 订单类型
    private String orderType;
    // 订单类型名称
    private String orderTypeName;
    // 成本中心
    private String costCenter;
    // 申请人
    private String applicant;
    // 电脑名
    private String ciName;
    // 故障情况描述
    private String problemDescription;
    // 维修时间
    private String maintenanceTime;
    // 维修备注
    private String maintenanceRemark;
    // 订单状态
    private String orderStatus;
    // 创建时间
    private String createTime;
    // 更新时间
    private String updateTime;
} 