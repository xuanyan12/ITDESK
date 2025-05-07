package ink.usr.admin.dao.VO;

import ink.usr.common.model.mysql.SysControlAssignModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysControlAssignListVO extends SysControlAssignModel {
    // 使用人
    private String user;
    // 上一个使用人
    private String lastLeastUser;
    // 分配人
    private String assigner;

}
