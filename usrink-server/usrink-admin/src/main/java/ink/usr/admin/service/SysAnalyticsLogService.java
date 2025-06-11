package ink.usr.admin.service;

import ink.usr.admin.dao.DTO.SysAnalyticsLogDTO;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * SEG OneLink 页面分析日志Service接口
 */
public interface SysAnalyticsLogService {
    
    /**
     * 记录分析日志
     */
    boolean recordAnalyticsLog(SysAnalyticsLogDTO dto, String ipAddress, String userAgent);
    
    /**
     * 获取统计数据
     */
    Map<String, Object> getAnalyticsStatistics(Date startDate, Date endDate);
    
    /**
     * 获取热门系统排行
     */
    List<Map<String, Object>> getPopularSystemsRanking(Date startDate, Date endDate, Integer limit);
    
    /**
     * 获取日访问量趋势
     */
    List<Map<String, Object>> getDailyTrend(Date startDate, Date endDate);
    
    /**
     * 获取系统点击分布
     */
    List<Map<String, Object>> getSystemDistribution(Date startDate, Date endDate, Integer limit);
} 