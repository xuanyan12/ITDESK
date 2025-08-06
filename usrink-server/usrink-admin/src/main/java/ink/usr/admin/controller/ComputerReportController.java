package ink.usr.admin.controller;

import ink.usr.admin.dao.DTO.ComputerReportDTO;
import ink.usr.admin.dao.VO.ComputerReportVO;
import ink.usr.admin.service.ComputerReportService;
import ink.usr.common.core.domain.Res;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 电脑报表控制器
 */
@RestController
@Slf4j
@RequestMapping("/computerReport")
public class ComputerReportController {
    
    @Autowired
    private ComputerReportService computerReportService;
    
    /**
     * 获取电脑统计报表
     * @param dto 查询参数
     * @return 报表数据
     */
    @PostMapping("/getReport")
    @RequiresPermissions("sys:computer:report:view")
    public Res getComputerReport(@RequestBody ComputerReportDTO dto) {
        try {
            log.info("获取电脑报表请求，参数: {}", dto);
            
            // 设置默认值
            if (dto == null) {
                dto = new ComputerReportDTO();
            }
            if (dto.getInternalOnly() == null) {
                dto.setInternalOnly(true);
            }
            
            ComputerReportVO result = computerReportService.getComputerReport(dto);
            
            return Res.success(result);
            
        } catch (Exception e) {
            log.error("获取电脑报表失败", e);
            return Res.error("获取电脑报表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取公司列表
     * @return 公司列表
     */
    @GetMapping("/getCompanies")
    @RequiresPermissions("sys:computer:report:view")
    public Res getCompanies() {
        try {
            // 返回固定的公司列表
            String[] companies = {"SGCS", "SES", "SGCC"};
            return Res.success(companies);
        } catch (Exception e) {
            log.error("获取公司列表失败", e);
            return Res.error("获取公司列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取设备类型列表
     * @return 设备类型列表
     */
    @GetMapping("/getDeviceTypes")
    @RequiresPermissions("sys:computer:report:view")
    public Res getDeviceTypes() {
        try {
            // 返回四种设备类型
            String[] deviceTypes = {
                "Standard Notebook", 
                "Performance Notebook", 
                "Standard Desktop", 
                "Performance Desktop"
            };
            return Res.success(deviceTypes);
        } catch (Exception e) {
            log.error("获取设备类型列表失败", e);
            return Res.error("获取设备类型列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取年限范围列表
     * @return 年限范围列表
     */
    @GetMapping("/getAgeRanges")
    @RequiresPermissions("sys:computer:report:view")
    public Res getAgeRanges() {
        try {
            String[] ageRanges = {"0-1","1-2", "2-3","3-4", "4-5","5-6", "6-7","7-8","8+",};
            return Res.success(ageRanges);
        } catch (Exception e) {
            log.error("获取年限范围列表失败", e);
            return Res.error("获取年限范围列表失败: " + e.getMessage());
        }
    }
}