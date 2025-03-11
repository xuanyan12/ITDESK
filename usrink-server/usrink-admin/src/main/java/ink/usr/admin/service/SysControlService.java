package ink.usr.admin.service;

import ink.usr.common.model.mysql.SysControlModel;

import java.util.List;

public interface SysControlService {
    List<SysControlModel> selectSysControlList(SysControlModel sysControlModel);

    int selectCountNum(SysControlModel sysControlModel);

    boolean updateSysControl(SysControlModel sysControlModel);

    boolean deleteSysControl(long id);
}
