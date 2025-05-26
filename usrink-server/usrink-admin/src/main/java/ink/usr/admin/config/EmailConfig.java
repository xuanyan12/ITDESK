package ink.usr.admin.config;

import ink.usr.common.interfaces.admin.ISysEmailLogService;
import ink.usr.common.model.mysql.SysEmailLogModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import javax.mail.internet.MimeMessage;

@Component
@Slf4j
public class EmailConfig {

    @Autowired
    private JavaMailSender javaMailSender;
    
    @Autowired
    private ISysEmailLogService sysEmailLogService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailConfig(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    /**
     * 异步发送HTML格式邮件
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param content HTML邮件内容
     */
    @Async
    public void sendMail(String to, String subject, String content) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail); // 必须与配置一致
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true); // true表示发送HTML格式邮件
            
            javaMailSender.send(message);
            log.info("邮件发送成功，发送给：{}", to);
            
            // 记录邮件发送日志
            SysEmailLogModel emailLog = SysEmailLogModel.builder()
                .toEmail(to)
                .subject(subject)
                .content(content)
                .status(0) // 0-成功
                .build();
            sysEmailLogService.insertSysEmailLog(emailLog);
        } catch (Exception e) {
            // 捕获并处理异常，确保不会影响主流程
            log.error("邮件发送失败，发送给：{}，原因：{}", to, e.getMessage());
            
            // 记录邮件发送失败日志
            SysEmailLogModel emailLog = SysEmailLogModel.builder()
                .toEmail(to)
                .subject(subject)
                .content(content)
                .status(1) // 1-失败
                .errorMsg(e.getMessage())
                .build();
            try {
                sysEmailLogService.insertSysEmailLog(emailLog);
            } catch (Exception ex) {
                log.error("记录邮件发送失败日志失败：{}", ex.getMessage());
            }
            
            e.printStackTrace();
        }
    }

    /**
     * 构建设备申请审批邮件内容（HTML格式）
     * @param applicantName 申请人姓名
     * @param applicantDepartment 申请人部门
     * @param deviceCategory 设备类别
     * @param deviceType 电脑类型
     * @param costCenter 成本中心
     * @param company 所属公司
     * @param responsibilityName 责任人
     * @param deviceSituation 电脑情形
     * @param companySystem 公司系统
     * @param reason 申请理由
     * @param ciName 需要更换的电脑名称（可为null）
     * @param approvalUrl 审批链接
     * @param applyTime 申请时间
     * @return HTML格式邮件内容
     */
    public String buildApplyEmailContent(
            String applicantName,
            String applicantDepartment,
            String deviceCategory,
            String deviceType,
            String costCenter,
            String company,
            String responsibilityName,
            String deviceSituation,
            String companySystem,
            String reason,
            String ciName,
            String approvalUrl,
            String applyTime
    ) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>")
            .append("<html>")
            .append("<head>")
            .append("<meta charset=\"UTF-8\">")
            .append("<style type=\"text/css\">")
            .append("table { border-collapse: collapse; }")
            .append("</style>")
            .append("</head>")
            .append("<body style=\"margin: 0; padding: 0; font-family: Arial, sans-serif;\">");
        
        // 主容器table
        html.append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #f8f9fa;\">")
            .append("<tr>")
            .append("<td align=\"center\" style=\"padding: 20px;\">")
            
            // 邮件内容table
            .append("<table width=\"600\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #ffffff; border: 1px solid #005389;\">")
            
            // 头部
            .append("<tr>")
            .append("<td style=\"background-color: #005389; padding: 30px; text-align: center;\">")
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">")
            .append("<tr>")
            .append("<td style=\"color: #ffffff; font-size: 24px; font-weight: bold; text-align: center;\">电脑申请审批通知</td>")
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
            .append("您好，有一条新的电脑申请已分配给您进行审批，关键信息如下：")
            .append("</td>")
            .append("</tr>")
            .append("</table>")
            
            // 关键信息区域
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"15\" cellspacing=\"0\" style=\"margin: 20px 0;\">")
            .append("<tr>")
            .append("<td>")
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"8\" cellspacing=\"0\">")
            .append("<tr>")
            .append("<td width=\"30%\" style=\"font-weight: bold; color: #005389;\">使用人：</td>")
            .append("<td style=\"color: #333;\">").append(applicantName).append("</td>")
            .append("</tr>")
            .append("<tr>")
            .append("<td style=\"font-weight: bold; color: #005389;\">申请类别：</td>")
            .append("<td style=\"color: #333;\">").append(deviceCategory).append("</td>")
            .append("</tr>")
            .append("<tr>")
            .append("<td style=\"font-weight: bold; color: #005389;\">申请时间：</td>")
            .append("<td style=\"color: #333;\">").append(applyTime).append("</td>")
            .append("</tr>");
        
        // 如果有申请理由，添加到表格中
        if (reason != null && !reason.isEmpty()) {
            html.append("<tr>")
                .append("<td style=\"font-weight: bold; color: #005389;\">申请理由：</td>")
                .append("<td style=\"color: #333;\">").append(reason).append("</td>")
                .append("</tr>");
        }
        
        html.append("</table>")
            .append("</td>")
            .append("</tr>")
            .append("</table>");
        
        // 审批按钮区域
        html.append("<table width=\"100%\" border=\"0\" cellpadding=\"25\" cellspacing=\"0\" style=\"margin: 20px 0;\">")
            .append("<tr>")
            .append("<td style=\"text-align: center;\">")
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">")
            .append("<tr>")
            .append("<td style=\"color: #005389; font-size: 16px; font-weight: bold; text-align: center; padding-bottom: 20px;\">请点击以下按钮查看详情并进行审批：</td>")
            .append("</tr>")
            .append("<tr>")
            .append("<td style=\"text-align: center;\">")
            .append("<table border=\"0\" cellpadding=\"15\" cellspacing=\"0\" style=\"background-color: #005389; margin: 0 auto;\">")
            .append("<tr>")
            .append("<td>")
            .append("<a href=\"").append(approvalUrl).append("\" style=\"color: #ffffff; text-decoration: none; font-weight: bold; font-size: 16px;\">🔍 立即审批</a>")
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
            .append("</table>"); // 结束主容器table
        
        html.append("</body>")
            .append("</html>");
        
        return html.toString();
    }
    
    /**
     * 构建设备申请审批邮件主题
     * @param applicantName 申请人姓名
     * @param applicantDepartment 申请人部门
     * @param deviceCategory 设备类别
     * @return 邮件主题
     */
    public String buildApplyEmailSubject(String applicantName, String applicantDepartment, String deviceCategory) {
        return String.format("SEG IT电脑管理系统 - 新增待审批 - %s", deviceCategory);
    }

    /**
     * 构建临时密码邮件内容（HTML格式）
     * @param userName 用户名
     * @param password 临时密码
     * @return HTML格式邮件内容
     */
    public String buildTempPasswordEmailContent(String userName, String password) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>")
            .append("<html>")
            .append("<head>")
            .append("<meta charset=\"UTF-8\">")
            .append("<style type=\"text/css\">")
            .append("table { border-collapse: collapse; }")
            .append("</style>")
            .append("</head>")
            .append("<body style=\"margin: 0; padding: 0; font-family: Arial, sans-serif;\">");
        
        // 主容器table
        html.append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #f8f9fa;\">")
            .append("<tr>")
            .append("<td align=\"center\" style=\"padding: 20px;\">")
            
            // 邮件内容table
            .append("<table width=\"600\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #ffffff; border: 1px solid #005389;\">")
            
            // 头部
            .append("<tr>")
            .append("<td style=\"background-color: #005389; padding: 30px; text-align: center;\">")
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">")
            .append("<tr>")
            .append("<td style=\"color: #ffffff; font-size: 24px; font-weight: bold; text-align: center;\">🔑 临时密码通知</td>")
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
            .append("您好，<strong style=\"color: #005389;\">").append(userName).append("</strong><br><br>")
            .append("您的临时密码已生成，请使用以下信息登录系统")
            .append("</td>")
            .append("</tr>")
            .append("</table>")
            
            // 密码显示区域
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"25\" cellspacing=\"0\" style=\"margin: 20px 0;\">")
            .append("<tr>")
            .append("<td style=\"text-align: center;\">")
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">")
            .append("<tr>")
            .append("<td style=\"font-weight: bold; color: #005389; font-size: 18px; text-align: center; padding-bottom: 15px;\">临时密码</td>")
            .append("</tr>")
            .append("<tr>")
            .append("<td style=\"text-align: center;\">")
            .append("<table border=\"0\" cellpadding=\"20\" cellspacing=\"0\" style=\"margin: 0 auto;\">")
            .append("<tr>")
            .append("<td style=\"font-family: 'Courier New', monospace; font-size: 28px; font-weight: bold; color: #005389; letter-spacing: 3px; text-align: center; word-break: break-all;\">")
            .append(password)
            .append("</td>")
            .append("</tr>")
            .append("</table>")
            .append("</td>")
            .append("</tr>")
            .append("</table>")
            .append("</td>")
            .append("</tr>")
            .append("</table>")
            
            // 安全提醒区域
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"20\" cellspacing=\"0\" style=\"margin: 20px 0;\">")
            .append("<tr>")
            .append("<td style=\"text-align: center;\">")
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">")
            .append("<tr>")
            .append("<td style=\"font-weight: bold; color: #dc3545; font-size: 16px; text-align: center; padding-bottom: 12px;\">⚠️ 请勿告知他人此密码</td>")
            .append("</tr>")
            .append("<tr>")
            .append("<td style=\"color: #333; font-size: 14px; text-align: center;\">SEG IT部门不会以任何方式索要您的密码，请妥善保管。</td>")
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
            .append("<td style=\"text-align: center; padding-bottom: 5px;\">此邮件由 SEG IT 管理系统自动发送，请勿回复</td>")
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
            .append("</table>"); // 结束主容器table
        
        html.append("</body>")
            .append("</html>");
        
        return html.toString();
    }
    
    /**
     * 构建临时密码邮件主题
     * @return 邮件主题
     */
    public String buildTempPasswordEmailSubject() {
        return "🔑 SEG IT系统 - 临时密码通知";
    }
    
    /**
     * 构建审批拒绝邮件内容（HTML格式）
     * @param applicantName 申请人姓名
     * @param deviceCategory 申请类别
     * @param applyTime 申请时间
     * @param rejectReason 拒绝原因
     * @param rejectTime 拒绝时间
     * @return HTML格式邮件内容
     */
    public String buildRejectionEmailContent(
            String applicantName,
            String deviceCategory,
            String applyTime,
            String rejectReason,
            String rejectTime
    ) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>")
            .append("<html>")
            .append("<head>")
            .append("<meta charset=\"UTF-8\">")
            .append("<style type=\"text/css\">")
            .append("table { border-collapse: collapse; }")
            .append("</style>")
            .append("</head>")
            .append("<body style=\"margin: 0; padding: 0; font-family: Arial, sans-serif;\">");
        
        // 主容器table
        html.append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #f8f9fa;\">")
            .append("<tr>")
            .append("<td align=\"center\" style=\"padding: 20px;\">")
            
            // 邮件内容table
            .append("<table width=\"600\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #ffffff; border: 1px solid #005389;\">")
            
            // 头部
            .append("<tr>")
            .append("<td style=\"background-color: #005389; padding: 30px; text-align: center;\">")
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">")
            .append("<tr>")
            .append("<td style=\"color: #ffffff; font-size: 24px; font-weight: bold; text-align: center;\">电脑申请被拒绝通知</td>")
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
            .append("您好，<strong style=\"color: #005389;\">").append(applicantName).append("</strong><br><br>")
            .append("您的电脑申请请求已于 <strong style=\"color: #005389;\">").append(rejectTime).append("</strong> <strong style=\"color: #dc3545;\">被拒绝</strong><br>")
            .append("以下是您的申请信息与审批理由：")
            .append("</td>")
            .append("</tr>")
            .append("</table>")
            
            // 申请信息区域
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"15\" cellspacing=\"0\" style=\"margin: 20px 0;\">")
            .append("<tr>")
            .append("<td>")
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"8\" cellspacing=\"0\">")
            .append("<tr>")
            .append("<td width=\"30%\" style=\"font-weight: bold; color: #005389;\">申请时间：</td>")
            .append("<td style=\"color: #333;\">").append(applyTime).append("</td>")
            .append("</tr>")
            .append("<tr>")
            .append("<td style=\"font-weight: bold; color: #005389;\">申请类别：</td>")
            .append("<td style=\"color: #333;\">").append(deviceCategory).append("</td>")
            .append("</tr>")
            .append("</table>")
            .append("</td>")
            .append("</tr>")
            .append("</table>")
            
            // 拒绝原因
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"15\" cellspacing=\"0\" style=\"margin: 20px 0;\">")
            .append("<tr>")
            .append("<td>")
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">")
            .append("<tr>")
            .append("<td style=\"font-weight: bold; color: #005389; font-size: 16px; text-align: center; padding-bottom: 12px;\">📝 拒绝原因</td>")
            .append("</tr>")
            .append("<tr>")
            .append("<td style=\"padding: 15px; color: #333; text-align: center;\">").append(rejectReason != null ? rejectReason : "未提供具体原因").append("</td>")
            .append("</tr>")
            .append("</table>")
            .append("</td>")
            .append("</tr>")
            .append("</table>")
            
            // 联系提示区域
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"25\" cellspacing=\"0\" style=\"margin: 20px 0;\">")
            .append("<tr>")
            .append("<td style=\"text-align: center;\">")
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">")
            .append("<tr>")
            .append("<td style=\"color: #666; font-size: 15px; text-align: center; padding-bottom: 15px;\">如有疑问，请联系IT部门或重新提交申请</td>")
            .append("</tr>")
            .append("<tr>")
            .append("<td style=\"color: #005389; font-weight: bold; font-size: 14px; text-align: center;\">📧 联系IT部门获取更多帮助</td>")
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
            .append("</table>"); // 结束主容器table
        
        html.append("</body>")
            .append("</html>");
        
        return html.toString();
    }
    
    /**
     * 构建审批拒绝邮件主题
     * @return 邮件主题
     */
    public String buildRejectionEmailSubject() {
        return "SEG IT电脑管理系统 - 您的电脑申请请求已被拒绝";
    }
    
    /**
     * 构建审批通过邮件内容（HTML格式）
     * @param applicantName 申请人姓名
     * @param applicantDepartment 申请人部门
     * @param approvalTime 审批通过时间
     * @return HTML格式邮件内容
     */
    public String buildApprovalPassedEmailContent(
            String applicantName,
            String applicantDepartment,
            String approvalTime
    ) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>")
            .append("<html>")
            .append("<head>")
            .append("<meta charset=\"UTF-8\">")
            .append("<style type=\"text/css\">")
            .append("table { border-collapse: collapse; }")
            .append("</style>")
            .append("</head>")
            .append("<body style=\"margin: 0; padding: 0; font-family: Arial, sans-serif;\">");
        
        // 主容器table
        html.append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #f8f9fa;\">")
            .append("<tr>")
            .append("<td align=\"center\" style=\"padding: 20px;\">")
            
            // 邮件内容table
            .append("<table width=\"600\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #ffffff; border: 1px solid #005389;\">")
            
            // 头部
            .append("<tr>")
            .append("<td style=\"background-color: #005389; padding: 30px; text-align: center;\">")
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">")
            .append("<tr>")
            .append("<td style=\"color: #ffffff; font-size: 24px; font-weight: bold; text-align: center;\">电脑申请已批准通知</td>")
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
            .append("您好，<strong style=\"color: #005389;\">").append(applicantName).append("</strong>")
            .append("</td>")
            .append("</tr>")
            .append("</table>")
            
            // 状态信息区域
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"25\" cellspacing=\"0\" style=\"margin: 20px 0;\">")
            .append("<tr>")
            .append("<td style=\"text-align: center;\">")
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">")
            .append("<tr>")
            .append("<td style=\"font-weight: bold; color: #005389; font-size: 18px; text-align: center; padding-bottom: 15px;\">🎉 申请审批通过</td>")
            .append("</tr>")
            .append("<tr>")
            .append("<td style=\"color: #333; font-size: 16px; text-align: center;\">")
            .append("您的电脑申请请求已于 <strong style=\"color: #005389;\">").append(approvalTime).append("</strong> 审批通过，<br>")
            .append("流程已进入IT部门采购分配阶段，请耐心等待分配，<br>")
            .append("分配完成后会通过邮件另行通知。")
            .append("</td>")
            .append("</tr>")
            .append("</table>")
            .append("</td>")
            .append("</tr>")
            .append("</table>")
            
            // 提示信息区域
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"25\" cellspacing=\"0\" style=\"margin: 20px 0;\">")
            .append("<tr>")
            .append("<td style=\"text-align: center;\">")
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">")
            .append("<tr>")
            .append("<td style=\"color: #666; font-size: 15px; text-align: center;\">")
            .append("感谢您的耐心等待，如有任何疑问请联系IT部门<br>")
            .append("📧 我们会在设备分配完成后第一时间通知您")
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
            .append("</table>"); // 结束主容器table
        
        html.append("</body>")
            .append("</html>");
        
        return html.toString();
    }
    
    /**
     * 构建审批通过邮件主题
     * @return 邮件主题
     */
    public String buildApprovalPassedEmailSubject() {
        return "SEG IT电脑管理系统 - 电脑申请请求已批准&等待分配";
    }
    
    /**
     * 构建电脑分配完成邮件内容（HTML格式）- 向后兼容版本
     * @param applicantName 申请人姓名
     * @param applicantDepartment 申请人部门
     * @param assignTime 分配完成时间
     * @param computerName 电脑名称
     * @param computerModel 电脑型号
     * @param computerType 电脑类型
     * @return HTML格式邮件内容
     */
    public String buildAssignmentCompletedEmailContent(
            String applicantName,
            String applicantDepartment,
            String assignTime,
            String computerName,
            String computerModel,
            String computerType
    ) {
        return buildAssignmentCompletedEmailContent(
                applicantName, applicantDepartment, assignTime, 
                computerName, computerModel, computerType, false
        );
    }
    
    /**
     * 构建电脑分配完成邮件内容（HTML格式）
     * @param applicantName 申请人姓名
     * @param applicantDepartment 申请人部门
     * @param assignTime 分配完成时间
     * @param computerName 电脑名称
     * @param computerModel 电脑型号
     * @param computerType 电脑类型
     * @param isTemporary 是否为暂分配
     * @return HTML格式邮件内容
     */
    public String buildAssignmentCompletedEmailContent(
            String applicantName,
            String applicantDepartment,
            String assignTime,
            String computerName,
            String computerModel,
            String computerType,
            boolean isTemporary
    ) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>")
            .append("<html>")
            .append("<head>")
            .append("<meta charset=\"UTF-8\">")
            .append("<style type=\"text/css\">")
            .append("table { border-collapse: collapse; }")
            .append("</style>")
            .append("</head>")
            .append("<body style=\"margin: 0; padding: 0; font-family: Arial, sans-serif;\">");
        
        // 主容器table
        html.append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #f8f9fa;\">")
            .append("<tr>")
            .append("<td align=\"center\" style=\"padding: 20px;\">")
            
            // 邮件内容table
            .append("<table width=\"600\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #ffffff; border: 1px solid #005389;\">")
            
            // 头部
            .append("<tr>")
            .append("<td style=\"background-color: #005389; padding: 30px; text-align: center;\">")
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">")
            .append("<tr>")
            .append("<td style=\"color: #ffffff; font-size: 24px; font-weight: bold; text-align: center;\">")
            .append(isTemporary ? "电脑已暂分配待领取" : "电脑已分配完成待领取")
            .append("</td>")
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
            .append("您好，<strong style=\"color: #005389;\">").append(applicantName).append("</strong>")
            .append("</td>")
            .append("</tr>")
            .append("</table>")
            
            // 分配完成信息区域
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"25\" cellspacing=\"0\" style=\"margin: 20px 0;\">")
            .append("<tr>")
            .append("<td style=\"text-align: center;\">")
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">")
            .append("<tr>")
            .append("<td style=\"font-weight: bold; color: #005389; font-size: 18px; text-align: center; padding-bottom: 15px;\">")
            .append(isTemporary ? "⚠️ 电脑暂分配完成" : "🎉 电脑分配完成")
            .append("</td>")
            .append("</tr>")
            .append("<tr>")
            .append("<td style=\"color: #333; font-size: 16px; text-align: center;\">")
            .append("您的电脑申请已于 <strong style=\"color: #005389;\">").append(assignTime).append("</strong> ")
            .append(isTemporary ? "完成暂分配" : "完成分配").append("，<br>")
            .append("请尽快前往IT部门领取，如有旧电脑请一并携带并交予。")
            .append("</td>")
            .append("</tr>")
            .append("</table>")
            .append("</td>")
            .append("</tr>")
            .append("</table>");
        
        // 暂分配特别提醒
        if (isTemporary) {
            html.append("<table width=\"100%\" border=\"0\" cellpadding=\"20\" cellspacing=\"0\" style=\"margin: 20px 0;\">")
                .append("<tr>")
                .append("<td style=\"text-align: center;\">")
                .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">")
                .append("<tr>")
                .append("<td style=\"font-weight: bold; color: #856404; font-size: 16px; text-align: center; padding-bottom: 10px;\">⚠️ 暂分配说明</td>")
                .append("</tr>")
                .append("<tr>")
                .append("<td style=\"color: #856404; font-size: 14px; text-align: center;\">")
                .append("此次为暂时分配，可能会根据实际情况进行调整。<br>")
                .append("如有变更，我们会及时通知您。请先按照以下信息领取设备。")
                .append("</td>")
                .append("</tr>")
                .append("</table>")
                .append("</td>")
                .append("</tr>")
                .append("</table>");
        }
        
        // 电脑信息区域
        html.append("<table width=\"100%\" border=\"0\" cellpadding=\"15\" cellspacing=\"0\" style=\"margin: 20px 0;\">")
            .append("<tr>")
            .append("<td>")
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">")
            .append("<tr>")
            .append("<td style=\"font-weight: bold; color: #005389; font-size: 16px; text-align: center; padding-bottom: 15px;\">💻 ")
            .append(isTemporary ? "暂分配给您的电脑信息" : "分配给您的电脑信息")
            .append("</td>")
            .append("</tr>")
            .append("<tr>")
            .append("<td>")
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"8\" cellspacing=\"0\">")
            .append("<tr>")
            .append("<td width=\"30%\" style=\"font-weight: bold; color: #005389;\">电脑名称：</td>")
            .append("<td style=\"color: #333;\">").append(computerName != null ? computerName : "待确认").append("</td>")
            .append("</tr>")
            .append("<tr>")
            .append("<td style=\"font-weight: bold; color: #005389;\">型号：</td>")
            .append("<td style=\"color: #333;\">").append(computerModel != null ? computerModel : "待确认").append("</td>")
            .append("</tr>")
            .append("<tr>")
            .append("<td style=\"font-weight: bold; color: #005389;\">电脑类型：</td>")
            .append("<td style=\"color: #333;\">").append(computerType != null ? computerType : "待确认").append("</td>")
            .append("</tr>")
            .append("</table>")
            .append("</td>")
            .append("</tr>")
            .append("</table>")
            .append("</td>")
            .append("</tr>")
            .append("</table>")
            
            // 领取时间安排区域
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"25\" cellspacing=\"0\" style=\"margin: 20px 0;\">")
            .append("<tr>")
            .append("<td style=\"text-align: center;\">")
            .append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">")
            .append("<tr>")
            .append("<td style=\"font-weight: bold; color: #005389; font-size: 16px; text-align: center; padding-bottom: 15px;\">⏰ 领取时间安排</td>")
            .append("</tr>")
            .append("<tr>")
            .append("<td style=\"color: #333; font-size: 15px; text-align: center;\">")
            .append("请在工作日的下列时间段前往IT部门领取：<br><br>")
            .append("<table border=\"0\" cellpadding=\"10\" cellspacing=\"0\" style=\"margin: 8px auto;\">")
            .append("<tr>")
            .append("<td style=\"text-align: center;\">🌅 <strong>上午：10:30 - 11:30</strong></td>")
            .append("</tr>")
            .append("</table>")
            .append("<table border=\"0\" cellpadding=\"10\" cellspacing=\"0\" style=\"margin: 8px auto;\">")
            .append("<tr>")
            .append("<td style=\"text-align: center;\">🌇 <strong>下午：14:00 - 15:00</strong></td>")
            .append("</tr>")
            .append("</table>")
            .append("<br>📍 如有疑问请联系IT部门")
            .append("</td>")
            .append("</tr>")
            .append("</table>")
            .append("</td>")
            .append("</tr>")
            
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
            .append("</table>"); // 结束主容器table
        
        html.append("</body>")
            .append("</html>");
        
        return html.toString();
    }
    
    /**
     * 构建电脑分配完成邮件主题
     * @return 邮件主题
     */
    public String buildAssignmentCompletedEmailSubject() {
        return "SEG IT电脑管理系统 - 电脑已分配完成待领取";
    }
    
    /**
     * 构建电脑暂分配邮件主题
     * @return 邮件主题
     */
    public String buildTempAssignmentEmailSubject() {
        return "SEG IT电脑管理系统 - 电脑已暂分配待领取";
    }
}
