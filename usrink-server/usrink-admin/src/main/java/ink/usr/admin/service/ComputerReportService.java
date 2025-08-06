package ink.usr.admin.service;

import ink.usr.admin.dao.DTO.ComputerReportDTO;
import ink.usr.admin.dao.VO.ComputerReportVO;

/**
 * 电脑报表服务接口
 */
public interface ComputerReportService {
    
    /**
     * 获取电脑统计报表数据
     * @param dto 查询参数
     * @return 报表数据
     */
    ComputerReportVO getComputerReport(ComputerReportDTO dto);
}