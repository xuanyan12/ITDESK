package ink.usr.admin.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;


@Mapper
public interface SysTokenMapper {

    @Update("update sys_approval_token set used = 1 where flowId = #{flowId}")
    int updateTokenByFlowId(Long flowId);
}
