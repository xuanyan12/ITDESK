package ink.usr.admin.mapper;

import ink.usr.common.model.mysql.SysAnalyticsLogModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * SEG OneLink 页面分析日志Mapper
 */
@Mapper
public interface SysAnalyticsLogMapper {
    
    /**
     * 插入分析日志
     */
    int insertAnalyticsLog(SysAnalyticsLogModel model);
    
    /**
     * 获取页面浏览量统计
     */
    Long getPageViewCount(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    /**
     * 获取系统点击量统计
     */
    Long getSystemClickCount(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    

    
    /**
     * 获取热门系统点击排行
     */
    List<Map<String, Object>> getPopularSystemsRanking(@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("limit") Integer limit);
    
    /**
     * 获取访问IP统计
     */
    Long getUniqueVisitorCount(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    /**
     * 获取日访问量趋势
     */
    List<Map<String, Object>> getDailyTrend(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    /**
     * 获取系统点击分布
     */
    List<Map<String, Object>> getSystemDistribution(@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("limit") Integer limit);
} 