package ink.usr.admin.service.Impl;

import com.github.pagehelper.Page;
import ink.usr.admin.dao.DTO.SysControlMaintenanceQueryDTO;
import ink.usr.admin.dao.VO.SysControlMaintenanceVO;
import ink.usr.admin.mapper.SysControlMaintenanceMapper;
import ink.usr.admin.service.SysControlMaintenanceService;
import ink.usr.common.core.domain.Dict;
import ink.usr.common.core.utils.PageUtil;
import ink.usr.common.model.mysql.SysControlMaintenanceModel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class SysControlMaintenanceServiceImpl implements SysControlMaintenanceService {

    @Autowired
    SysControlMaintenanceMapper sysControlMaintenanceMapper;

    @Override
    public Dict getSysControlMaintenance(SysControlMaintenanceQueryDTO queryDTO){
        log.info("查询电脑修改记录，参数: {}", queryDTO);

        // 设置分页参数
        if (queryDTO.getPageNum() <= 0) {
            queryDTO.setPageNum(1);
        }
        if (queryDTO.getPageSize() <= 0) {
            queryDTO.setPageSize(10);
        }

        // 使用PageHelper进行分页
        Page<Object> pages = PageUtil.startPage((int)queryDTO.getPageNum(), (int)queryDTO.getPageSize());

        try {
            List<SysControlMaintenanceVO> recordList = sysControlMaintenanceMapper.getControlMaintenanceList(queryDTO);
            log.info("查询到记录数量: {}, 总记录数: {}", recordList.size(), pages.getTotal());

            Dict result = Dict.create()
                    .set("list", recordList)
                    .set("total", pages.getTotal())
                    .set("pageNum", queryDTO.getPageNum())
                    .set("pageSize", queryDTO.getPageSize());

            return result;
        } catch (Exception e) {
            log.error("查询电脑修改记录失败", e);
            throw new RuntimeException("查询电脑修改记录失败: " + e.getMessage());
        }
    }

    @Override
    public int selectCountNum(SysControlMaintenanceQueryDTO sysControlMaintenanceQueryDTO) {
        int controlMaintenanceCount = sysControlMaintenanceMapper.getControlMaintenanceCount(sysControlMaintenanceQueryDTO);
        return controlMaintenanceCount;
    }
    
    @Override
    @Transactional
    public boolean saveMaintenanceRecord(String orderNumber, String ciName, String applicant, 
                                       String maintenanceCategory, String problemDescription,
                                       String maintenanceResult, String maintenanceRecord, 
                                       String maintenanceRemark, String maintenanceTime) {
        try {
            SysControlMaintenanceModel record = new SysControlMaintenanceModel();
            record.setOrderNumber(orderNumber);
            record.setCiName(ciName);
            record.setApplicant(applicant);
            record.setMaintenanceCategory(maintenanceCategory);
            record.setProblemDescription(problemDescription);
            record.setMaintenanceResult(maintenanceResult);
            record.setMaintenanceRecord(maintenanceRecord);
            record.setMaintenanceRemark(maintenanceRemark);
            record.setMaintenanceTime(maintenanceTime);
            record.setStatus(1); // 默认有效状态
            
            int result = sysControlMaintenanceMapper.insertMaintenanceRecord(record);
            log.info("保存维修记录: 订单号={}, 电脑名={}, 申请人={}, 结果={}", 
                orderNumber, ciName, applicant, result > 0 ? "成功" : "失败");
            return result > 0;
        } catch (Exception e) {
            log.error("保存维修记录失败: 订单号={}, 电脑名={}, 申请人={}", 
                orderNumber, ciName, applicant, e);
            return false;
        }
    }
}
