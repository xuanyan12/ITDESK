package ink.usr.admin.dao.VO;

import ink.usr.common.model.mysql.SysApprovalFlowModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysApprovalFlowVO extends SysApprovalFlowModel {

    // 添加一级审批流是否审批标识 0为“审批中”（一级审批流未审批状态），1为“审批通过”或“审批不通过”（一级审批流已审批状态）
    private int status1Signal = 1;
}
