package ink.usr.admin.dao.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysControlRecordQueryDTO {
    // 电脑名称
    private String ciName;
    
    // 开始时间
    private String startTime;
    
    // 结束时间
    private String endTime;
    
    // 分页属性
    private long pageNum = 1;
    private long pageSize = 10;
} 