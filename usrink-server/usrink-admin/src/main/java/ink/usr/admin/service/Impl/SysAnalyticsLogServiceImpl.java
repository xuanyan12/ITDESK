package ink.usr.admin.service.Impl;

import ink.usr.admin.dao.DTO.SysAnalyticsLogDTO;
import ink.usr.admin.mapper.SysAnalyticsLogMapper;
import ink.usr.admin.service.SysAnalyticsLogService;
import ink.usr.common.model.mysql.SysAnalyticsLogModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SEG OneLink 页面分析日志Service实现类
 */
@Service
@Slf4j
public class SysAnalyticsLogServiceImpl implements SysAnalyticsLogService {
    
    @Autowired
    private SysAnalyticsLogMapper sysAnalyticsLogMapper;
    
    @Override
    public boolean recordAnalyticsLog(SysAnalyticsLogDTO dto, String ipAddress, String userAgent) {
        try {
            // 非空验证
            if (dto == null) {
                log.warn("分析日志DTO为空");
                return false;
            }
            
            if (dto.getSessionId() == null || dto.getSessionId().trim().isEmpty()) {
                log.warn("会话ID为空");
                return false;
            }
            
            if (dto.getEventType() == null || dto.getEventType().trim().isEmpty()) {
                log.warn("事件类型为空");
                return false;
            }
            
            if (ipAddress == null || ipAddress.trim().isEmpty()) {
                log.warn("IP地址为空");
                return false;
            }
            
            // 验证事件类型是否有效（只支持页面浏览和系统点击）
            String eventType = dto.getEventType().trim();
            if (!eventType.equals("page_view") && !eventType.equals("system_click")) {
                log.warn("无效的事件类型: {}", eventType);
                return false;
            }
            
            // 对于系统点击事件，系统名称不能为空
            if ("system_click".equals(eventType)) {
                if (dto.getSystemName() == null || dto.getSystemName().trim().isEmpty()) {
                    log.warn("系统点击事件的系统名称为空");
                    return false;
                }
            }
            
            SysAnalyticsLogModel model = new SysAnalyticsLogModel();
            
            // 设置会话ID（限制长度64）
            String sessionId = dto.getSessionId().trim();
            if (sessionId.length() > 64) {
                sessionId = sessionId.substring(0, 64);
            }
            model.setSessionId(sessionId);
            
            // 设置IP地址（限制长度45）
            String ip = ipAddress.trim();
            if (ip.length() > 45) {
                ip = ip.substring(0, 45);
            }
            model.setIpAddress(ip);
            
            // 设置用户代理（可能很长，但数据库字段是TEXT类型）
            model.setUserAgent(userAgent != null ? userAgent.trim() : "");
            
            // 设置事件类型
            model.setEventType(eventType);
            
            // 设置系统名称（限制长度100）
            if (dto.getSystemName() != null) {
                String systemName = dto.getSystemName().trim();
                if (systemName.length() > 100) {
                    systemName = systemName.substring(0, 100);
                }
                model.setSystemName(systemName);
            } else {
                model.setSystemName(null);
            }
            
            // 设置系统URL（限制长度500）
            if (dto.getSystemUrl() != null) {
                String systemUrl = dto.getSystemUrl().trim();
                if (systemUrl.length() > 500) {
                    systemUrl = systemUrl.substring(0, 500);
                }
                model.setSystemUrl(systemUrl);
            } else {
                model.setSystemUrl(null);
            }
            
            // 不再记录停留时间（数据库表中也没有此字段）
            model.setCreateTime(new Date());
            
            int result = sysAnalyticsLogMapper.insertAnalyticsLog(model);
            return result > 0;
        } catch (Exception e) {
            log.error("记录分析日志失败", e);
            return false;
        }
    }
    
    @Override
    public Map<String, Object> getAnalyticsStatistics(Date startDate, Date endDate) {
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            log.info("开始获取统计数据，日期范围: {} 到 {}", startDate, endDate);
            
            // 页面浏览量
            Long pageViewCount = sysAnalyticsLogMapper.getPageViewCount(startDate, endDate);
            log.info("页面浏览量查询结果: {}", pageViewCount);
            statistics.put("pageViewCount", pageViewCount != null ? pageViewCount : 0L);
            
            // 系统点击量
            Long systemClickCount = sysAnalyticsLogMapper.getSystemClickCount(startDate, endDate);
            log.info("系统点击量查询结果: {}", systemClickCount);
            statistics.put("systemClickCount", systemClickCount != null ? systemClickCount : 0L);
            
            // 不再统计平均停留时间
            
            // 独立访客数
            Long uniqueVisitorCount = sysAnalyticsLogMapper.getUniqueVisitorCount(startDate, endDate);
            log.info("独立访客数查询结果: {}", uniqueVisitorCount);
            statistics.put("uniqueVisitorCount", uniqueVisitorCount != null ? uniqueVisitorCount : 0L);
            
            log.info("最终统计结果: {}", statistics);
            
        } catch (Exception e) {
            log.error("获取统计数据失败", e);
        }
        
        return statistics;
    }
    
    @Override
    public List<Map<String, Object>> getPopularSystemsRanking(Date startDate, Date endDate, Integer limit) {
        try {
            return sysAnalyticsLogMapper.getPopularSystemsRanking(startDate, endDate, limit);
        } catch (Exception e) {
            log.error("获取热门系统排行失败", e);
            return null;
        }
    }
    
    @Override
    public List<Map<String, Object>> getDailyTrend(Date startDate, Date endDate) {
        try {
            return sysAnalyticsLogMapper.getDailyTrend(startDate, endDate);
        } catch (Exception e) {
            log.error("获取日访问量趋势失败", e);
            return null;
        }
    }
    
    @Override
    public List<Map<String, Object>> getSystemDistribution(Date startDate, Date endDate, Integer limit) {
        try {
            return sysAnalyticsLogMapper.getSystemDistribution(startDate, endDate, limit);
        } catch (Exception e) {
            log.error("获取系统点击分布失败", e);
            return null;
        }
    }
} 