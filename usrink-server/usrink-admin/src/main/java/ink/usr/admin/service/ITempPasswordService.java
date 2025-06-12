package ink.usr.admin.service;

/**
 * 临时密码服务接口
 * 
 * @author UsrInk
 * @date 2025-01-10
 */
public interface ITempPasswordService {
    
    /**
     * 生成并发送临时密码
     * 
     * @param userName 用户名
     * @return 处理结果消息
     */
    String generateAndSendTempPassword(String userName);
    
    /**
     * 验证临时密码
     * 
     * @param userName 用户名
     * @param tempPassword 临时密码
     * @return 是否验证通过
     */
    boolean validateTempPassword(String userName, String tempPassword);
    
    /**
     * 清除用户的临时密码
     * 
     * @param userName 用户名
     */
    void clearTempPassword(String userName);
    
    /**
     * 检查用户是否可以获取临时密码（频率控制）
     * 
     * @param userName 用户名
     * @return 是否可以获取
     */
    boolean canRequestTempPassword(String userName);
} 