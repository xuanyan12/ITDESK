package ink.usr.admin.mapper;

import ink.usr.admin.dao.DTO.SysControlDTO;
import ink.usr.admin.dao.VO.SysControlBillListVO;
import ink.usr.common.model.mysql.SysControlModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysControlAssetMapper {

    List<SysControlBillListVO> getCoputerAssetList(SysControlModel sysControlModel);

    /**
     * 查询电脑资产列表
     * @param sysControlDTO 查询条件
     * @return 资产列表
     */
    List<SysControlBillListVO> selectSysControlAssetList(SysControlDTO sysControlDTO);

    /**
     * 批量更新IFRS数据
     * @param dataList IFRS数据列表
     */
    void batchUpdateIfrsData(@Param("list") List<Map<String, Object>> dataList);

    /**
     * 批量更新PRC数据
     * @param dataList PRC数据列表
     */
    void batchUpdatePrcData(@Param("list") List<Map<String, Object>> dataList);
}
