package ink.usr.admin.mapper;

import ink.usr.common.model.mysql.SysOnelinkSystemModel;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * OneLink系统管理Mapper接口
 */
@Mapper
public interface SysOnelinkSystemMapper {

    /**
     * 查询系统列表
     */
    @Select("<script>" +
            "SELECT * FROM sys_onelink_system " +
            "WHERE 1=1 " +
            "<if test='categoryName != null and categoryName != \"\"'>" +
            "AND categoryName = #{categoryName} " +
            "</if>" +
            "<if test='isActive != null'>" +
            "AND isActive = #{isActive} " +
            "</if>" +
            "ORDER BY categoryName, sortOrder, systemId " +
            "</script>")
    List<SysOnelinkSystemModel> selectSystemList(@Param("categoryName") String categoryName,
                                                  @Param("isActive") Boolean isActive);

    /**
     * 查询所有分类
     */
    @Select("SELECT DISTINCT categoryName FROM sys_onelink_system ORDER BY categoryName")
    List<String> selectCategories();

    /**
     * 根据ID查询系统
     */
    @Select("SELECT * FROM sys_onelink_system WHERE systemId = #{systemId}")
    SysOnelinkSystemModel selectById(@Param("systemId") Long systemId);

    /**
     * 插入系统
     */
    @Insert("INSERT INTO sys_onelink_system (systemName, systemDescription, systemUrl, systemIcon, " +
            "categoryName, sortOrder, isActive, createdAt, updatedAt) " +
            "VALUES (#{systemName}, #{systemDescription}, #{systemUrl}, #{systemIcon}, " +
            "#{categoryName}, #{sortOrder}, #{isActive}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "systemId")
    int insertSystem(SysOnelinkSystemModel system);

    /**
     * 更新系统
     */
    @Update("UPDATE sys_onelink_system SET " +
            "systemName = #{systemName}, " +
            "systemDescription = #{systemDescription}, " +
            "systemUrl = #{systemUrl}, " +
            "systemIcon = #{systemIcon}, " +
            "categoryName = #{categoryName}, " +
            "sortOrder = #{sortOrder}, " +
            "isActive = #{isActive}, " +
            "updatedAt = NOW() " +
            "WHERE systemId = #{systemId}")
    int updateSystem(SysOnelinkSystemModel system);

    /**
     * 删除系统
     */
    @Delete("DELETE FROM sys_onelink_system WHERE systemId = #{systemId}")
    int deleteSystem(@Param("systemId") Long systemId);

    /**
     * 查询启用的系统列表
     */
    @Select("SELECT * FROM sys_onelink_system WHERE isActive = 1 ORDER BY categoryName, sortOrder, systemId")
    List<SysOnelinkSystemModel> selectActiveSystems();

    /**
     * 检查系统名称是否存在
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM sys_onelink_system " +
            "WHERE systemName = #{systemName} " +
            "<if test='systemId != null'>" +
            "AND systemId != #{systemId} " +
            "</if>" +
            "</script>")
    int countBySystemName(@Param("systemName") String systemName, @Param("systemId") Long systemId);
} 