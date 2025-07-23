package ink.usr.common.model.mysql;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysControlMaintenanceModel implements Serializable {

    // 电脑名
    private String ciName;

    // 电脑维修记录
    private String maintenanceRecord;

    // 电脑维修时间
    private String updateTime;
}