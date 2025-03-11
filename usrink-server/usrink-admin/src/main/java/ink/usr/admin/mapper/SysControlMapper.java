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
}
