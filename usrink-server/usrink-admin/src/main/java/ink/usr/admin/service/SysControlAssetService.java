package ink.usr.admin.service;

import ink.usr.admin.dao.DTO.SysControlDTO;
import ink.usr.admin.dao.VO.PageVO;
import ink.usr.admin.dao.VO.SysControlBillListVO;
import org.springframework.web.multipart.MultipartFile;

public interface SysControlAssetService {

    /**
     * 查询电脑资产列表
     * @param sysControlDTO 查询条件
     * @return 分页后的资产列表
     */
    PageVO<SysControlBillListVO> selectSysControlAssetList(SysControlDTO sysControlDTO);

    /**
     * 导入IFRS数据
     * @param file Excel文件
     * @throws Exception 导入异常
     */
    void importIfrsData(MultipartFile file) throws Exception;

    /**
     * 导入PRC数据
     * @param file Excel文件
     * @throws Exception 导入异常
     */
    void importPrcData(MultipartFile file) throws Exception;

}
