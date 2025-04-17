package ink.usr.admin.dao.DTO;

import ink.usr.common.model.mysql.SysApprovalRequestModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysApplyRequestDTO extends SysApprovalRequestModel {
    // 责任人name
    private String responsibilityName;
}
