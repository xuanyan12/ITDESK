package ink.usr.admin.dao.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysControlMaintenanceQueryDTO {
    // 电脑名称
    private String ciName;
    // 维修时间范围
    private String startTime;
    private String endTime;
    // 分页属性
    private long pageNum = 1;
    private long pageSize = 10;
}
