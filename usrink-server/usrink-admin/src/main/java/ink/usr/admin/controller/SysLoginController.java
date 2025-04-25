package ink.usr.admin.controller;

import ink.usr.admin.dao.DTO.SysAutoLoginUserDto;
import ink.usr.admin.service.ShiroService;
import ink.usr.admin.service.SysLadpService;
import ink.usr.common.core.annotation.Log;
import ink.usr.common.core.domain.Dict;
import ink.usr.common.core.domain.Res;
import ink.usr.common.domain.admin.SysUserMenus;
import ink.usr.common.interfaces.admin.ISysLoginService;
import ink.usr.common.interfaces.admin.ISysRoleService;
import ink.usr.common.interfaces.admin.ISysUserService;
import ink.usr.common.model.mysql.SysLadpUserModel;
import ink.usr.common.model.mysql.SysRoleModel;
import ink.usr.common.model.mysql.SysUserModel;
import ink.usr.framework.shiro.domain.ShiroRoleInfo;
import ink.usr.framework.shiro.domain.ShiroUserInfo;
import ink.usr.framework.shiro.utils.JwtUtil;
import ink.usr.framework.shiro.utils.ShiroUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@CrossOrigin
public class SysLoginController {

    @Autowired
    private ISysLoginService sysLoginService;
    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private ISysRoleService sysRoleService;
    @Autowired
    private ShiroService shiroService;
    @Autowired
    private SysLadpService sysLadpService;


    /**
     * 登录接口<br>
     *
     * @param userName 用户名
     * @param password 密码
     */
    @RequestMapping(value = "/login")
    public Res login(String userName, String password) {
        // 先尝试使用LDAP认证
        SysLadpUserModel ladpUserModel = null;
        SysUserModel sysUserModel = null;
        
        try {
            ladpUserModel = sysLadpService.authenticate(userName, password);
        } catch (Exception e) {
            // 捕获LDAP认证过程中的异常，不做处理，继续使用Shiro认证
        }
        
        if (ladpUserModel != null) {
            // LDAP认证成功，查找对应的系统用户
            sysUserModel = sysUserService.getUserInfoByUserName(ladpUserModel.getName());
            
            if (sysUserModel == null) {
                return Res.error("用户在系统中不存在，请联系管理员");
            }
            
            // 已通过LDAP验证，不再使用Shiro验证密码
            // 获取用户的Shiro信息，仅用于后续操作
            ShiroUserInfo shiroUserInfo = shiroService.selectSysUserForLogin(sysUserModel.getUserName());
            
            if (shiroUserInfo == null) {
                return Res.error("用户认证信息获取失败");
            }
            
            // 不返回密码
            sysUserModel.setUserPassword(null);
            
            // 查询用户角色
            SysRoleModel sysRoleModel = sysRoleService.selectRoleInfo(sysUserModel.getUserRoleId());
            
            // 查询用户拥有的菜单
            SysUserMenus userMenus = sysLoginService.selectUserMenuList(sysRoleModel.getRoleMenuIds());
            
            // 生成Token令牌
            String tokenString = JwtUtil.createToken(
                    shiroUserInfo.getUserName(),
                    shiroUserInfo.getUserId(),
                    shiroUserInfo.getUserPassword());
            
            // 返回登录结果
            Dict result = Dict.create()
                    .set("token", tokenString)
                    .set("userInfo", sysUserModel)
                    .set("roleInfo", sysRoleModel)
                    .set("userMenus", userMenus);
            
            // 保存用户角色缓存
            shiroService.updateUserRoleCache(shiroUserInfo.getUserId(), sysRoleModel);
            
            return Res.success(result);
        } else {
            // LDAP认证失败，继续使用原有的Shiro认证
            try {
                // 创建一个用户名密码令牌
                UsernamePasswordToken token = new UsernamePasswordToken(userName, password);
                
                // Shiro进行身份认证
                // 用户名不存在将抛出Shiro的UnknownAccountException异常
                // 密码错误将抛出Shiro的IncorrectCredentialsException异常
                SecurityUtils.getSubject().login(token);
                
                // 用户认证凭证信息
                ShiroUserInfo shiroUserInfo = ShiroUtil.getShiroUserInfo();
                
                // 查询用户资料信息
                sysUserModel = sysUserService.selectSysUserInfo(shiroUserInfo.getUserId());
                // 不返回密码
                sysUserModel.setUserPassword(null);
                
                // 查询用户角色
                SysRoleModel sysRoleModel = sysRoleService.selectRoleInfo(sysUserModel.getUserRoleId());
                
                // 查询用户拥有的菜单
                SysUserMenus userMenus = sysLoginService.selectUserMenuList(sysRoleModel.getRoleMenuIds());
                
                // 生成Token令牌
                String tokenString = JwtUtil.createToken(
                        shiroUserInfo.getUserName(),
                        shiroUserInfo.getUserId(),
                        shiroUserInfo.getUserPassword());
                
                // 返回登录结果
                Dict result = Dict.create()
                        .set("token", tokenString)
                        .set("userInfo", sysUserModel)
                        .set("roleInfo", sysRoleModel)
                        .set("userMenus", userMenus);
                
                // 保存用户角色缓存
                shiroService.updateUserRoleCache(shiroUserInfo.getUserId(), sysRoleModel);
                
                return Res.success(result);
            } catch (Exception e) {
                return Res.error("用户名或密码错误");
            }
        }
    }


    /**
     * 查询用户菜单列表
     */
    @Log("查询用户的菜单列表")
    @RequestMapping(value = "/userMenu")
    public Res selectUserMenuList() {
        ShiroUserInfo shiroUserInfo = ShiroUtil.getShiroUserInfo();
        ShiroRoleInfo shiroRoleInfo = shiroService.getRoleByUserId(shiroUserInfo.getUserId());
        if (shiroRoleInfo == null) {
            return Res.error("用户角色信息不存在");
        }

        // 查询用户拥有的菜单
        SysUserMenus userMenus = sysLoginService.selectUserMenuList(shiroRoleInfo.getRoleMenuIds());
        return Res.success(userMenus);
    }

}
