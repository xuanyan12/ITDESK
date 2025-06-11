package ink.usr.admin.mapper;

import ink.usr.common.model.mysql.SysEmailLogModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 邮件日志 Mapper 接口
 */
@Mapper
public interface SysEmailLogMapper {

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
    SysEmailLogModel selectSysEmailLogById(@Param("id") Long id);

    /**
     * 根据邮箱地址查询邮件日志列表
     *
     * @param toEmail 收件人邮箱
     * @return 邮件日志列表
     */
    List<SysEmailLogModel> selectSysEmailLogByEmail(@Param("toEmail") String toEmail);
} 