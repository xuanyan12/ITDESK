package ink.usr.admin.dao.DTO;

import lombok.Data;

/**
 * SEG OneLink 页面分析日志DTO
 */
@Data
public class SysAnalyticsLogDTO {
    
    /**
     * 会话ID
     */
    private String sessionId;
    
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
    

} 