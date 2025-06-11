package ink.usr.admin.service;

import ink.usr.common.model.mysql.SysOnelinkSystemModel;

import java.util.List;

/**
 * OneLink系统管理服务接口
 */
public interface SysOnelinkSystemService {

    /**
     * 获取系统列表
     */
    List<SysOnelinkSystemModel> getSystemList(String categoryName, Boolean isActive);

    /**
     * 获取分类列表
     */
    List<String> getCategories();

    /**
     * 添加系统
     */
    void addSystem(SysOnelinkSystemModel system);

    /**
     * 更新系统
     */
    void updateSystem(SysOnelinkSystemModel system);

    /**
     * 删除系统
     */
    void deleteSystem(Long systemId);

    /**
     * 根据ID获取系统
     */
    SysOnelinkSystemModel getSystemById(Long systemId);

    /**
     * 获取启用的系统列表
     */
    List<SysOnelinkSystemModel> getActiveSystems();
} 