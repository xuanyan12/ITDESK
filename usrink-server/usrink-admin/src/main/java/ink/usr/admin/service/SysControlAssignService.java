package ink.usr.admin.service;

import ink.usr.common.model.mysql.SysControlAssignModel;

import java.util.List;

public interface SysControlAssignService {
    List<SysControlAssignModel> getControlAssignList();
    
    /**
     * 获取控制分配列表(带查询条件)
     * @param queryModel 查询条件模型
     * @return 符合条件的分配记录列表
     */
    List<SysControlAssignModel> getControlAssignList(SysControlAssignModel queryModel);
}
