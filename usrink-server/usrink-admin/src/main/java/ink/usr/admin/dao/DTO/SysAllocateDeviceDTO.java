package ink.usr.admin.dao.DTO;

import ink.usr.common.model.mysql.SysControlModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysAllocateDeviceDTO extends SysControlModel {

    // 使用人
    private Long applicant;

    // 是否是临时分配
    private Long isTemp;

    // 订单号
    private Long approvalId;
    
    // 电脑归属情况（用于设备领取）
    private String pcClass;
    
    // 申请类型（用于区分普通申请和共享电脑申请）
    private String deviceCategory;
}
