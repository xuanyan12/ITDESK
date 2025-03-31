package ink.usr.admin.dao.VO;

import ink.usr.common.model.mysql.SysApprovalFlowModel;
import ink.usr.common.model.mysql.SysApprovalRequestModel;
import ink.usr.common.model.mysql.SysControlModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysApprovalRequestListVO extends SysApprovalFlowModel {
    // 申请人id
    private Long applicant;
    // 设备名称
    private String deviceName;
    // 设备数量
    private String quantity;
    // 设备大类
    private String deviceCategory;
    // 设备小类
    private String deviceType;
    // 申请理由
    private String reason;
    // 申请人名称
    private String userName;
}
