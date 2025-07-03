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

    // 使用人（从用户表获取的userNick或拼接的姓名）
    private String userName;

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

    // cpu
    private String cpu;

    // 内存
    private String memory;

    // 硬盘
    private String disk;

    // 显卡
    private String graphic;

    // 硬件状态
    private String hardwareStatus;

    // 下单号
    private String pr;

    // 订单号
    private String po;

    // 供应商公司
    private String vendor;

    // SGCS/SES/SGCC
    private String company;

    // 资产号
    private String wbsNum;

    // 临时分配标识，0代表非临时分配，1代表临时分配
    private int temp;

    // 价格
    private String price;
}
