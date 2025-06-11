package ink.usr.admin.controller;

import ink.usr.admin.dao.DTO.SysAnalyticsLogDTO;
import ink.usr.admin.service.SysAnalyticsLogService;
import ink.usr.common.core.domain.Dict;
import ink.usr.common.core.domain.Res;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * SEG OneLink 页面分析日志Controller
 */
@RestController
@Slf4j
@RequestMapping("/sysAnalyticsLog")
public class SysAnalyticsLogController {
    
    @Autowired
    private SysAnalyticsLogService sysAnalyticsLogService;
    
    /**
     * 记录分析日志
     */
    @PostMapping("/record")
    public Res recordAnalyticsLog(@RequestBody SysAnalyticsLogDTO dto, HttpServletRequest request) {
        try {
            // 基本参数验证
            if (dto == null) {
                return Res.error("请求参数不能为空");
            }
            
            // 获取客户端IP地址
            String ipAddress = getClientIpAddress(request);
            // 获取用户代理信息
            String userAgent = request.getHeader("User-Agent");
            
            boolean result = sysAnalyticsLogService.recordAnalyticsLog(dto, ipAddress, userAgent);
            
            if (result) {
                return Res.success("记录成功");
            } else {
                return Res.error("记录失败");
            }
        } catch (Exception e) {
            log.error("记录分析日志失败", e);
            return Res.error("记录失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取统计数据
     */
    @GetMapping("/statistics")
    public Res getAnalyticsStatistics(
            @RequestParam(value = "startDate", required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "endDate", required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        try {
            log.info("获取统计数据，开始日期: {}, 结束日期: {}", startDate, endDate);
            Map<String, Object> statistics = sysAnalyticsLogService.getAnalyticsStatistics(startDate, endDate);
            log.info("统计数据结果: {}", statistics);
            return Res.success(statistics);
        } catch (Exception e) {
            log.error("获取统计数据失败", e);
            return Res.error("获取统计数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取热门系统排行
     */
    @GetMapping("/popularSystems")
    public Res getPopularSystemsRanking(
            @RequestParam(value = "startDate", required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "endDate", required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        try {
            log.info("获取热门系统排行，开始日期: {}, 结束日期: {}, 限制: {}", startDate, endDate, limit);
            List<Map<String, Object>> ranking = sysAnalyticsLogService.getPopularSystemsRanking(startDate, endDate, limit);
            log.info("热门系统排行结果: {}", ranking);
            Dict result = Dict.create().set("list", ranking);
            return Res.success(result);
        } catch (Exception e) {
            log.error("获取热门系统排行失败", e);
            return Res.error("获取热门系统排行失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取日访问量趋势
     */
    @GetMapping("/dailyTrend")
    public Res getDailyTrend(
            @RequestParam(value = "startDate", required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "endDate", required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        try {
            log.info("获取日访问量趋势，开始日期: {}, 结束日期: {}", startDate, endDate);
            List<Map<String, Object>> dailyTrend = sysAnalyticsLogService.getDailyTrend(startDate, endDate);
            log.info("日访问量趋势结果: {}", dailyTrend);
            Dict result = Dict.create().set("list", dailyTrend);
            return Res.success(result);
        } catch (Exception e) {
            log.error("获取日访问量趋势失败", e);
            return Res.error("获取日访问量趋势失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取系统点击分布
     */
    @GetMapping("/systemDistribution")
    public Res getSystemDistribution(
            @RequestParam(value = "startDate", required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "endDate", required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam(value = "limit", defaultValue = "8") Integer limit) {
        try {
            log.info("获取系统点击分布，开始日期: {}, 结束日期: {}, 限制: {}", startDate, endDate, limit);
            List<Map<String, Object>> distribution = sysAnalyticsLogService.getSystemDistribution(startDate, endDate, limit);
            log.info("系统点击分布结果: {}", distribution);
            Dict result = Dict.create().set("list", distribution);
            return Res.success(result);
        } catch (Exception e) {
            log.error("获取系统点击分布失败", e);
            return Res.error("获取系统点击分布失败：" + e.getMessage());
        }
    }
    
    /**
     * 测试接口 - 获取所有数据总数
     */
    @GetMapping("/test/count")
    public Res getTestCount() {
        try {
            Map<String, Object> result = sysAnalyticsLogService.getAnalyticsStatistics(null, null);
            return Res.success(result);
        } catch (Exception e) {
            log.error("获取测试数据失败", e);
            return Res.error("获取测试数据失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        String remoteAddr = request.getRemoteAddr();
        
        // 将IPv6本地回环地址转换为IPv4格式
        if ("0:0:0:0:0:0:0:1".equals(remoteAddr) || "::1".equals(remoteAddr)) {
            return "127.0.0.1";
        }
        
        return remoteAddr;
    }
} 