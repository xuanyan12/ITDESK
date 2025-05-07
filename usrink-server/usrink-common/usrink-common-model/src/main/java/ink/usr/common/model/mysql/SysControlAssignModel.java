package ink.usr.common.model.mysql;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysControlAssignModel {
    // 分配id
    private int id;
    // 电脑名
    private String ciName;
    // 电脑类型
    private String deviceType;
    // 电脑情形
    private String deviceSituation;
    // 订单号
    private Long approvalId;
    // 公司
    private String company;
    // 使用人ID
    private Long applicant;
    // 所分配电脑的上一个使用者nt账号
    private String lastLeastUserNtAccount;
    // 分配人NT账号
    private String assignor;
    // 分配时间
    private String assignTime;
    // 分配状态
    private String assignStatus;

}
