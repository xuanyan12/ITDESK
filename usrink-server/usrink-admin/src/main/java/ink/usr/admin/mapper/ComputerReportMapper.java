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

    /**
     * 统计一人多台电脑的用户数量
     * @param dto 查询参数
     * @return 用户数量（拥有>1台设备的用户数）
     */
    Integer getMultiDeviceUserCount(ComputerReportDTO dto);

    /**
     * 统计未来若干季度内达到6年的设备数量（按季度汇总）
     */
    List<ComputerReportVO.QuarterScrapStatistics> getQuarterScrapStatistics(ComputerReportDTO dto);

    /**
     * 各公司四类电脑在用数量按年份统计
     */
    List<ComputerReportVO.YearlyInUseStatistics> getYearlyInUseStatistics(ComputerReportDTO dto);
}