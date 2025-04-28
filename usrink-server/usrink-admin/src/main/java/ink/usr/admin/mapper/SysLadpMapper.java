package ink.usr.admin.mapper;

import ink.usr.common.model.mysql.SysLadpUserModel;
import ink.usr.common.model.mysql.SysUserModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysLadpMapper {

    boolean deleteAllInfos();

    boolean linkLDAPRefreshAllInfo(List<SysLadpUserModel> persons);

    @Select("select * from sys_user")
    List<SysUserModel> getAllLadpUsers();

    void updateLadpUser(SysUserModel user);

    void batchInsertLadpUsers(List<SysUserModel> toInsert);

    void batchDeleteLadpUsers(List<String> userNamesToDelete);
}
