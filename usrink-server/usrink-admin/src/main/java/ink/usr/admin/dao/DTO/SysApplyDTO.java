package ink.usr.admin.dao.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysApplyDTO {

    // 设备大类
    private String deviceCategory;
    // 设备小类
    private String deviceType;
    // 设备名称
    private String deviceName;
    // 设备数量
    private String quantity;
    // 申请理由
    private String reason;

}
