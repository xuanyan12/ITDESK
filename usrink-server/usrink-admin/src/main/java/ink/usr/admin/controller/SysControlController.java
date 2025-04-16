package ink.usr.admin.controller;

import ink.usr.admin.dao.DTO.SysControlDTO;
import ink.usr.admin.dao.VO.SysControlVO;
import ink.usr.admin.service.SysControlService;
import ink.usr.common.core.domain.Dict;
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

    /**
     * 获取电脑设备列表
     * @param sysControlModel
     * @return
     */
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

    /**
     * 更新电脑
     * @param sysControlModel
     * @return
     */
    @RequestMapping("/updateSysControl")
    @RequiresPermissions("sys:device:control:update")
    public Res updateSysControl(@RequestBody SysControlModel sysControlModel){
        boolean flag = sysControlService.updateSysControl(sysControlModel);
        return Res.success(flag);
    }

    /**
     * 删除电脑
     * @param id
     * @return
     */
    @RequestMapping("/deleteSysControl")
    @RequiresPermissions("sys:device:control:delete")
    public Res deleteSysControl(long id){
        boolean flag = sysControlService.deleteSysControl(id);
        return Res.success(flag);
    }

    /**
     * 通过userName获取用户的 internal computer
     * @param userName
     * @return
     */
    @RequestMapping("/getInternalComputerByUserName")
    public Res getInternalComputerByUserName(@RequestParam("userName") String userName) throws Exception {
        SysControlModel sysControlModel = null;
        try {
            sysControlModel = sysControlService.getInternalComputerByUserName(userName);
        }catch (Exception e){
            throw new Exception(e);
        }

        return Res.success(sysControlModel);
    }

    /**
     * 通过userName获取用户的电脑名称列表
     * @param userName
     * @return
     */
    @RequestMapping("/getComputerListByUserName")
    public Res getComputerListByUserName(@RequestParam("userName") String userName) throws Exception {
        List<SysControlModel> sysControlModelList = null;
        Dict result = null;
        try {
            sysControlModelList = sysControlService.getComputerListByUserName(userName);
            result = Dict.create()
                    .set("list", sysControlModelList);
        }catch (Exception e){
            throw new Exception(e);
        }

        return Res.success(result);
    }

    /**
     * 通过电脑名获取电脑信息
     * @param ciName
     * @return
     */
    @RequestMapping("/getComputerInfoByCiName")
    public Res getComputerInfoByCiName(@RequestParam("ciName") String ciName){
        SysControlModel sysControlModel = sysControlService.getComputerInfoByCiName(ciName);
        return Res.success(sysControlModel);
    }
}
