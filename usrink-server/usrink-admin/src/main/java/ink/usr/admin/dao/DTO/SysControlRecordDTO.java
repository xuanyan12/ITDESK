package ink.usr.admin.dao.DTO;

import ink.usr.common.model.mysql.SysControlModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysControlRecordDTO extends SysControlModel {

    // 更新时间
    public String updateTime;
}
