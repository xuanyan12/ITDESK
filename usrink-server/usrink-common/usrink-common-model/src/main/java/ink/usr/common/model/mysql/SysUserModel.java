package ink.usr.common.model.mysql;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysUserModel implements Serializable {

    /**
     * 系统用户ID，主键
     */
    private Long userId;

    /**
     * 系统用户名，唯一
     */
    private String userName;

    /**
     * 系统用户密码
     */
    private String userPassword;

    /**
     * 角色ID
     */
    private Long userRoleId;

    /**
     * 用户昵称
     */
    private String userNick;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户手机号
     */
    private String phone;

    /**
     * 性别，0为女，1为男，-1为未知，默认-1
     */
    public Integer sex = -1;

    /**
     * 创建时间
     */
    public String createTime;

    /**
     * 更新时间
     */
    public String updateTime;

    /**
     * 用户状态, 0:正常, -1:禁用
     */
    private Integer status = 0;


    /**
     * 用户所属部门
     */
    private String department;

    /**
     * 责任人（正式员工为自己，实习生为mentor）
     */
    private String responsibility;

    /**
     * 所属公司
     */
    private String company;

    /**
     * 成本中心
     */
    private String costCenter;
}
