package ink.usr.admin.mapper;

import ink.usr.common.model.mysql.SysUserModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysUserMapper {

    /**
     * 用户登录，查询用户信息
     *
     * @param userName 用户名
     * @return 用户信息
     */
    SysUserModel selectSysUserForLogin(String userName);

    /**
     * 查询用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    SysUserModel selectSysUserById(Long userId);

    /**
     * 查询用户列表
     *
     * @param sysUserModel 用户信息
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
     * 逻辑删除用户
     *
     * @param userId 用户ID
     */
    int deleteSysUserLogicById(Long userId);

    /**
     * 恢复删除的用户
     *
     * @param userId 用户ID
     */
    int recoverSysUserById(Long userId);

    /**
     * 删除用户信息
     *
     * @param userId 用户ID
     */
    int deleteSysUserById(Long userId);

    @Select("select userName from sys_user where userId = #{userId}")
    String getNameByUserId(Long userId);

    @Select("select * from sys_user where userName like CONCAT('%', #{userName}, '%') LIMIT 1")
    SysUserModel getUserInfoByUserName(String userName);

    @Select("select userNick from sys_user where userId = #{userId}")
    String getUserNickNameByUserId(Long userId);

    @Select("select userId from sys_user where userName = #{responsibilityName}")
    Long getUserIdByUserName(String responsibilityName);

    @Update("update sys_user set userRoleId = 1 where userName = #{userName}")
    void updateUserRole2AdminByName(String userName);

    /**
     * 通过用户ID更新用户角色
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 更新的记录数
     */
    @Update("UPDATE sys_user SET userRoleId = #{roleId} WHERE userId = #{userId}")
    int updateUserRoleById(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * 通过用户名或用户昵称进行模糊查询
     * @param query 查询条件
     * @return 匹配的用户列表
     */
    @Select("SELECT * FROM sys_user WHERE userName LIKE CONCAT('%', #{query}, '%') OR REPLACE(userNick, ',', '') LIKE CONCAT('%', #{query}, '%') LIMIT 10")
    List<SysUserModel> searchUsersByNameOrNick(String query);

    /**
     * 获取所有非空且唯一的部门
     * @return 唯一部门列表
     */
    @Select("SELECT DISTINCT department FROM sys_user WHERE department IS NOT NULL AND department != '' ORDER BY department")
    List<String> getAllDistinctDepartments();

    /**
     * 获取所有非空且唯一的成本中心
     * @return 唯一成本中心列表
     */
    @Select("SELECT DISTINCT costCenter FROM sys_user WHERE costCenter IS NOT NULL AND costCenter != '' ORDER BY costCenter")
    List<String> getAllDistinctCostCenters();
}
