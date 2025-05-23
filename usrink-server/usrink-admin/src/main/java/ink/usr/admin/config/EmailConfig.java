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
     * @return HTML格式邮件内容
     */
    public String buildApplyEmailContent(
            String applicantName,
            String deviceCategory,
            String deviceType,
            String costCenter,
            String company,
            String responsibilityName,
            String deviceSituation,
            String companySystem,
            String reason,
            String ciName,
            String approvalUrl
    ) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>")
            .append("<html lang=\"zh-CN\">")
            .append("<head>")
            .append("<meta charset=\"UTF-8\">")
            .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
            .append("<title>设备申请审批</title>")
            .append("<style>")
            .append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 20px; background-color: #f8f9fa; }")
            .append(".email-container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0, 83, 137, 0.08); border: 1px solid rgba(0, 83, 137, 0.2); }")
            .append(".email-header { background: linear-gradient(135deg, #005389, #029165); color: white; padding: 30px 25px; position: relative; }")
            .append(".email-header::before { content: ''; position: absolute; top: 0; left: 0; right: 0; height: 3px; background: linear-gradient(90deg, #005389, #029165, #005389); }")
            .append(".email-title { font-size: 24px; font-weight: 600; margin: 0 0 8px 0; }")
            .append(".email-subtitle { font-size: 14px; opacity: 0.9; margin: 0; }")
            .append(".email-body { padding: 25px; }")
            .append(".info-section { margin-bottom: 25px; }")
            .append(".section-title { font-size: 16px; font-weight: 600; color: #005389; margin-bottom: 15px; padding-bottom: 8px; border-bottom: 2px solid rgba(0, 83, 137, 0.1); }")
            .append(".info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 15px; }")
            .append(".info-item { background: rgba(0, 83, 137, 0.02); border: 1px solid rgba(0, 83, 137, 0.1); border-radius: 8px; padding: 12px; }")
            .append(".info-label { font-weight: 600; color: #005389; font-size: 13px; margin-bottom: 4px; }")
            .append(".info-value { color: #333; font-size: 14px; word-break: break-all; }")
            .append(".reason-section { background: linear-gradient(135deg, rgba(0, 83, 137, 0.05), rgba(2, 145, 101, 0.05)); border: 1px solid rgba(0, 83, 137, 0.1); border-radius: 8px; padding: 15px; margin: 20px 0; }")
            .append(".reason-title { font-weight: 600; color: #005389; margin-bottom: 8px; }")
            .append(".reason-content { line-height: 1.6; color: #333; white-space: pre-wrap; }")
            .append(".action-section { text-align: center; margin-top: 30px; padding: 20px; background: rgba(0, 83, 137, 0.02); border-radius: 8px; }")
            .append(".approval-button { display: inline-block; background: linear-gradient(135deg, #005389, #029165); color: white; text-decoration: none; padding: 12px 30px; border-radius: 8px; font-weight: 600; font-size: 16px; box-shadow: 0 4px 12px rgba(0, 83, 137, 0.2); transition: all 0.3s ease; }")
            .append(".approval-button:hover { background: linear-gradient(135deg, #0068ab, #02a674); box-shadow: 0 6px 16px rgba(0, 83, 137, 0.3); }")
            .append(".email-footer { background: #f8f9fa; padding: 20px 25px; text-align: center; color: #666; font-size: 12px; border-top: 1px solid rgba(0, 83, 137, 0.1); }")
            .append("@media (max-width: 600px) { .info-grid { grid-template-columns: 1fr; } .email-container { margin: 10px; } }")
            .append("</style>")
            .append("</head>")
            .append("<body>");
        
        html.append("<div class=\"email-container\">")
            .append("<div class=\"email-header\">")
            .append("<div class=\"email-title\">📋 设备申请审批</div>")
            .append("<div class=\"email-subtitle\">Device Application Approval</div>")
            .append("</div>");
        
        html.append("<div class=\"email-body\">")
            .append("<div class=\"info-section\">")
            .append("<div class=\"section-title\">📋 申请信息</div>")
            .append("<div class=\"info-grid\">")
            .append("<div class=\"info-item\"><div class=\"info-label\">申请人</div><div class=\"info-value\">").append(applicantName).append("</div></div>")
            .append("<div class=\"info-item\"><div class=\"info-label\">申请类别</div><div class=\"info-value\">").append(deviceCategory).append("</div></div>")
            .append("<div class=\"info-item\"><div class=\"info-label\">电脑类型</div><div class=\"info-value\">").append(deviceType).append("</div></div>")
            .append("<div class=\"info-item\"><div class=\"info-label\">成本中心</div><div class=\"info-value\">").append(costCenter).append("</div></div>")
            .append("<div class=\"info-item\"><div class=\"info-label\">所属公司</div><div class=\"info-value\">").append(company).append("</div></div>")
            .append("<div class=\"info-item\"><div class=\"info-label\">责任人</div><div class=\"info-value\">").append(responsibilityName).append("</div></div>")
            .append("<div class=\"info-item\"><div class=\"info-label\">电脑情形</div><div class=\"info-value\">").append(deviceSituation).append("</div></div>")
            .append("<div class=\"info-item\"><div class=\"info-label\">公司系统</div><div class=\"info-value\">").append(companySystem).append("</div></div>");
        
        if (ciName != null && !ciName.isEmpty() && !"申请新电脑".equals(ciName)) {
            html.append("<div class=\"info-item\" style=\"grid-column: 1 / -1;\">")
                .append("<div class=\"info-label\">需要更换的电脑</div>")
                .append("<div class=\"info-value\">").append(ciName).append("</div></div>");
        }
        
        html.append("</div></div>");
        
        html.append("<div class=\"reason-section\">")
            .append("<div class=\"reason-title\">💬 申请理由</div>")
            .append("<div class=\"reason-content\">").append(reason).append("</div>")
            .append("</div>");
        
        html.append("<div class=\"action-section\">")
            .append("<p style=\"margin: 0 0 15px 0; color: #666;\">请点击下方按钮进行审批操作</p>")
            .append("<a href=\"").append(approvalUrl).append("\" class=\"approval-button\">🔍 立即审批</a>")
            .append("</div>");
        
        html.append("</div>");
        
        html.append("<div class=\"email-footer\">")
            .append("<p style=\"margin: 0;\">此邮件由 SEG IT 管理系统自动发送，请勿回复</p>")
            .append("<p style=\"margin: 5px 0 0 0;\">© 2025 SEG IT Department. All Rights Reserved</p>")
            .append("</div>");
        
        html.append("</div>")
            .append("</body>")
            .append("</html>");
        
        return html.toString();
    }
    
    /**
     * 构建设备申请审批邮件主题
     * @param applicantName 申请人姓名
     * @param deviceCategory 设备类别
     * @return 邮件主题
     */
    public String buildApplyEmailSubject(String applicantName, String deviceCategory) {
        return String.format("📋 设备申请审批 - %s - %s", applicantName, deviceCategory);
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
            .append("</head>")
            .append("<body>");
        
        html.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #f5f5f5; padding: 20px;\">")
            .append("<tr>")
            .append("<td align=\"center\">")
            .append("<table width=\"500\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #ffffff; border: 1px solid #cccccc;\">")
            .append("<tr>")
            .append("<td align=\"center\" style=\"background-color: #005389; color: #ffffff; padding: 30px;\">")
            .append("<h1 style=\"margin: 0; font-size: 24px; color: #ffffff;\">🔑 临时密码通知</h1>")
            .append("</td>")
            .append("</tr>")
            .append("<tr>")
            .append("<td style=\"padding: 30px; text-align: center;\">")
            .append("<p style=\"font-size: 18px; color: #333333; margin: 0 0 20px 0;\">您好，<strong style=\"color: #005389;\">").append(userName).append("</strong></p>")
            .append("<p style=\"color: #666666; margin: 0 0 25px 0;\">您的临时密码已生成，请使用以下信息登录系统</p>")
            .append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin: 20px 0;\">")
            .append("<tr>")
            .append("<td align=\"center\" style=\"background-color: #f8f9fa; border: 2px solid #005389; padding: 25px;\">")
            .append("<p style=\"font-weight: bold; color: #005389; margin: 0 0 15px 0; font-size: 16px;\">临时密码</p>")
            .append("<p style=\"font-family: 'Courier New', monospace; font-size: 28px; font-weight: bold; color: #005389; background-color: #ffffff; padding: 15px; border: 1px solid #cccccc; letter-spacing: 3px; margin: 0;\">").append(password).append("</p>")
            .append("</td>")
            .append("</tr>")
            .append("</table>")
            .append("<p style=\"color: #e74c3c; font-weight: bold; margin: 20px 0 0 0; font-size: 16px;\">⚠️ 请勿告知他人此密码</p>")
            .append("<p style=\"color: #666666; margin: 15px 0 0 0; font-size: 14px;\">SEG IT部门不会以任何方式索要您的密码，请妥善保管。</p>")
            .append("</td>")
            .append("</tr>")
            .append("<tr>")
            .append("<td style=\"background-color: #f8f9fa; padding: 20px; text-align: center; color: #666666; font-size: 12px; border-top: 1px solid #cccccc;\">")
            .append("<p style=\"margin: 0;\">此邮件由 SEG IT 管理系统自动发送，请勿回复</p>")
            .append("<p style=\"margin: 5px 0 0 0;\">© 2025 SEG IT Department. All Rights Reserved</p>")
            .append("</td>")
            .append("</tr>")
            .append("</table>")
            .append("</td>")
            .append("</tr>")
            .append("</table>");
        
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
}
