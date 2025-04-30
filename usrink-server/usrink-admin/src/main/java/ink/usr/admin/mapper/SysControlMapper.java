package ink.usr.admin.mapper;

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

    boolean insertSysControl(SysControlModel sysControlModel);
}
