package ink.usr.common.interfaces.admin;

import ink.usr.common.domain.admin.UpdatePwdForm;
import ink.usr.common.model.mysql.SysUserModel;

import java.util.List;

public interface ISysUserService {

    /**
     * 查询系统用户资料
     */
    SysUserModel selectSysUserInfo(Long userId);

    /**
     * 查询用户列表
     *
     * @param sysUserModel 用户信息
     * @return 用户列表
     */
    List<SysUserModel> selectSysUserList(SysUserModel sysUserModel);

    /**
     * 添加用户信息
     *
     * @param sysUserModel 用户信息
     */
    int insertSysUser(SysUserModel sysUserModel);

    /**
     * 检查用户名是否存在
     */
    SysUserModel checkUserNameUnique(String userName);

    /**
     * 修改用户信息
     *
     * @param sysUserModel 用户信息
     */
    int updateSysUser(SysUserModel sysUserModel);

    /**
     * 修改用户角色
     */
    int updateSysUserRole(SysUserModel sysUserModel);

    /**
     * 更新用户密码
     */
    int updateSysUserPassword(SysUserModel sysUserModel);

    /**
     * 逻辑删除用户信息
     *
     * @param userId 用户ID
     */
    int deleteSysUserLogic(Long userId);

    /**
     * 恢复删除的用户
     */
    int recoverSysUser(Long userId);

    /**
     * 物理删除用户
     */
    int deleteSysUserPhysics(Long userId);

    /**
     * 修改个人密码
     */
    boolean updateMyPwd(Long userId, UpdatePwdForm updatePwdForm);

    /**
     * 根据userId查找用户名
     * @param userId
     * @return
     */
    String getNameByUserId(Long userId);

    /**
     * 通过userName获取用户信息
     * @param userName
     * @return
     */
    SysUserModel getUserInfoByUserName(String userName);

    /**
     * 通过userId获取用户的名称（nickName）
     * @param userId
     * @return
     */
    String getUserNickNameByUserId(Long userId);

    /**
     * 通过userName获取userId
     * @param responsibilityName
     * @return
     */
    Long getUserIdByUserName(String responsibilityName);

    /**
     * 根据用户名或用户昵称进行模糊查询
     * @param query 查询条件（可以是姓名或NT账号的部分）
     * @return 匹配的用户列表
     */
    List<SysUserModel> searchUsersByNameOrNick(String query);

}
