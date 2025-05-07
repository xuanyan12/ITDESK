package ink.usr.admin.service.Impl;

import ink.usr.admin.dao.DTO.SysControlAssignDTO;
import ink.usr.admin.mapper.SysControlMapper;
import ink.usr.admin.service.SysControlService;
import ink.usr.common.model.mysql.SysControlAssignModel;
import ink.usr.common.model.mysql.SysControlModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    public List getComputerListByUserName(String userName) {
        List<SysControlModel> sysControlModelList = sysControlMapper.getComputerListByUserName(userName);
        List controlNameList = new ArrayList<>();
        String controlName = null;
        for(SysControlModel model : sysControlModelList){
            controlName = model.getCiName();
            controlNameList.add(controlName);
        }
        return controlNameList;
    }

    @Override
    public SysControlModel getComputerInfoByCiName(String ciName) {
        SysControlModel sysControlModel = sysControlMapper.getComputerInfoByCiName(ciName);
        return sysControlModel;
    }

    @Override
    public List<SysControlModel> selectNonScrappedComputersByCiName(String ciName) {
        return sysControlMapper.selectNonScrappedComputersByCiName(ciName);
    }

    @Override
    public boolean insertSysControl(SysControlModel sysControlModel) {
        return sysControlMapper.insertSysControl(sysControlModel);
    }

    @Override
    public List<SysControlModel> getComputerByInfo(SysControlAssignDTO sysControlAssignModel) {
        List<SysControlModel> sysControlModel = sysControlMapper.getComputerByInfo(sysControlAssignModel);
        return sysControlModel;
    }
}
