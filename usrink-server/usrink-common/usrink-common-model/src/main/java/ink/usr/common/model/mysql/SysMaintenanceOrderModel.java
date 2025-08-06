package ink.usr.common.model.mysql;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysMaintenanceOrderModel implements Serializable {
    // 维修订单ID
    private Long orderId;
    // 维修申请ID
    private Long maintenanceId;
    // 订单编号
    private String orderNumber;
    // 订单类型：1-保修期内质量问题,2-保修期外质量问题,3-保修期内人为损坏,4-保修期外人为损坏
    private String orderType;
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
    // 订单状态：待审批,审批中,已完成,已取消
    private String orderStatus;
    // 创建时间
    private String createTime;
    // 更新时间
    private String updateTime;
} 