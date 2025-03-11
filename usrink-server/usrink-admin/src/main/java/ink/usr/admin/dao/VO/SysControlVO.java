package ink.usr.admin.dao.VO;

import ink.usr.common.model.mysql.SysControlModel;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysControlVO{
    // 查询到的设备列表
    private List<SysControlModel> sysControlModelLists;
    // 记录数
    private long total;
    // 分页的第几页
    private long pageNum;
    // 一页的大小
    private long pageSize;
}
