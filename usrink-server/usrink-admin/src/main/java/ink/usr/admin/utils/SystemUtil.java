package ink.usr.admin.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * 系统工具类，用于获取系统相关信息
 */
@Slf4j
public class SystemUtil {

    /**
     * 获取当前操作系统登录的用户名
     * 
     * @return 当前操作系统登录的用户名
     */
    public static String getCurrentOsUser() {
        try {
            // 获取系统属性中的当前用户
            String osUser = System.getProperty("user.name");
            log.info("当前操作系统登录用户: {}", osUser);
            return osUser;
        } catch (Exception e) {
            log.error("获取操作系统登录用户失败", e);
            return null;
        }
    }
    
    /**
     * 比较两个用户名是否相同（不区分大小写）
     * 
     * @param user1 用户名1
     * @param user2 用户名2
     * @return 是否相同
     */
    public static boolean isSameUser(String user1, String user2) {
        if (user1 == null || user2 == null) {
            return false;
        }
        return user1.equalsIgnoreCase(user2);
    }
} 