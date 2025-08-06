package ink.usr.admin.mapper;

import ink.usr.admin.dao.DTO.ComputerReportDTO;
import ink.usr.admin.dao.VO.ComputerReportVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 电脑报表Mapper接口
 */
@Mapper
public interface ComputerReportMapper {
    
    /**
     * 获取按公司和设备类型统计的数据
     * @param dto 查询参数
     * @return 统计结果
     */
    List<ComputerReportVO.CompanyDeviceStatistics> getCompanyDeviceStatistics(ComputerReportDTO dto);
    
    /**
     * 获取按年限统计的数据
     * @param dto 查询参数  
     * @return 统计结果
     */
    List<ComputerReportVO.AgeRangeStatistics> getAgeRangeStatistics(ComputerReportDTO dto);
}