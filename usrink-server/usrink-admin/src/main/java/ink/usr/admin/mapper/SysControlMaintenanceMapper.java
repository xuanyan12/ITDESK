package ink.usr.admin.mapper;

import ink.usr.admin.dao.DTO.SysControlMaintenanceQueryDTO;
import ink.usr.admin.dao.VO.SysControlMaintenanceVO;
import ink.usr.common.model.mysql.SysControlMaintenanceModel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysControlMaintenanceMapper {

    // 获取电脑修改记录列表
    List<SysControlMaintenanceVO> getControlMaintenanceList(SysControlMaintenanceQueryDTO queryDTO);

    // 获取电脑修改记录总数
    int getControlMaintenanceCount(SysControlMaintenanceQueryDTO queryDTO);
    
    // 插入维修记录
    int insertMaintenanceRecord(SysControlMaintenanceModel maintenanceRecord);
}
