package ink.usr.admin.controller;

import ink.usr.admin.dao.DTO.SysAutoLoginUserDto;
import ink.usr.admin.service.SysLadpService;
import ink.usr.common.core.domain.Res;
import ink.usr.common.model.mysql.SysLadpUserModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/sysLadp")
public class SysLadpController {

    @Autowired
    private SysLadpService sysLadpService;

    /**
     * 身份验证（用于AD域登录）
     * @param sysAutoLoginUserDto
     * @return
     */
    @RequestMapping("/authenticate")
    public Res authenticate(@RequestBody SysAutoLoginUserDto sysAutoLoginUserDto){
        SysLadpUserModel sysLadpUserModel = sysLadpService.authenticate(sysAutoLoginUserDto.getName(), sysAutoLoginUserDto.getPassword());
        return Res.success(sysLadpUserModel);
    }

    @GetMapping("/linkLDAPRefreshAllInfo")
    public Res linkLDAPRefreshAllInfo() {
        return Res.success( sysLadpService.linkLDAPRefreshAllInfo() );
    }
}
