package ink.usr.admin.dao.VO;

import ink.usr.common.model.mysql.SysApprovalRequestModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysApplyListVO extends SysApprovalRequestModel {
    // 申请人名称
    private String userName;
    // 责任人名称
    private String responsibilityName;
    // 分配状态
    private String assignStatus;
    // 维修类别
    private String fixCategory;
}
