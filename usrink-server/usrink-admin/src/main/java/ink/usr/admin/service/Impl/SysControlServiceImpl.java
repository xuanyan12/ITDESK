package ink.usr.admin.service.Impl;

import ink.usr.admin.mapper.SysControlMapper;
import ink.usr.admin.service.SysControlService;
import ink.usr.common.model.mysql.SysControlModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysControlServiceImpl implements SysControlService {

    @Autowired
    private SysControlMapper sysControlMapper;

    public List<SysControlModel> selectSysControlList(SysControlModel sysControlModel) {
        List<SysControlModel> controlList = sysControlMapper.selectSysControlList(sysControlModel);
        return controlList;
    }

    public int selectCountNum(SysControlModel sysControlModel) {
        int num = sysControlMapper.selectCountNum(sysControlModel);
        return num;
    }

    public boolean updateSysControl(SysControlModel sysControlModel) {
        boolean flag = sysControlMapper.updateSysControl(sysControlModel);
        return flag;
    }

    public boolean deleteSysControl(long id) {
        boolean flag = sysControlMapper.deleteSysControl(id);
        return flag;
    }

    @Override
    public SysControlModel getInternalComputerByUserName(String userName) {
        SysControlModel sysControlModel = sysControlMapper.getInternalComputerByUserName(userName);
        return sysControlModel;
    }

    @Override
    public List<SysControlModel> getComputerListByUserName(String userName) {
        List<SysControlModel> sysControlModelList = sysControlMapper.getComputerListByUserName(userName);
        return sysControlModelList;
    }
}
