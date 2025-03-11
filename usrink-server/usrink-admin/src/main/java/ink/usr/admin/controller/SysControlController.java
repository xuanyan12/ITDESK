package ink.usr.admin.controller;

import ink.usr.admin.dao.DTO.SysControlDTO;
import ink.usr.admin.dao.VO.SysControlVO;
import ink.usr.admin.service.SysControlService;
import ink.usr.common.core.domain.Res;
import ink.usr.common.model.mysql.SysControlModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/sysControl")
public class SysControlController {

    @Autowired
    private SysControlService sysControlService;

    @PostMapping ("/selectSysControlList")
    @RequiresPermissions("sys:device:control:select")
    public Res selectSysControlList(@RequestBody SysControlDTO sysControlModel){
        // 设置limit的偏移量
        long pageNumSize = (sysControlModel.getPageNum() - 1) * sysControlModel.getPageSize();
        sysControlModel.setPageNum(pageNumSize);
        List<SysControlModel> controlList = sysControlService.selectSysControlList(sysControlModel);
        SysControlVO sysControlVO = new SysControlVO();
        sysControlVO.setSysControlModelLists(controlList);
        int countNum = sysControlService.selectCountNum(sysControlModel);
        sysControlVO.setTotal(countNum);
        sysControlVO.setPageNum(sysControlModel.getPageNum());
        sysControlVO.setPageSize(sysControlModel.getPageSize());
        return Res.success(sysControlVO);
    }

    @RequestMapping("/updateSysControl")
    @RequiresPermissions("sys:device:control:update")
    public Res updateSysControl(@RequestBody SysControlModel sysControlModel){
        boolean flag = sysControlService.updateSysControl(sysControlModel);
        return Res.success(flag);
    }

    @RequestMapping("/deleteSysControl")
    @RequiresPermissions("sys:device:control:delete")
    public Res deleteSysControl(long id){
        boolean flag = sysControlService.deleteSysControl(id);
        return Res.success(flag);
    }
}
