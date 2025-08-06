package ink.usr.admin.service.Impl;

import ink.usr.admin.dao.DTO.ComputerReportDTO;
import ink.usr.admin.dao.VO.ComputerReportVO;
import ink.usr.admin.mapper.ComputerReportMapper;
import ink.usr.admin.service.ComputerReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 电脑报表服务实现
 */
@Service
@Slf4j
public class ComputerReportServiceImpl implements ComputerReportService {
    
    @Autowired
    private ComputerReportMapper computerReportMapper;
    
    @Override
    public ComputerReportVO getComputerReport(ComputerReportDTO dto) {
        try {
            log.info("获取电脑报表数据，参数: {}", dto);
            
            ComputerReportVO result = new ComputerReportVO();
            
            // 获取按公司和设备类型统计的数据
            List<ComputerReportVO.CompanyDeviceStatistics> companyDeviceStats = 
                computerReportMapper.getCompanyDeviceStatistics(dto);
            result.setCompanyDeviceStats(companyDeviceStats);
            
            // 获取按年限统计的数据
            List<ComputerReportVO.AgeRangeStatistics> ageRangeStats = 
                computerReportMapper.getAgeRangeStatistics(dto);
            result.setAgeRangeStats(ageRangeStats);
            
            log.info("电脑报表数据获取成功，公司设备统计: {} 条，年限统计: {} 条", 
                companyDeviceStats.size(), ageRangeStats.size());
            
            return result;
            
        } catch (Exception e) {
            log.error("获取电脑报表数据失败", e);
            throw new RuntimeException("获取电脑报表数据失败: " + e.getMessage());
        }
    }
}