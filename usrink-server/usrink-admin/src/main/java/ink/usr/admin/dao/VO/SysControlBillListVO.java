package ink.usr.admin.dao.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysControlBillListVO {
    // 电脑id
    private long id;
    // 电脑名
    private String ciName;
    // 电脑型号
    private String modelOrVersion;
    // NT账号
    private String ntAccount;
    // 使用人
    private String userName;
    // 所属部门
    private String department;
    // 资产号
    private String wbsNum;
    // 价格
    private double price;
    // 成本中心
    private String costCenter;
    // IFRS总原值
    private double ifrsValue;
    // IFRS总残值
    private double ifrsAllResidualValue;
    // PRC总残值
    private double prcAllResidualValue;
    // 电脑在对应wbs的价值占比
    private String wbsPercent;
    // PRC残值
    private double prcResidualValue;
    // IFPS残值
    private double ifrsResidualValue;
    // 价格的10%
    private double tenPercentPrice;
    // 转卖价
    private double sellPrice;
}
