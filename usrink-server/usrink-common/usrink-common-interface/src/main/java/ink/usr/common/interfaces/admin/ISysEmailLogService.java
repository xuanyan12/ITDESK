package ink.usr.common.interfaces.admin;

import ink.usr.common.model.mysql.SysEmailLogModel;

import java.util.List;

/**
 * 邮件日志 Service 接口
 */
public interface ISysEmailLogService {

    /**
     * 插入邮件日志
     *
     * @param sysEmailLogModel 邮件日志
     * @return 影响行数
     */
    int insertSysEmailLog(SysEmailLogModel sysEmailLogModel);

    /**
     * 根据条件查询邮件日志列表
     *
     * @param sysEmailLogModel 查询条件
     * @return 邮件日志列表
     */
    List<SysEmailLogModel> selectSysEmailLogList(SysEmailLogModel sysEmailLogModel);

    /**
     * 根据ID查询邮件日志
     *
     * @param id 邮件日志ID
     * @return 邮件日志对象
     */
    SysEmailLogModel selectSysEmailLogById(Long id);

    /**
     * 根据邮箱地址查询邮件日志列表
     *
     * @param toEmail 收件人邮箱
     * @return 邮件日志列表
     */
    List<SysEmailLogModel> selectSysEmailLogByEmail(String toEmail);
} 