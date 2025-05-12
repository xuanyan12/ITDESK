package ink.usr.admin.service.Impl;

import ink.usr.admin.dao.DTO.SysAllocateDeviceDTO;
import ink.usr.admin.mapper.SysControlAssignMapper;
import ink.usr.admin.mapper.SysControlMapper;
import ink.usr.admin.mapper.SysUserMapper;
import ink.usr.admin.service.SysControlAssignService;
import ink.usr.common.model.mysql.SysControlAssignModel;
import ink.usr.common.model.mysql.SysControlModel;
import ink.usr.framework.shiro.domain.ShiroUserInfo;
import ink.usr.framework.shiro.utils.ShiroUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SysControlAssignServiceImpl implements SysControlAssignService {
    @Autowired
    private SysControlAssignMapper sysControlAssignMapper;
    @Autowired
    private SysControlMapper sysControlMapper;
    @Autowired
    private SysUserMapper sysUserMapper;

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

    @Override
    public boolean allocateDevice(SysAllocateDeviceDTO sysAllocateDeviceDTO) {

        // 通过approvalId找到订单
        SysControlAssignModel controlAssign = sysControlAssignMapper.getControlAssign(sysAllocateDeviceDTO.getApprovalId());
        if(controlAssign!=null){
            // 2.订单表sys_control_assign进行更改
            if(sysAllocateDeviceDTO.getIsTemp()!=null){
                // 如果isTemp为1，将temp置为1，assignStatus置为暂分配；否则置为分配完成。
                if(sysAllocateDeviceDTO.getIsTemp() == 1){
                    controlAssign.setAssignStatus("暂分配");
                } else{
                    controlAssign.setAssignStatus("分配完成");
                }
            }
            // 填入电脑名，上一个使用者的nt账号（如果有）
            if(sysAllocateDeviceDTO.getCiName()!=null){
                controlAssign.setCiName(sysAllocateDeviceDTO.getCiName());
                SysControlModel computerInfoByCiName = sysControlMapper.getComputerInfoByCiName(sysAllocateDeviceDTO.getCiName());
                if(computerInfoByCiName!=null){
                    String ntAccount = computerInfoByCiName.getNtAccount();
                    // 直接设置ntAccount，无论是否为null
                    controlAssign.setLastLeastUserNtAccount(ntAccount);
                }
            }
            // 通过shiro找到当前分配人的id后找到nt账号并赋值
            ShiroUserInfo shiroUserInfo = ShiroUtil.getShiroUserInfo();
            Long userId = shiroUserInfo.getUserId();
            String nameByUserId = sysUserMapper.getNameByUserId(userId);
            if(nameByUserId!=null){
                controlAssign.setAssignor(nameByUserId);
            }
            // 分配时间就是当前
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime time = LocalDateTime.now();
            String localTime = df.format(time);
            controlAssign.setAssignTime(localTime);
            boolean flag = sysControlAssignMapper.updateControlAssign(controlAssign);
            return flag;
        }

        return false;
    }
}
