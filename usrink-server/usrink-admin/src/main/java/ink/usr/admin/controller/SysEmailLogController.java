package ink.usr.admin.controller;

import com.github.pagehelper.Page;
import ink.usr.common.core.annotation.Log;
import ink.usr.common.core.domain.Dict;
import ink.usr.common.core.domain.Res;
import ink.usr.common.core.utils.PageUtil;
import ink.usr.common.interfaces.admin.ISysEmailLogService;
import ink.usr.common.model.mysql.SysEmailLogModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 邮件日志 控制器
 */
@Slf4j
@RestController
@RequestMapping(value = "/sysEmailLog")
public class SysEmailLogController {

    @Autowired
    private ISysEmailLogService sysEmailLogService;

    /**
     * 查询邮件日志列表
     *
     * @param sysEmailLogModel 查询条件
     * @return 结果
     */
    @Log("查询邮件日志列表")
    @RequestMapping(value = "/selectSysEmailLogList")
    @RequiresPermissions("sys:email:log:list")
    public Res selectSysEmailLogList(SysEmailLogModel sysEmailLogModel) {
        // 开启分页
        Page<Object> pages = PageUtil.startPage();
        List<SysEmailLogModel> sysEmailLogModels = sysEmailLogService.selectSysEmailLogList(sysEmailLogModel);

        Dict result = Dict.create()
                .set("list", sysEmailLogModels)
                .set("total", pages.getTotal());
        return Res.success(result);
    }

    /**
     * 根据ID查询邮件日志
     *
     * @param id 邮件日志ID
     * @return 结果
     */
    @Log("根据ID查询邮件日志")
    @RequestMapping(value = "/selectSysEmailLogById")
    @RequiresPermissions("sys:email:log:query")
    public Res selectSysEmailLogById(Long id) {
        SysEmailLogModel sysEmailLogModel = sysEmailLogService.selectSysEmailLogById(id);
        return Res.success(sysEmailLogModel);
    }

    /**
     * 根据邮箱地址查询邮件日志列表
     *
     * @param toEmail 收件人邮箱
     * @return 结果
     */
    @Log("根据邮箱地址查询邮件日志列表")
    @RequestMapping(value = "/selectSysEmailLogByEmail")
    @RequiresPermissions("sys:email:log:query")
    public Res selectSysEmailLogByEmail(String toEmail) {
        List<SysEmailLogModel> sysEmailLogModels = sysEmailLogService.selectSysEmailLogByEmail(toEmail);
        return Res.success(sysEmailLogModels);
    }
} 