package ink.usr.common.model.mysql;

import lombok.Data;
import java.util.Date;

/**
 * SEG OneLink 页面分析日志实体类
 */
@Data
public class SysAnalyticsLogModel {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * IP地址
     */
    private String ipAddress;
    
    /**
     * 用户代理信息
     */
    private String userAgent;
    
    /**
     * 事件类型：page_view, system_click
     */
    private String eventType;
    
    /**
     * 系统名称（点击事件时记录）
     */
    private String systemName;
    
    /**
     * 系统URL（点击事件时记录）
     */
    private String systemUrl;
    

    
    /**
     * 创建时间
     */
    private Date createTime;
} 