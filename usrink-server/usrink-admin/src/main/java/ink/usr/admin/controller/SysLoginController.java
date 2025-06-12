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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@Tag(name = "系统登录接口", description = "用户登录认证相关接口")
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
    @Autowired
    private ink.usr.admin.service.ITempPasswordService tempPasswordService;


    /**
     * 登录接口<br>
     *
     * @param userName 用户名
     * @param password 密码
     */
    @Operation(summary = "用户登录", description = "用户登录认证接口，支持LDAP和本地认证")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "登录成功"),
        @ApiResponse(responseCode = "400", description = "用户名或密码错误"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @RequestMapping(value = "/login")
    public Res login(
        @Parameter(description = "用户名", required = true) String userName, 
        @Parameter(description = "密码", required = true) String password) {
        // 先尝试使用LDAP认证
        SysLadpUserModel ladpUserModel = null;
        SysUserModel sysUserModel = null;
        
        try {
            ladpUserModel = sysLadpService.authenticate(userName, password);
        } catch (Exception e) {
            // 检查是否是预定义的错误消息
            String errorMsg = e.getMessage();
            if (errorMsg != null && (
                errorMsg.equals("用户不在域中，请联系IT处理") || 
                errorMsg.equals("AD域连接异常，请检查账号密码或联系IT"))) {
                return Res.error(errorMsg);
            }
            // 其他异常继续使用Shiro认证
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
                // Shiro认证失败，尝试临时密码验证
                if (tempPasswordService.validateTempPassword(userName, password)) {
                    // 临时密码验证成功，获取用户信息并登录
                    sysUserModel = sysUserService.getUserInfoByUserName(userName);
                    if (sysUserModel == null) {
                        return Res.error("用户不存在，请联系IT进行处理");
                    }
                    
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
                    return Res.error("用户名或密码错误");
                }
            }
        }
    }


    /**
     * 查询用户菜单列表
     */
    @Operation(summary = "查询用户菜单", description = "获取当前登录用户的菜单权限列表")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "401", description = "未授权访问"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @SecurityRequirement(name = "Bearer")
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
    
    /**
     * 为指定用户生成备用密码
     * 仅供管理员使用
     */
    @Operation(summary = "生成备用密码", description = "为指定用户生成备用密码，仅管理员可用")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "生成成功"),
        @ApiResponse(responseCode = "403", description = "权限不足"),
        @ApiResponse(responseCode = "400", description = "用户不存在"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @SecurityRequirement(name = "Bearer")
    @Log("生成用户备用密码")
    @RequestMapping(value = "/generateBackupPassword")
    public Res generateBackupPassword(
        @Parameter(description = "用户名", required = true) String userName) {
        try {
            // 验证当前用户是否为管理员
            ShiroUserInfo shiroUserInfo = ShiroUtil.getShiroUserInfo();
            ShiroRoleInfo shiroRoleInfo = shiroService.getRoleByUserId(shiroUserInfo.getUserId());
            
            if (shiroRoleInfo == null || shiroRoleInfo.getRoleId() != 1) {
                return Res.error("权限不足，仅管理员可以生成备用密码");
            }
            
            // 生成备用密码
            String backupPassword = sysLadpService.getUserBackupPassword(userName);
            
            if (backupPassword == null) {
                return Res.error("用户不存在或生成备用密码失败");
            }
            
            Dict result = Dict.create()
                    .set("userName", userName)
                    .set("backupPassword", backupPassword)
                    .set("message", "备用密码已生成，请妥善保管");
            
            return Res.success(result);
        } catch (Exception e) {
            return Res.error("生成备用密码失败: " + e.getMessage());
        }
    }

}
