package ink.usr.admin.controller;

import com.github.pagehelper.Page;
import ink.usr.admin.service.SysOnelinkSystemService;
import ink.usr.common.core.domain.Dict;
import ink.usr.common.core.domain.Res;
import ink.usr.common.core.utils.PageUtil;
import ink.usr.common.model.mysql.SysOnelinkSystemModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * OneLink系统管理控制器
 */
@RestController
@Slf4j
@RequestMapping("/sysOnelinkSystem")
public class SysOnelinkSystemController {

    @Autowired
    private SysOnelinkSystemService sysOnelinkSystemService;

    /**
     * 获取系统列表
     */
    @RequestMapping("/list")
    public Res getSystemList(
            @RequestParam(value = "page", defaultValue = "1") int pageNum,
            @RequestParam(value = "size", defaultValue = "10") int pageSize,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) Boolean isActive) {
        try {
            // 设置分页参数
            PageUtil.startPage(pageNum, pageSize);
            List<SysOnelinkSystemModel> systemList = sysOnelinkSystemService.getSystemList(categoryName, isActive);
            
            // 获取分页信息
            Page<SysOnelinkSystemModel> pageInfo = (Page<SysOnelinkSystemModel>) systemList;
            
            Dict result = Dict.create()
                    .set("list", systemList)
                    .set("total", pageInfo.getTotal());
            return Res.success(result);
        } catch (Exception e) {
            log.error("获取系统列表失败", e);
            return Res.error("获取系统列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取分类列表
     */
    @RequestMapping("/categories")
    public Res getCategories() {
        try {
            List<String> categories = sysOnelinkSystemService.getCategories();
            Dict result = Dict.create().set("list", categories);
            return Res.success(result);
        } catch (Exception e) {
            log.error("获取分类列表失败", e);
            return Res.error("获取分类列表失败: " + e.getMessage());
        }
    }

    /**
     * 添加系统
     */
    @RequestMapping("/add")
    public Res addSystem(@RequestBody SysOnelinkSystemModel system) {
        try {
            sysOnelinkSystemService.addSystem(system);
            return Res.success("添加成功");
        } catch (Exception e) {
            log.error("添加系统失败", e);
            return Res.error("添加系统失败: " + e.getMessage());
        }
    }

    /**
     * 更新系统
     */
    @RequestMapping("/update")
    public Res updateSystem(@RequestBody SysOnelinkSystemModel system) {
        try {
            sysOnelinkSystemService.updateSystem(system);
            return Res.success("更新成功");
        } catch (Exception e) {
            log.error("更新系统失败", e);
            return Res.error("更新系统失败: " + e.getMessage());
        }
    }

    /**
     * 删除系统
     */
    @RequestMapping("/delete")
    public Res deleteSystem(@RequestParam("systemId") Long systemId) {
        try {
            sysOnelinkSystemService.deleteSystem(systemId);
            return Res.success("删除成功");
        } catch (Exception e) {
            log.error("删除系统失败", e);
            return Res.error("删除系统失败: " + e.getMessage());
        }
    }

    /**
     * 获取系统详情
     */
    @RequestMapping("/detail")
    public Res getSystemDetail(@RequestParam("systemId") Long systemId) {
        try {
            SysOnelinkSystemModel system = sysOnelinkSystemService.getSystemById(systemId);
            return Res.success(system);
        } catch (Exception e) {
            log.error("获取系统详情失败", e);
            return Res.error("获取系统详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取启用的系统列表（供OneLink页面使用）
     */
    @RequestMapping("/active")
    public Res getActiveSystems() {
        try {
            List<SysOnelinkSystemModel> systems = sysOnelinkSystemService.getActiveSystems();
            Dict result = Dict.create().set("list", systems);
            return Res.success(result);
        } catch (Exception e) {
            log.error("获取启用系统列表失败", e);
            return Res.error("获取启用系统列表失败: " + e.getMessage());
        }
    }
} 