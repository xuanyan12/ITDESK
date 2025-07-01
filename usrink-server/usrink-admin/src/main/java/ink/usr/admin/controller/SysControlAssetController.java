package ink.usr.admin.controller;

import ink.usr.admin.dao.DTO.SysControlDTO;
import ink.usr.admin.dao.VO.PageVO;
import ink.usr.admin.dao.VO.SysControlBillListVO;
import ink.usr.admin.service.SysControlAssetService;
import ink.usr.common.core.domain.Res;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequestMapping("/sysControlAsset")
public class SysControlAssetController {

    @Autowired
    private SysControlAssetService sysControlAssetService;

    /**
     * 获取电脑资产列表
     * @param sysControlDTO 查询条件
     * @return 分页后的资产列表
     */
    @PostMapping("/selectSysControlAssetList")
    @RequiresPermissions("sys:device:control:select")
    public Res selectSysControlAssetList(SysControlDTO sysControlDTO) {
        try {
            PageVO<SysControlBillListVO> pageVO = sysControlAssetService.selectSysControlAssetList(sysControlDTO);
            return Res.success(pageVO);
        } catch (Exception e) {
            log.error("查询电脑资产列表失败", e);
            return Res.error("查询电脑资产列表失败：" + e.getMessage());
        }
    }

    /**
     * 导入IFRS数据
     * @param file Excel文件
     * @return 导入结果
     */
    @PostMapping("/importIfrs")
    @RequiresPermissions("sys:device:control:import")
    public Res importIfrs(@RequestParam("file") MultipartFile file) {
        try {
            sysControlAssetService.importIfrsData(file);
            return Res.success("IFRS数据导入成功");
        } catch (Exception e) {
            log.error("IFRS数据导入失败", e);
            return Res.error("IFRS数据导入失败：" + e.getMessage());
        }
    }

    /**
     * 导入PRC数据
     * @param file Excel文件
     * @return 导入结果
     */
    @PostMapping("/importPrc")
    @RequiresPermissions("sys:device:control:import")
    public Res importPrc(@RequestParam("file") MultipartFile file) {
        try {
            sysControlAssetService.importPrcData(file);
            return Res.success("PRC数据导入成功");
        } catch (Exception e) {
            log.error("PRC数据导入失败", e);
            return Res.error("PRC数据导入失败：" + e.getMessage());
        }
    }
}
