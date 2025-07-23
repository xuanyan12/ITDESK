package ink.usr.admin.controller;

import ink.usr.admin.dao.DTO.SysControlMaintenanceQueryDTO;
import ink.usr.admin.service.SysControlMaintenanceService;
import ink.usr.common.core.domain.Dict;
import ink.usr.common.core.domain.Res;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/sysControlMaintenance")
public class SysControlMaintenanceController {

    @Autowired
    private SysControlMaintenanceService sysControlMaintenanceService;

    @RequestMapping("/getControlMaintenanceList")
    public Res getControlMaintenanceList(@RequestBody SysControlMaintenanceQueryDTO queryDTO) {
        log.info("获取电脑维修记录列表 参数: {}", queryDTO.toString());

        try {
            Dict controlMaintenance = sysControlMaintenanceService.getSysControlMaintenance(queryDTO);
            return Res.success(controlMaintenance);
        }catch (Exception e) {
            log.error(e.getMessage());
            return Res.error("获取电脑维修记录失败" + e.getMessage());
        }
    }
}
