package ink.usr.admin.config;

import ink.usr.common.interfaces.admin.ISysEmailLogService;
import ink.usr.common.model.mysql.SysEmailLogModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

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
     * 异步发送邮件
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     */
    @Async
    public void sendMail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail); // 必须与配置一致
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
//            javaMailSender.send(message);
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
     * 构建设备申请审批邮件内容
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
     * @return 邮件内容
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
        StringBuilder emailContent = new StringBuilder();
        emailContent.append("设备申请审批\n\n");
        emailContent.append("申请人: ").append(applicantName).append("\n");
        emailContent.append("申请类别: ").append(deviceCategory).append("\n");
        emailContent.append("电脑类型: ").append(deviceType).append("\n");
        emailContent.append("成本中心: ").append(costCenter).append("\n");
        emailContent.append("所属公司: ").append(company).append("\n");
        emailContent.append("责任人: ").append(responsibilityName).append("\n");
        emailContent.append("电脑情形: ").append(deviceSituation).append("\n");
        emailContent.append("公司系统: ").append(companySystem).append("\n");
        emailContent.append("申请理由: ").append(reason).append("\n");
        
        if (ciName != null && !ciName.isEmpty()) {
            emailContent.append("需要更换的电脑: ").append(ciName).append("\n");
        }
        
        emailContent.append("\n请点击以下链接进行审批: \n").append(approvalUrl);
        
        return emailContent.toString();
    }
    
    /**
     * 构建设备申请审批邮件主题
     * @param applicantName 申请人姓名
     * @param deviceCategory 设备类别
     * @return 邮件主题
     */
    public String buildApplyEmailSubject(String applicantName, String deviceCategory) {
        return String.format("设备申请审批 - %s - %s", applicantName, deviceCategory);
    }

    /**
     * 构建临时密码邮件内容
     * @param userName 用户名
     * @param password 临时密码
     * @return 邮件内容
     */
    public String buildTempPasswordEmailContent(String userName, String password) {
        StringBuilder emailContent = new StringBuilder();
        emailContent.append("临时密码通知\n\n");
        emailContent.append("用户名: ").append(userName).append("\n");
        emailContent.append("临时密码: ").append(password).append("\n\n");
        emailContent.append("请使用此密码登录系统，建议登录后立即修改密码。\n");
        emailContent.append("此邮件由系统自动发送，请勿回复。");
        
        return emailContent.toString();
    }
    
    /**
     * 构建临时密码邮件主题
     * @return 邮件主题
     */
    public String buildTempPasswordEmailSubject() {
        return "SEG IT系统 - 临时密码通知";
    }
}
