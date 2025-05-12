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
}
