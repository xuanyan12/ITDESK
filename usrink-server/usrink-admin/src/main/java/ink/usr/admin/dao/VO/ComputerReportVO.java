package ink.usr.admin.dao.VO;

import lombok.Data;
import java.util.List;

/**
 * 电脑报表视图对象
 */
@Data
public class ComputerReportVO {
    
    /**
     * 按公司和设备类型统计的数据
     */
    private List<CompanyDeviceStatistics> companyDeviceStats;
    
    /**
     * 按年限统计的数据
     */
    private List<AgeRangeStatistics> ageRangeStats;
    
    /**
     * 一人多台电脑的用户数量（同一用户名同时分配/在用的电脑数量>1）
     */
    private Integer multiDeviceUserCount;

    /**
     * 各季度待报废（达到6年）设备数量
     */
    private List<QuarterScrapStatistics> quarterScrapStats;
    
    /**
     * 各公司在用数量按年份统计
     */
    private List<YearlyInUseStatistics> yearlyInUseStats;
    
    /**
     * 公司和设备类型统计
     */
    @Data
    public static class CompanyDeviceStatistics {
        private String company;                    // 公司
        private String deviceClass;               // 设备类型
        private Integer totalCount;               // 总数量
        private Integer age0to1;                  // 0-1年
        private Integer age1to2;                  // 1-2年
        private Integer age2to3;                  // 2-3年
        private Integer age3to4;                  // 3-4年
        private Integer age4to5;                  // 4-5年
        private Integer age5to6;                  // 5-6年
        private Integer age6to7;                  // 6-7年
        private Integer age7to8;                  // 7-8年
        private Integer age8plus;                 // 8年以上
        private Double avgAge;                    // 平均年限
    }
    
    /**
     * 年限范围统计
     */
    @Data
    public static class AgeRangeStatistics {
        private String ageRange;                  // 年限范围
        private Integer standardNotebook;         // Standard Notebook数量
        private Integer performanceNotebook;      // Performance Notebook数量
        private Integer standardDesktop;          // Standard Desktop数量
        private Integer performanceDesktop;       // Performance Desktop数量
        private Integer total;                    // 总计
    }

    /**
     * 季度待报废统计
     */
    @Data
    public static class QuarterScrapStatistics {
        private String quarter;                   // 形如 2025Q1
        private Integer total;                    // 数量
    }

    /**
     * 按年份统计在用数量（按公司分组，四类设备分列）
     */
    @Data
    public static class YearlyInUseStatistics {
        private String company;                   // 公司
        private Integer year;                     // 年份
        private Integer standardNotebook;         // Standard Notebook数量
        private Integer performanceNotebook;      // Performance Notebook数量
        private Integer standardDesktop;          // Standard Desktop数量
        private Integer performanceDesktop;       // Performance Desktop数量
        private Integer total;                    // 合计
    }
}