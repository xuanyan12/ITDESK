package ink.usr.admin.service.Impl;

import ink.usr.admin.dao.DTO.SysAllocateDeviceDTO;
import ink.usr.admin.mapper.SysControlAssignMapper;
import ink.usr.admin.mapper.SysControlMapper;
import ink.usr.admin.mapper.SysUserMapper;
import ink.usr.admin.service.SysControlAssignService;
import ink.usr.admin.service.SysUserService;
import ink.usr.admin.config.EmailConfig;
import ink.usr.common.model.mysql.SysControlAssignModel;
import ink.usr.common.model.mysql.SysControlModel;
import ink.usr.common.model.mysql.SysUserModel;
import ink.usr.framework.shiro.domain.ShiroUserInfo;
import ink.usr.framework.shiro.utils.ShiroUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class SysControlAssignServiceImpl implements SysControlAssignService {
    @Autowired
    private SysControlAssignMapper sysControlAssignMapper;
    @Autowired
    private SysControlMapper sysControlMapper;
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private EmailConfig emailConfig;

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
            
            // 如果分配完成或暂分配，发送邮件通知申请人
            if (flag && ("分配完成".equals(controlAssign.getAssignStatus()) || "暂分配".equals(controlAssign.getAssignStatus()))) {
                try {
                    // 获取申请人信息
                    String applicantName = sysUserService.getUserNickNameByUserId(controlAssign.getApplicant());
                    applicantName = applicantName != null ? applicantName : "未知申请人";
                    
                    // 获取申请人邮箱和部门
                    String applicantUserName = sysUserService.getNameByUserId(controlAssign.getApplicant());
                    SysUserModel applicantUserInfo = sysUserService.getUserInfoByUserName(applicantUserName);
                    String applicantEmail = applicantUserInfo != null ? applicantUserInfo.getEmail() : null;
                    String applicantDepartment = applicantUserInfo != null ? applicantUserInfo.getDepartment() : "";
                    
                    if (applicantEmail != null && !applicantEmail.isEmpty()) {
                        // 获取分配的电脑信息
                        String computerName = controlAssign.getCiName();
                        String computerType = controlAssign.getDeviceType();
                        boolean isTemporary = "暂分配".equals(controlAssign.getAssignStatus());
                        
                        // 通过电脑名获取电脑型号
                        String computerModel = "";
                        if (computerName != null && !computerName.isEmpty()) {
                            SysControlModel computerInfo = sysControlMapper.getComputerInfoByCiName(computerName);
                            if (computerInfo != null) {
                                computerModel = computerInfo.getModelOrVersion();
                            }
                        }
                        
                        // 构建邮件内容
                        String emailContent = emailConfig.buildAssignmentCompletedEmailContent(
                                applicantName,
                                applicantDepartment,
                                localTime,
                                computerName,
                                computerModel,
                                computerType,
                                isTemporary
                        );
                        
                        String emailSubject = isTemporary ? 
                            emailConfig.buildTempAssignmentEmailSubject() : 
                            emailConfig.buildAssignmentCompletedEmailSubject();
                        
                        // 发送邮件
                        emailConfig.sendMail(applicantEmail, emailSubject, emailContent);
                    }
                } catch (Exception e) {
                    // 记录日志但不影响主流程
                    log.error("发送分配邮件失败", e);
                }
            }
            
            return flag;
        }

        return false;
    }
}
