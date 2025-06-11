package ink.usr.admin.service;

import ink.usr.admin.dao.SysEmailLogDao;
import ink.usr.framework.mysql.enums.Ds;
import ink.usr.common.interfaces.admin.ISysEmailLogService;
import ink.usr.common.model.mysql.SysEmailLogModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 邮件日志 Service 实现类
 */
@Service
public class SysEmailLogService implements ISysEmailLogService {

    @Autowired
    private SysEmailLogDao sysEmailLogDao;

    /**
     * 插入邮件日志
     *
     * @param sysEmailLogModel 邮件日志
     * @return 影响行数
     */
    @Override
    public int insertSysEmailLog(SysEmailLogModel sysEmailLogModel) {
        return sysEmailLogDao.insertSysEmailLog(Ds.W, sysEmailLogModel);
    }

    /**
     * 根据条件查询邮件日志列表
     *
     * @param sysEmailLogModel 查询条件
     * @return 邮件日志列表
     */
    @Override
    public List<SysEmailLogModel> selectSysEmailLogList(SysEmailLogModel sysEmailLogModel) {
        return sysEmailLogDao.selectSysEmailLogList(Ds.R, sysEmailLogModel);
    }

    /**
     * 根据ID查询邮件日志
     *
     * @param id 邮件日志ID
     * @return 邮件日志对象
     */
    @Override
    public SysEmailLogModel selectSysEmailLogById(Long id) {
        return sysEmailLogDao.selectSysEmailLogById(Ds.R, id);
    }

    /**
     * 根据邮箱地址查询邮件日志列表
     *
     * @param toEmail 收件人邮箱
     * @return 邮件日志列表
     */
    @Override
    public List<SysEmailLogModel> selectSysEmailLogByEmail(String toEmail) {
        return sysEmailLogDao.selectSysEmailLogByEmail(Ds.R, toEmail);
    }
} 