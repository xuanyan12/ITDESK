package ink.usr.admin.service.Impl;

import ink.usr.admin.config.EmailConfig;
import ink.usr.admin.service.ITempPasswordService;
import ink.usr.common.interfaces.admin.ISysUserService;
import ink.usr.common.core.utils.StringUtil;
import ink.usr.common.model.mysql.SysUserModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * 临时密码服务实现类
 * 
 * @author UsrInk
 * @date 2025-01-10
 */
@Service
@Slf4j
public class TempPasswordServiceImpl implements ITempPasswordService {
    
    @Autowired
    private ISysUserService sysUserService;
    
    @Autowired
    private EmailConfig emailConfig;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 临时密码在Redis中的key前缀
    private static final String TEMP_PASSWORD_PREFIX = "temp_password:";
    
    // 临时密码请求频率控制key前缀
    private static final String TEMP_PASSWORD_REQUEST_PREFIX = "temp_password_request:";
    
    // 从配置文件读取临时密码配置
    @Value("${temp.password.expire.seconds:1800}")
    private Long tempPasswordExpireSeconds;
    
    @Value("${temp.password.length:8}")
    private Integer tempPasswordLength;
    
    @Value("${temp.password.request.interval.seconds:60}")
    private Long requestIntervalSeconds;
    
    @Override
    public String generateAndSendTempPassword(String userName) {

        try {
            log.info("开始处理获取临时密码请求，用户名: {}", userName);
            
            // 基本参数验证
            if (StringUtil.isEmpty(userName)) {
                return "用户名不能为空";
            }
            
            // 检查请求频率
            if (!canRequestTempPassword(userName)) {
                return String.format("请求过于频繁，请等待%d秒后再试", requestIntervalSeconds);
            }
            
            // 检查用户是否存在
            SysUserModel sysUserModel = sysUserService.checkUserNameUnique(userName);
            if (sysUserModel == null) {
                return "该用户不存在，请联系IT进行处理";
            }
            
            // 验证邮箱是否存在
            if (StringUtil.isEmpty(sysUserModel.getEmail())) {
                return "该用户未设置邮箱，请联系IT进行处理";
            }
            
            // 生成临时密码
            String tempPassword = generateTempPassword();
            
            // 存储到Redis中
            String redisKey = TEMP_PASSWORD_PREFIX + userName;
            redisTemplate.opsForValue().set(redisKey, tempPassword, tempPasswordExpireSeconds, TimeUnit.SECONDS);
            
            // 记录请求时间，用于频率控制
            String requestKey = TEMP_PASSWORD_REQUEST_PREFIX + userName;
            redisTemplate.opsForValue().set(requestKey, System.currentTimeMillis(), requestIntervalSeconds, TimeUnit.SECONDS);
            
            // 发送邮件
            String emailSubject = emailConfig.buildTempPasswordEmailSubject();
            String emailContent = emailConfig.buildTempPasswordEmailContent(userName, tempPassword);
            emailConfig.sendMail(sysUserModel.getEmail(), emailSubject, emailContent);
            
            log.info("临时密码生成并发送成功，用户名: {}, 邮箱: {}", userName, sysUserModel.getEmail());
            
            return "临时密码已发送到您的邮箱，请查收";
            
        } catch (Exception e) {
            log.error("生成并发送临时密码失败，用户名: {}", userName, e);
            return "获取临时密码失败，请联系IT进行处理";
        }
    }
    
    @Override
    public boolean validateTempPassword(String userName, String tempPassword) {
        try {
            if (StringUtil.isEmpty(userName) || StringUtil.isEmpty(tempPassword)) {
                return false;
            }
            
            String redisKey = TEMP_PASSWORD_PREFIX + userName;
            Object storedPassword = redisTemplate.opsForValue().get(redisKey);
            
            if (storedPassword == null) {
                log.info("临时密码已过期或不存在，用户名: {}", userName);
                return false;
            }
            
            boolean isValid = tempPassword.equals(storedPassword.toString());
            
            if (isValid) {
                log.info("临时密码验证成功，用户名: {}", userName);
                // 验证成功后清除临时密码
                clearTempPassword(userName);
            } else {
                log.warn("临时密码验证失败，用户名: {}", userName);
            }
            
            return isValid;
            
        } catch (Exception e) {
            log.error("验证临时密码时发生错误，用户名: {}", userName, e);
            return false;
        }
    }
    
    @Override
    public void clearTempPassword(String userName) {
        try {
            if (!StringUtil.isEmpty(userName)) {
                String redisKey = TEMP_PASSWORD_PREFIX + userName;
                redisTemplate.delete(redisKey);
                log.info("临时密码已清除，用户名: {}", userName);
            }
        } catch (Exception e) {
            log.error("清除临时密码失败，用户名: {}", userName, e);
        }
    }
    
    @Override
    public boolean canRequestTempPassword(String userName) {
        try {
            String requestKey = TEMP_PASSWORD_REQUEST_PREFIX + userName;
            Object lastRequestTime = redisTemplate.opsForValue().get(requestKey);
            
            if (lastRequestTime == null) {
                return true; // 没有请求记录，可以请求
            }
            
            long lastTime = Long.parseLong(lastRequestTime.toString());
            long currentTime = System.currentTimeMillis();
            long timeDiff = (currentTime - lastTime) / 1000; // 转换为秒
            
            return timeDiff >= requestIntervalSeconds;
            
        } catch (Exception e) {
            log.error("检查临时密码请求频率时发生错误，用户名: {}", userName, e);
            return true; // 出错时允许请求，避免影响用户体验
        }
    }
    
    /**
     * 生成临时密码
     * 使用数字和字母组合，确保安全性和易读性
     * 
     * @return 临时密码
     */
    private String generateTempPassword() {
        // 定义字符集：数字+大写字母+小写字母，排除容易混淆的字符
        String chars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < tempPasswordLength; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }
} 