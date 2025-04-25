package ink.usr.common.model.mysql;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysLadpUserModel implements Serializable {

    // 姓名
    private String name;

    // 部门
    private String department;

    // 邮件
    private String mail;

    // 显示姓名
    private String displayName;

    // 成本中心
    private String description;

    // 电话
    private String telephoneNumber;

    // 标识性别 Mr. Mrs.
    private String title;

    // 标识mentor（fix-term才需要换成mentor，默认为Name，即用户自己）
    private String manager;

    // 标识地点（区分长沙与长春）
    private String l;

    // 标识账号的启停情况。 512：启用的普通账号（默认值）；544：启用，密码永不过期；514：账号被禁用；66050：被禁用 + 密码永不过期。
    private String userAccountControl;

    // 标识用户的创建时间
    private String whenCreated;
}
