package ink.usr.admin.service;


import ink.usr.admin.dao.DTO.SysControlMaintenanceQueryDTO;
import ink.usr.common.core.domain.Dict;
import ink.usr.common.model.mysql.SysControlMaintenanceModel;
import ink.usr.common.model.mysql.SysControlModel;

public interface SysControlMaintenanceService {

    /**
     *
     * 获取电脑的维修记录
     * @param sysControlMaintenanceQueryDTO 查询条件
     * @return 记录列表和分页信息
     */
    Dict getSysControlMaintenance(SysControlMaintenanceQueryDTO sysControlMaintenanceQueryDTO);


    /**
     *
     * 获取电脑维修记录的总数量
     * @param sysControlMaintenanceQueryDTO 查询条件
     * @return 维修记录的总数量
     */
    int selectCountNum(SysControlMaintenanceQueryDTO sysControlMaintenanceQueryDTO);
}
