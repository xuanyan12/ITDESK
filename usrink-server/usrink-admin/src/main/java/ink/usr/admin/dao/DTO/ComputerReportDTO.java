package ink.usr.admin.dao.DTO;

import lombok.Data;

/**
 * 电脑报表查询参数
 */
@Data
public class ComputerReportDTO {
    
    /**
     * 公司筛选（SGCS/SES/SGCC）
     */
    private String company;
    
    /**
     * 设备类型筛选
     */
    private String deviceClass;
    
    /**
     * 年限筛选范围
     */
    private String ageRange;
    
    /**
     * 是否只统计内部员工（默认true）
     */
    private Boolean internalOnly = true;

    /**
     * 员工类型筛选
     * 可选值："Internal User" | "External User" | "All"
     * 若填写该字段，则优先生效；当为"All"时，不限定pcClass；
     * 若该字段为空，则回退使用 internalOnly 逻辑。
     */
    private String employeeType;

    /**
     * 统计参考日期（仅用于“设备类型年限分布”和“详细统计数据”）
     * 格式：yyyy-MM-dd；为空时按当前日期
     */
    private String statDate;
}