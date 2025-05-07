package ink.usr.admin.service;

import ink.usr.admin.dao.DTO.SysControlAssignDTO;
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
     * 插入新的电脑记录
     * @param sysControlModel 电脑信息
     * @return 插入是否成功
     */
    boolean insertSysControl(SysControlModel sysControlModel);

    List<SysControlModel> getComputerByInfo(SysControlAssignDTO sysControlAssignModel);
}
