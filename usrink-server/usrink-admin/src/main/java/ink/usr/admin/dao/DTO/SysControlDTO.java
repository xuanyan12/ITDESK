package ink.usr.admin.dao.DTO;

import ink.usr.common.model.mysql.SysControlModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysControlDTO extends SysControlModel {
    // 分页的第几页
    private long pageNum;
    // 一页的大小
    private long pageSize;
}
