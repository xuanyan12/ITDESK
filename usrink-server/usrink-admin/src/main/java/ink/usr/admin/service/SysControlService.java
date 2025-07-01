package ink.usr.admin.service;

import ink.usr.admin.dao.DTO.SysAllocateDeviceDTO;
import ink.usr.admin.dao.DTO.SysControlAssignDTO;
import ink.usr.admin.dao.DTO.SysControlRecordQueryDTO;
import ink.usr.admin.dao.VO.SysControlBillListVO;
import ink.usr.common.core.domain.Dict;
import ink.usr.common.model.mysql.SysControlAssignModel;
import ink.usr.common.model.mysql.SysControlModel;

import java.util.List;

public interface SysControlService {
    List<SysControlModel> selectSysControlList(SysControlModel sysControlModel);

    int selectCountNum(SysControlModel sysControlModel);

    boolean updateSysControl(SysControlModel sysControlModel);

    boolean deleteSysControl(long id);

    SysControlModel getInternalComputerByUserName(String userName);

    List getComputerListByUserName(String userName);

    SysControlModel getComputerInfoByCiName(String ciName);
    
    /**
     * 查询指定ciName的电脑，但排除pcStatus为Scrapped的记录
     * @param ciName 电脑名称
     * @return 非报废状态的电脑列表
     */
    List<SysControlModel> selectNonScrappedComputersByCiName(String ciName);
    
    /**
     * 根据成本中心获取设备列表
     * @param costCenter 成本中心
     * @return 设备列表
     */
    List<SysControlModel> getComputerListByCostCenter(String costCenter);
    
    /**
     * 获取审批人管理的所有设备列表（基于其负责的成本中心）
     * @param userName 用户名
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param costCenterFilter 成本中心筛选条件（可选）
     * @return 包含设备信息和成本中心标识的字典
     */
    Dict getApproverManagedComputers(String userName, int pageNum, int pageSize, String costCenterFilter);
    
    /**
     * 插入新的电脑记录
     * @param sysControlModel 电脑信息
     * @return 插入是否成功
     */
    boolean insertSysControl(SysControlModel sysControlModel);

    List<SysControlModel> getComputerByInfo(SysControlAssignDTO sysControlAssignModel);

    boolean allocateDevice(SysAllocateDeviceDTO sysAllocateDeviceDTO);
    
    /**
     * 更新电脑归属情况
     * @param ciName 电脑名
     * @param pcClass 电脑归属情况
     * @return 是否成功
     */
    boolean updateComputerOwnership(String ciName, String pcClass);
    
    /**
     * 更新电脑状态和归属情况
     * @param ciName 电脑名
     * @param pcStatus 电脑状态
     * @param pcClass 电脑归属情况
     * @return 是否成功
     */
    boolean updateComputerStatusAndOwnership(String ciName, String pcStatus, String pcClass);
    
    /**
     * 获取电脑修改记录列表
     * @param queryDTO 查询条件
     * @return 记录列表和分页信息
     */
    Dict getControlRecordList(SysControlRecordQueryDTO queryDTO);
}


