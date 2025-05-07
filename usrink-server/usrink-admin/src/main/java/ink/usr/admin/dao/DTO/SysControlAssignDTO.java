package ink.usr.admin.dao.DTO;

import ink.usr.common.model.mysql.SysControlAssignModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysControlAssignDTO extends SysControlAssignModel {
    // 电脑的归属人ntAccount
    private String ntAccount;
}
