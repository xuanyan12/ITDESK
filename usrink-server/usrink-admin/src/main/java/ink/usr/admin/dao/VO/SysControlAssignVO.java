package ink.usr.admin.dao.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysControlAssignVO {

    // 电脑名
    private String ciName;

    // 电脑类型
    private String deviceType;

    // 公司
    private String company;

    // 分配状态
    private String assignStatus;

    // nt账号
    private String ntAccount;

    // 电脑情形
    private String deviceSituation;

    // 申请类别
    private String deviceCategory;

    // 申请理由
    private String reason;
}
