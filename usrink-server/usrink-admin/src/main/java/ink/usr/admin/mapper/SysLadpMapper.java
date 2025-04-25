package ink.usr.admin.mapper;

import ink.usr.common.model.mysql.SysLadpUserModel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysLadpMapper {

    boolean deleteAllInfos();

    boolean linkLDAPRefreshAllInfo(List<SysLadpUserModel> persons);
}
