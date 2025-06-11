package ink.usr.admin.service.Impl;

import ink.usr.admin.mapper.SysOnelinkSystemMapper;
import ink.usr.admin.service.SysOnelinkSystemService;
import ink.usr.common.model.mysql.SysOnelinkSystemModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * OneLink系统管理服务实现类
 */
@Service
public class SysOnelinkSystemServiceImpl implements SysOnelinkSystemService {

    @Autowired
    private SysOnelinkSystemMapper sysOnelinkSystemMapper;

    @Override
    public List<SysOnelinkSystemModel> getSystemList(String categoryName, Boolean isActive) {
        return sysOnelinkSystemMapper.selectSystemList(categoryName, isActive);
    }

    @Override
    public List<String> getCategories() {
        return sysOnelinkSystemMapper.selectCategories();
    }

    @Override
    @Transactional
    public void addSystem(SysOnelinkSystemModel system) {
        // 验证系统名称是否重复
        if (sysOnelinkSystemMapper.countBySystemName(system.getSystemName(), null) > 0) {
            throw new RuntimeException("系统名称已存在");
        }
        
        // 设置默认值
        if (system.getSortOrder() == null) {
            system.setSortOrder(0);
        }
        if (system.getIsActive() == null) {
            system.setIsActive(true);
        }
        // 如果没有提供图标，设置默认图标
        if (system.getSystemIcon() == null || system.getSystemIcon().trim().isEmpty()) {
            system.setSystemIcon("fas fa-desktop");
        }
        
        int result = sysOnelinkSystemMapper.insertSystem(system);
        if (result <= 0) {
            throw new RuntimeException("添加系统失败");
        }
    }

    @Override
    @Transactional
    public void updateSystem(SysOnelinkSystemModel system) {
        if (system.getSystemId() == null) {
            throw new RuntimeException("系统ID不能为空");
        }
        
        // 验证系统是否存在
        SysOnelinkSystemModel existingSystem = sysOnelinkSystemMapper.selectById(system.getSystemId());
        if (existingSystem == null) {
            throw new RuntimeException("系统不存在");
        }
        
        // 验证系统名称是否重复（排除自己）
        if (sysOnelinkSystemMapper.countBySystemName(system.getSystemName(), system.getSystemId()) > 0) {
            throw new RuntimeException("系统名称已存在");
        }
        
        // 如果没有提供图标，保持原有图标或设置默认图标
        if (system.getSystemIcon() == null || system.getSystemIcon().trim().isEmpty()) {
            if (existingSystem.getSystemIcon() != null && !existingSystem.getSystemIcon().trim().isEmpty()) {
                system.setSystemIcon(existingSystem.getSystemIcon());
            } else {
                system.setSystemIcon("fas fa-desktop");
            }
        }
        
        int result = sysOnelinkSystemMapper.updateSystem(system);
        if (result <= 0) {
            throw new RuntimeException("更新系统失败");
        }
    }

    @Override
    @Transactional
    public void deleteSystem(Long systemId) {
        if (systemId == null) {
            throw new RuntimeException("系统ID不能为空");
        }
        
        // 验证系统是否存在
        SysOnelinkSystemModel existingSystem = sysOnelinkSystemMapper.selectById(systemId);
        if (existingSystem == null) {
            throw new RuntimeException("系统不存在");
        }
        
        int result = sysOnelinkSystemMapper.deleteSystem(systemId);
        if (result <= 0) {
            throw new RuntimeException("删除系统失败");
        }
    }

    @Override
    public SysOnelinkSystemModel getSystemById(Long systemId) {
        if (systemId == null) {
            throw new RuntimeException("系统ID不能为空");
        }
        
        SysOnelinkSystemModel system = sysOnelinkSystemMapper.selectById(systemId);
        if (system == null) {
            throw new RuntimeException("系统不存在");
        }
        
        return system;
    }

    @Override
    public List<SysOnelinkSystemModel> getActiveSystems() {
        return sysOnelinkSystemMapper.selectActiveSystems();
    }
} 