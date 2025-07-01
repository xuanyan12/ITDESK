package ink.usr.admin.mapper;

import ink.usr.admin.dao.DTO.SysControlAssignDTO;
import ink.usr.admin.dao.DTO.SysControlRecordDTO;
import ink.usr.admin.dao.DTO.SysControlRecordQueryDTO;
import ink.usr.admin.dao.VO.SysControlBillListVO;
import ink.usr.common.model.mysql.SysControlAssignModel;
import ink.usr.common.model.mysql.SysControlModel;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysControlMapper {


    List<SysControlModel> selectSysControlList(SysControlModel sysControlModel);


    int selectCountNum(SysControlModel sysControlModel);

    boolean updateSysControl(SysControlModel sysControlModel);

    @Delete("delete from sys_control where id = #{id}")
    boolean deleteSysControl(long id);

    @Select("SELECT * FROM sys_control WHERE ntAccount = #{userName} AND pcClass = 'Internal User' AND pcStatus = 'In Use'")
    SysControlModel getInternalComputerByUserName(String userName);

    @Select("select * from sys_control where ntAccount = #{userName} AND pcStatus = 'In Use'")
    List<SysControlModel> getComputerListByUserName(String userName);

    @Select("select * from sys_control where ciName = #{ciName}")
    SysControlModel getComputerInfoByCiName(String ciName);

    @Select("select * from sys_control where ciName = #{ciName} and pcStatus != 'Scrapped'")
    List<SysControlModel> selectNonScrappedComputersByCiName(String ciName);

    @Select("SELECT * FROM sys_control WHERE costCenter = #{costCenter} AND pcStatus = 'In Use'")
    List<SysControlModel> getComputerListByCostCenter(String costCenter);

    boolean insertSysControl(SysControlModel sysControlModel);

    List<SysControlModel> getComputerByInfo(SysControlAssignDTO sysControlAssignModel);

    void updateSysControlRecord(SysControlRecordDTO sysControlRecordDTO);
    
    // 获取电脑修改记录列表
    List<SysControlRecordDTO> getControlRecordList(SysControlRecordQueryDTO queryDTO);
    
    // 获取电脑修改记录总数
    int getControlRecordCount(SysControlRecordQueryDTO queryDTO);


}
