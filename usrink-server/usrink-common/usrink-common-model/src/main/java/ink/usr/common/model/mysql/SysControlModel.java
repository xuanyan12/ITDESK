package ink.usr.common.model.mysql;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysControlModel implements Serializable {

    // 电脑id
    private long id;

    // 电脑使用情况
    private String pcStatus;

    // 电脑名
    private String ciName;

    // 电脑序列号
    private String serialNumber;

    // 电脑类型
    private String deviceClass;

    // 制造商
    private String manufacture;

    // 电脑型号
    private String modelOrVersion;

    // NT账号
    private String ntAccount;

    // 电脑归属情况
    private String pcClass;

    // 备注
    private String comment;

    // 名
    private String firstName;

    // 姓
    private String lastName;

    // 邮箱地址
    private String emailAddress;

    // 电话号码
    private String telephone;

    // 所属部门
    private String department;

    // 成本中心
    private String costCenter;

    // 出厂时间
    private String lifeCycleStart;

    // 出厂日期到今天的时间
    private String yrsToDay;
}
