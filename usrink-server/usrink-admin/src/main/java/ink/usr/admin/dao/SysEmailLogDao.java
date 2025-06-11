package ink.usr.admin.dao;

import ink.usr.admin.mapper.SysEmailLogMapper;
import ink.usr.framework.mysql.enums.Ds;
import ink.usr.common.model.mysql.SysEmailLogModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 邮件日志 DAO
 */
@Component
public class SysEmailLogDao {

    @Autowired
    private SysEmailLogMapper sysEmailLogMapper;

    /**
     * 插入邮件日志
     *
     * @param rw 数据源
     * @param sysEmailLogModel 邮件日志
     * @return 影响行数
     */
    public int insertSysEmailLog(Ds rw, SysEmailLogModel sysEmailLogModel) {
        return sysEmailLogMapper.insertSysEmailLog(sysEmailLogModel);
    }

    /**
     * 根据条件查询邮件日志列表
     *
     * @param rw 数据源
     * @param sysEmailLogModel 查询条件
     * @return 邮件日志列表
     */
    public List<SysEmailLogModel> selectSysEmailLogList(Ds rw, SysEmailLogModel sysEmailLogModel) {
        return sysEmailLogMapper.selectSysEmailLogList(sysEmailLogModel);
    }

    /**
     * 根据ID查询邮件日志
     *
     * @param rw 数据源
     * @param id 邮件日志ID
     * @return 邮件日志对象
     */
    public SysEmailLogModel selectSysEmailLogById(Ds rw, Long id) {
        return sysEmailLogMapper.selectSysEmailLogById(id);
    }

    /**
     * 根据邮箱地址查询邮件日志列表
     *
     * @param rw 数据源
     * @param toEmail 收件人邮箱
     * @return 邮件日志列表
     */
    public List<SysEmailLogModel> selectSysEmailLogByEmail(Ds rw, String toEmail) {
        return sysEmailLogMapper.selectSysEmailLogByEmail(toEmail);
    }
} 