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
public class SysControlMaintenanceModel implements Serializable {

    // 主键ID
    private Long id;
    
    // 维修订单号
    private String orderNumber;
    
    // 电脑名
    private String ciName;
    
    // 申请人
    private String applicant;
    
    // 维修类别
    private String maintenanceCategory;
    
    // 故障描述
    private String problemDescription;
    
    // 维修结果
    private String maintenanceResult;
    
    // 维修操作记录
    private String maintenanceRecord;
    
    // 维修备注
    private String maintenanceRemark;
    
    // 维修完成时间
    private String maintenanceTime;
    
    // 记录创建时间
    private String createTime;
    
    // 记录更新时间
    private String updateTime;
    
    // 记录状态：1-有效，0-无效
    private Integer status;
}