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
}