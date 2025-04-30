package ink.usr.admin.controller;

import ink.usr.admin.dao.DTO.SysAutoLoginUserDto;
import ink.usr.admin.service.SysLadpService;
import ink.usr.common.core.domain.Res;
import ink.usr.common.model.mysql.SysLadpUserModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ink.usr.common.core.domain.Dict;

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

    /**
     * 将ad的数据更新到数据库中
     * @return
     */
    @RequestMapping("/linkLDAPRefreshAllInfo")
    public Res linkLDAPRefreshAllInfo() {
        String instructResult = sysLadpService.linkLDAPRefreshAllInfo();
        return Res.success(instructResult);
    }

    /**
     * 通过导入的excel更新approver表
     * @return
     */
    @RequestMapping("/updateApprover")
    public Res updateApprover(@RequestParam("file") MultipartFile file){
        boolean flag = sysLadpService.updateApprover(file);

        return Res.success(flag);
    }
}
