package ink.usr.admin.service.Impl;

import ink.usr.admin.dao.DTO.SysAllocateDeviceDTO;
import ink.usr.admin.dao.VO.SysControlAssignListVO;
import ink.usr.admin.mapper.SysControlAssignMapper;
import ink.usr.admin.mapper.SysControlMapper;
import ink.usr.admin.mapper.SysUserMapper;
import ink.usr.admin.service.SysControlAssignService;
import ink.usr.admin.service.SysUserService;
import ink.usr.admin.service.SysControlService;
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
    private SysControlService sysControlService;
    @Autowired
    private EmailConfig emailConfig;

    @Override
    public List<SysControlAssignListVO> getControlAssignListWithApprovalInfo(SysControlAssignModel queryModel) {
        if (queryModel == null) {
            queryModel = new SysControlAssignModel();
        }
        // 直接调用关联查询方法
        return sysControlAssignMapper.getControlAssignListWithApprovalInfo(queryModel);
    }


//    @Override
//    public List<SysControlAssignModel> getControlAssignList(SysControlAssignModel queryModel) {
//        if (queryModel == null) {
//            // 如果查询模型为空，返回所有记录
//            return sysControlAssignMapper.getControlAssignList();
//        } else {
//            // 否则根据条件进行查询
//            return sysControlAssignMapper.getControlAssignListWithConditions(queryModel);
//        }
//    }
    @Override
    public boolean allocateDevice(SysAllocateDeviceDTO sysAllocateDeviceDTO) {

        // 通过approvalId找到订单
        SysControlAssignModel controlAssign = sysControlAssignMapper.getControlAssign(sysAllocateDeviceDTO.getApprovalId());
        if(controlAssign!=null){
            // 2.订单表sys_control_assign进行更改
            if(sysAllocateDeviceDTO.getIsTemp()!=null){
                // 根据申请类型和临时分配标志设置分配状态
                String deviceCategory = sysAllocateDeviceDTO.getDeviceCategory();
                if(sysAllocateDeviceDTO.getIsTemp() == 1){
                    controlAssign.setAssignStatus("暂分配");
                } else{
                    // 共享电脑申请设置为"待归还"，普通申请设置为"分配完成"
                    if ("共享电脑申请".equals(deviceCategory)) {
                        controlAssign.setAssignStatus("待归还");
                    } else {
                        controlAssign.setAssignStatus("分配完成");
                    }
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

    @Override
    public boolean deleteOrder(Long approvalId) {
        try {
            // 通过approvalId找到订单
            SysControlAssignModel controlAssign = sysControlAssignMapper.getControlAssign(approvalId);
            if (controlAssign != null) {
                // 将状态改为已关闭
                controlAssign.setAssignStatus("已关闭");
                
                // 更新时间
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime time = LocalDateTime.now();
                String localTime = df.format(time);
                controlAssign.setAssignTime(localTime);
                
                // 更新数据库
                boolean flag = sysControlAssignMapper.updateControlAssign(controlAssign);
                
                if (flag) {
                    log.info("订单 {} 删除成功，状态已变更为已关闭", approvalId);
                    
                    // 发送邮件通知申请人（可选）
                    try {
                        String applicantName = sysUserService.getUserNickNameByUserId(controlAssign.getApplicant());
                        applicantName = applicantName != null ? applicantName : "未知申请人";
                        
                        String applicantUserName = sysUserService.getNameByUserId(controlAssign.getApplicant());
                        SysUserModel applicantUserInfo = sysUserService.getUserInfoByUserName(applicantUserName);
                        String applicantEmail = applicantUserInfo != null ? applicantUserInfo.getEmail() : null;
                        
                        if (applicantEmail != null) {
                            String subject = "SEG IT电脑管理系统 - 订单已关闭";
                            String content = buildOrderDeletedEmailContent(
                                applicantName, 
                                approvalId
                            );
                            emailConfig.sendMail(applicantEmail, subject, content);
                            log.info("订单删除通知邮件已发送给申请人: {}", applicantEmail);
                        }
                    } catch (Exception e) {
                        log.error("发送订单删除通知邮件失败", e);
                        // 邮件发送失败不影响业务流程
                    }
                }
                
                return flag;
            } else {
                log.warn("未找到订单号为 {} 的记录", approvalId);
                return false;
            }
        } catch (Exception e) {
            log.error("删除订单失败: {}", approvalId, e);
            return false;
        }
    }

    @Override
    public boolean receiveDevice(SysAllocateDeviceDTO receiveDeviceDTO) {
        try {
            // 通过approvalId找到订单
            SysControlAssignModel controlAssign = sysControlAssignMapper.getControlAssign(receiveDeviceDTO.getApprovalId());
            if (controlAssign != null) {
                // 验证当前状态是否为分配完成
                if (!"分配完成".equals(controlAssign.getAssignStatus())) {
                    log.warn("订单 {} 当前状态为 {}，不允许领取", receiveDeviceDTO.getApprovalId(), controlAssign.getAssignStatus());
                    return false;
                }
                
                // 将状态改为已领取
                controlAssign.setAssignStatus("已领取");
                
                // 更新时间
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime time = LocalDateTime.now();
                String localTime = df.format(time);
                controlAssign.setAssignTime(localTime);
                
                boolean flag = sysControlAssignMapper.updateControlAssign(controlAssign);
                
                if (flag) {
                    log.info("设备领取成功，订单号: {}", receiveDeviceDTO.getApprovalId());
                    
                    // 发送邮件通知申请人
                    try {
                        String applicantName = sysUserService.getUserNickNameByUserId(controlAssign.getApplicant());
                        applicantName = applicantName != null ? applicantName : "未知申请人";
                        
                        String applicantUserName = sysUserService.getNameByUserId(controlAssign.getApplicant());
                        SysUserModel applicantUserInfo = sysUserService.getUserInfoByUserName(applicantUserName);
                        String applicantEmail = applicantUserInfo != null ? applicantUserInfo.getEmail() : null;
                        
                        if (applicantEmail != null) {
                            String subject = "SEG IT电脑管理系统 - 电脑已领取";
                            String content = buildDeviceReceivedEmailContent(
                                applicantName, 
                                receiveDeviceDTO.getApprovalId(), 
                                controlAssign.getCiName()
                            );
                            emailConfig.sendMail(applicantEmail, subject, content);
                            log.info("设备领取成功通知邮件已发送给申请人: {}", applicantEmail);
                        }
                    } catch (Exception e) {
                        log.error("发送设备领取成功通知邮件失败", e);
                        // 邮件发送失败不影响业务流程
                    }
                }
                
                return flag;
            } else {
                log.warn("未找到订单号为 {} 的记录", receiveDeviceDTO.getApprovalId());
                return false;
            }
        } catch (Exception e) {
            log.error("设备领取失败: {}", receiveDeviceDTO.getApprovalId(), e);
            return false;
        }
    }

    /**
     * 构建设备领取成功邮件内容（HTML格式）
     * @param applicantName 申请人姓名
     * @param approvalId 订单号
     * @param deviceName 设备名称
     * @return HTML格式邮件内容
     */
    private String buildDeviceReceivedEmailContent(String applicantName, Long approvalId, String deviceName) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>")
            .append("<html>")
            .append("<head>")
            .append("<meta charset=\"UTF-8\">")
            .append("<style type=\"text/css\">")
            .append("table { border-collapse: collapse; }")
            .append("</style>")
            .append("</head>")
            .append("<body style=\"margin: 0; padding: 0; font-family: Arial, sans-serif;\">")
            
            // 主容器table
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #f8f9fa;\">")
            .append("<tr>")
            .append("<td align=\"center\" style=\"padding: 20px;\">")
            
            // 邮件内容table
            .append("<table width=\"600\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #ffffff; border: 1px solid #005389;\">")
            
            // 头部
            .append("<tr>")
            .append("<td style=\"background-color: #005389; padding: 30px; text-align: center;\">")
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">")
            .append("<tr>")
            .append("<td style=\"color: #ffffff; font-size: 24px; font-weight: bold; text-align: center;\">✅ 设备领取成功</td>")
            .append("</tr>")
            .append("</table>")
            .append("</td>")
            .append("</tr>")
            
            // 主体内容
            .append("<tr>")
            .append("<td style=\"padding: 30px;\">")
            
            // 问候语
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">")
            .append("<tr>")
            .append("<td style=\"font-size: 16px; color: #333; text-align: center; padding-bottom: 20px;\">")
            .append("您好 ").append(applicantName).append("：")
            .append("</td>")
            .append("</tr>")
            .append("<tr>")
            .append("<td style=\"font-size: 16px; color: #333; text-align: center; padding-bottom: 30px;\">")
            .append("恭喜您！您的设备已成功领取。")
            .append("</td>")
            .append("</tr>")
            
            // 设备信息
            .append("<tr>")
            .append("<td>")
            .append("<table width=\"100%\" border=\"1\" cellpadding=\"15\" cellspacing=\"0\" style=\"border-color: #e0e0e0; margin-bottom: 20px;\">")
            .append("<tr style=\"background-color: #f8f9fa;\">")
            .append("<td colspan=\"2\" style=\"text-align: center; font-weight: bold; color: #005389; font-size: 18px;\">设备信息</td>")
            .append("</tr>")
            .append("<tr>")
            .append("<td style=\"width: 30%; font-weight: bold; background-color: #f8f9fa;\">设备名称：</td>")
            .append("<td style=\"width: 70%;\">").append(deviceName != null ? deviceName : "未知").append("</td>")
            .append("</tr>")
            .append("</table>")
            .append("</td>")
            .append("</tr>")
            
            // 注意事项
            .append("<tr>")
            .append("<td style=\"background-color: #e8f4fd; padding: 20px; border-left: 4px solid #005389; margin-bottom: 20px;\">")
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">")
            .append("<tr>")
            .append("<td style=\"font-weight: bold; color: #005389; font-size: 16px; text-align: center; padding-bottom: 12px;\">📋 重要提醒</td>")
            .append("</tr>")
            .append("<tr>")
            .append("<td style=\"color: #333; font-size: 14px; line-height: 1.6; text-align: center;\">")
            .append("请妥善保管设备，如有问题请及时联系管理员<br>")
            .append("设备仅限本人使用，请勿转借他人<br>")
            .append("如设备出现故障，请及时报修<br>")
            .append("离职或调岗时请及时归还设备")
            .append("</td>")
            .append("</tr>")
            .append("</table>")
            .append("</td>")
            .append("</tr>")
            
            // 联系信息
            .append("</table>")
            
            .append("</td>")
            .append("</tr>")
            
            // 底部
            .append("<tr>")
            .append("<td style=\"background-color: #f8f9fa; padding: 20px; text-align: center; color: #666; font-size: 12px;\">")
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">")
            .append("<tr>")
            .append("<td style=\"text-align: center; padding-bottom: 5px;\">此邮件由 SEG IT电脑管理系统 自动发送，请勿回复</td>")
            .append("</tr>")
            .append("<tr>")
            .append("<td style=\"text-align: center;\">Copyright © 2025 SEG IT Department. All Rights Reserved</td>")
            .append("</tr>")
            .append("</table>")
            .append("</td>")
            .append("</tr>")
            
            .append("</table>") // 结束邮件内容table
            .append("</td>")
            .append("</tr>")
            .append("</table>") // 结束主容器table
            
            .append("</body>")
            .append("</html>");
        
        return html.toString();
    }

    /**
     * 构建订单删除邮件内容（HTML格式）
     * @param applicantName 申请人姓名
     * @param approvalId 订单号
     * @return HTML格式邮件内容
     */
    private String buildOrderDeletedEmailContent(String applicantName, Long approvalId) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>")
            .append("<html>")
            .append("<head>")
            .append("<meta charset=\"UTF-8\">")
            .append("<style type=\"text/css\">")
            .append("table { border-collapse: collapse; }")
            .append("</style>")
            .append("</head>")
            .append("<body style=\"margin: 0; padding: 0; font-family: Arial, sans-serif;\">")
            
            // 主容器table
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #f8f9fa;\">")
            .append("<tr>")
            .append("<td align=\"center\" style=\"padding: 20px;\">")
            
            // 邮件内容table
            .append("<table width=\"600\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #ffffff; border: 1px solid #dc3545;\">")
            
            // 头部
            .append("<tr>")
            .append("<td style=\"background-color: #dc3545; padding: 30px; text-align: center;\">")
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">")
            .append("<tr>")
            .append("<td style=\"color: #ffffff; font-size: 24px; font-weight: bold; text-align: center;\">🗑️ 订单关闭通知</td>")
            .append("</tr>")
            .append("</table>")
            .append("</td>")
            .append("</tr>")
            
            // 主体内容
            .append("<tr>")
            .append("<td style=\"padding: 30px;\">")
            
            // 问候语
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">")
            .append("<tr>")
            .append("<td style=\"font-size: 16px; color: #333; text-align: center; padding-bottom: 20px;\">")
            .append("您好 ").append(applicantName).append("：")
            .append("</td>")
            .append("</tr>")
            .append("<tr>")
            .append("<td style=\"font-size: 16px; color: #333; text-align: center; padding-bottom: 30px;\">")
            .append("您的设备申请订单已被关闭。")
            .append("</td>")
            .append("</tr>")
            
            // 说明信息
            .append("<tr>")
            .append("<td style=\"background-color: #fff3cd; padding: 20px; border-left: 4px solid #ffc107; margin-bottom: 20px;\">")
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">")
            .append("<tr>")
            .append("<td style=\"font-weight: bold; color: #856404; font-size: 16px; text-align: center; padding-bottom: 12px;\">📋 说明</td>")
            .append("</tr>")
            .append("<tr>")
            .append("<td style=\"color: #333; font-size: 14px; line-height: 1.6; text-align: center;\">")
            .append("您的设备申请订单已被管理员关闭，状态已变更为已关闭。<br>")
            .append("如需要重新申请，请联系IT部门。")
            .append("</td>")
            .append("</tr>")
            .append("</table>")
            .append("</td>")
            .append("</tr>")
            .append("</table>")
            
            .append("</td>")
            .append("</tr>")
            
            // 底部
            .append("<tr>")
            .append("<td style=\"background-color: #f8f9fa; padding: 20px; text-align: center; color: #666; font-size: 12px;\">")
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">")
            .append("<tr>")
            .append("<td style=\"text-align: center; padding-bottom: 5px;\">此邮件由 SEG IT电脑管理系统 自动发送，请勿回复</td>")
            .append("</tr>")
            .append("<tr>")
            .append("<td style=\"text-align: center;\">Copyright © 2025 SEG IT Department. All Rights Reserved</td>")
            .append("</tr>")
            .append("</table>")
            .append("</td>")
            .append("</tr>")
            
            .append("</table>") // 结束邮件内容table
            .append("</td>")
            .append("</tr>")
            .append("</table>") // 结束主容器table
            
            .append("</body>")
            .append("</html>");
        
        return html.toString();
    }

    @Override
    public boolean returnSharedComputer(Long approvalId, String ciName) {
        try {
            // 1. 更新订单状态为"已归还"
            SysControlAssignModel controlAssign = sysControlAssignMapper.getControlAssign(approvalId);
            if (controlAssign != null) {
                controlAssign.setAssignStatus("已归还");
                
                // 更新时间
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime time = LocalDateTime.now();
                String localTime = df.format(time);
                controlAssign.setAssignTime(localTime);
                
                // 更新订单
                boolean orderUpdateFlag = sysControlAssignMapper.updateControlAssign(controlAssign);
                
                // 2. 更新电脑归属情况为"ShareNotebook"
                boolean computerUpdateFlag = sysControlService.updateComputerOwnership(ciName, "ShareNotebook");
                
                if (orderUpdateFlag && computerUpdateFlag) {
                    log.info("共享电脑归还成功, 订单号: {}, 电脑名: {}", approvalId, ciName);
                    return true;
                } else {
                    log.error("共享电脑归还失败, 订单更新: {}, 电脑更新: {}", orderUpdateFlag, computerUpdateFlag);
                    return false;
                }
            } else {
                log.warn("未找到订单号为 {} 的记录", approvalId);
                return false;
            }
        } catch (Exception e) {
            log.error("归还共享电脑失败: approvalId={}, ciName={}", approvalId, ciName, e);
            return false;
        }
    }
    
}
