package ink.usr.admin.service.Impl;

import ink.usr.admin.mapper.SysControlAssignMapper;
import ink.usr.admin.service.SysControlAssignService;
import ink.usr.common.model.mysql.SysControlAssignModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysControlAssignServiceImpl implements SysControlAssignService {
    @Autowired
    private SysControlAssignMapper sysControlAssignMapper;

    @Override
    public List<SysControlAssignModel> getControlAssignList() {
        List<SysControlAssignModel> sysControlAssignModel = sysControlAssignMapper.getControlAssignList();
        return sysControlAssignModel;
    }
    
    @Override
    public List<SysControlAssignModel> getControlAssignList(SysControlAssignModel queryModel) {
        if (queryModel == null) {
            // 如果查询模型为空，返回所有记录
            return sysControlAssignMapper.getControlAssignList();
        } else {
            // 否则根据条件进行查询
            return sysControlAssignMapper.getControlAssignListWithConditions(queryModel);
        }
    }
}
