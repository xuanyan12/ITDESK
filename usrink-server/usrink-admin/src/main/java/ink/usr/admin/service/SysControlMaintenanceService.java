package ink.usr.admin.service;


import ink.usr.admin.dao.DTO.SysControlMaintenanceQueryDTO;
import ink.usr.common.core.domain.Dict;

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
    
    /**
     * 保存维修记录
     * @param orderNumber 维修订单号
     * @param ciName 电脑名
     * @param applicant 申请人
     * @param maintenanceCategory 维修类别
     * @param problemDescription 故障描述
     * @param maintenanceResult 维修结果
     * @param maintenanceRecord 完整维修记录
     * @param maintenanceRemark 维修备注
     * @param maintenanceTime 维修时间
     * @return 是否保存成功
     */
    boolean saveMaintenanceRecord(String orderNumber, String ciName, String applicant, 
                                String maintenanceCategory, String problemDescription,
                                String maintenanceResult, String maintenanceRecord, 
                                String maintenanceRemark, String maintenanceTime);
}
