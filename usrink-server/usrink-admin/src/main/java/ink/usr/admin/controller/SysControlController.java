package ink.usr.admin.controller;

import ink.usr.admin.dao.DTO.SysControlDTO;
import ink.usr.admin.dao.VO.SysControlVO;
import ink.usr.admin.service.SysControlService;
import ink.usr.common.core.domain.Dict;
import ink.usr.common.core.domain.Res;
import ink.usr.common.model.mysql.SysControlModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

@RestController
@Slf4j
@RequestMapping("/sysControl")
public class SysControlController {

    @Autowired
    private SysControlService sysControlService;

    /**
     * 获取电脑设备列表
     * @param sysControlModel
     * @return
     */
    @PostMapping ("/selectSysControlList")
    @RequiresPermissions("sys:device:control:select")
    public Res selectSysControlList(@RequestBody SysControlDTO sysControlModel){
        // 设置limit的偏移量
        long pageNumSize = (sysControlModel.getPageNum() - 1) * sysControlModel.getPageSize();
        sysControlModel.setPageNum(pageNumSize);
        List<SysControlModel> controlList = sysControlService.selectSysControlList(sysControlModel);
        SysControlVO sysControlVO = new SysControlVO();
        sysControlVO.setSysControlModelLists(controlList);
        int countNum = sysControlService.selectCountNum(sysControlModel);
        sysControlVO.setTotal(countNum);
        sysControlVO.setPageNum(sysControlModel.getPageNum());
        sysControlVO.setPageSize(sysControlModel.getPageSize());
        return Res.success(sysControlVO);
    }

    /**
     * 更新电脑
     * @param sysControlModel
     * @return
     */
    @RequestMapping("/updateSysControl")
    @RequiresPermissions("sys:device:control:update")
    public Res updateSysControl(@RequestBody SysControlModel sysControlModel){
        boolean flag = sysControlService.updateSysControl(sysControlModel);
        return Res.success(flag);
    }

    /**
     * 删除电脑
     * @param id
     * @return
     */
    @RequestMapping("/deleteSysControl")
    @RequiresPermissions("sys:device:control:delete")
    public Res deleteSysControl(long id){
        boolean flag = sysControlService.deleteSysControl(id);
        return Res.success(flag);
    }

    /**
     * 通过userName获取用户的 internal computer
     * @param userName
     * @return
     */
    @RequestMapping("/getInternalComputerByUserName")
    public Res getInternalComputerByUserName(@RequestParam("userName") String userName) throws Exception {
        SysControlModel sysControlModel = null;
        try {
            sysControlModel = sysControlService.getInternalComputerByUserName(userName);
        }catch (Exception e){
            throw new Exception(e);
        }

        return Res.success(sysControlModel);
    }

    /**
     * 通过userName获取用户的电脑名称列表
     * @param userName
     * @return
     */
    @RequestMapping("/getComputerListByUserName")
    public Res getComputerListByUserName(@RequestParam("userName") String userName) throws Exception {
        List<SysControlModel> sysControlModelList = null;
        Dict result = null;
        try {
            sysControlModelList = sysControlService.getComputerListByUserName(userName);
            result = Dict.create()
                    .set("list", sysControlModelList);
        }catch (Exception e){
            throw new Exception(e);
        }

        return Res.success(result);
    }

    /**
     * 通过电脑名获取电脑信息
     * @param ciName
     * @return
     */
    @RequestMapping("/getComputerInfoByCiName")
    public Res getComputerInfoByCiName(@RequestParam("ciName") String ciName){
        SysControlModel sysControlModel = sysControlService.getComputerInfoByCiName(ciName);
        return Res.success(sysControlModel);
    }

    /**
     * 通过提取上传的excel文件中的数据来插入数据
     * @return
     */
    @RequestMapping("/importComputerByExcel")
    public Res importComputerByExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Res.error("上传文件不能为空");
        }
        
        try {
            // 获取文件名
            String fileName = file.getOriginalFilename();
            log.info("开始导入电脑信息, 文件名: {}", fileName);
            
            // 根据文件扩展名创建不同的Workbook
            Workbook workbook;
            if (fileName.endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(file.getInputStream());
            } else if (fileName.endsWith(".xls")) {
                workbook = new HSSFWorkbook(file.getInputStream());
            } else {
                return Res.error("文件格式不正确，请使用Excel文件(.xlsx或.xls)");
            }
            
            // 获取第一个工作表
            Sheet sheet = workbook.getSheetAt(0);
            
            // 获取Excel的总行数
            int rowCount = sheet.getPhysicalNumberOfRows();
            if (rowCount <= 1) {
                workbook.close();
                return Res.error("Excel文件为空或只有表头");
            }
            log.info("Excel文件共有 {} 行数据", rowCount - 1); // 减去表头
            
            // 定义必须的列名
            Set<String> requiredHeaders = new HashSet<>(Arrays.asList(
                "pcStatus", "ciName", "serialNumber", "deviceClass", "manufacture", "modelOrVersion", 
                "ntAccount", "pcClass", "comment", "wbsNum", "vendor", "lifeCycleStart", 
                "company", "hardwareStatus", "cpu", "memory", "disk", "graphic", 
                "yrsToDay", "pr", "po", "price"
            ));
            
            // 获取表头并验证必须的列
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                workbook.close();
                return Res.error("Excel文件格式错误,请先下载模板,填入数据后上传");
            }
            
            // 存储实际的表头和索引的映射
            Map<String, Integer> headerIndexMap = new HashMap<>();
            Set<String> actualHeaders = new HashSet<>();
            
            // 提取表头并验证
            for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++) {
                String headerName = getCellValueAsString(headerRow.getCell(i)).trim();
                if (!headerName.isEmpty()) {
                    headerIndexMap.put(headerName, i);
                    actualHeaders.add(headerName);
                }
            }
            
            // 验证所有必需的表头是否存在
            if (!actualHeaders.containsAll(requiredHeaders)) {
                Set<String> missingHeaders = new HashSet<>(requiredHeaders);
                missingHeaders.removeAll(actualHeaders);
                workbook.close();
                return Res.error("Excel文件格式错误：缺少必要的列: " + String.join(", ", missingHeaders));
            }
            
            // 验证是否有多余的表头
            if (!requiredHeaders.containsAll(actualHeaders)) {
                Set<String> extraHeaders = new HashSet<>(actualHeaders);
                extraHeaders.removeAll(requiredHeaders);
                workbook.close();
                return Res.error("Excel文件格式错误：包含无效的列: " + String.join(", ", extraHeaders));
            }
            
            // 找到ciName列的索引
            int ciNameColumnIndex = headerIndexMap.get("ciName");
            
            log.info("开始处理Excel数据，进行更新或插入操作:");
            int updateCount = 0;
            int insertCount = 0;
            int skipCount = 0;
            
            // 从第二行开始读取数据（跳过表头）
            for (int i = 1; i < rowCount; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                
                // 获取ciName
                Cell ciNameCell = row.getCell(ciNameColumnIndex);
                String ciName = getCellValueAsString(ciNameCell);
                
                if (ciName == null || ciName.trim().isEmpty()) {
                    log.info("第 {} 行: ciName为空，跳过", i + 1);
                    skipCount++;
                    continue;
                }
                
                // 创建电脑对象并填充数据
                SysControlModel computerModel = new SysControlModel();
                
                // 遍历所有表头，将数据填充到电脑对象中
                for (Map.Entry<String, Integer> entry : headerIndexMap.entrySet()) {
                    String headerName = entry.getKey();
                    int columnIndex = entry.getValue();
                    String cellValue = getCellValueAsString(row.getCell(columnIndex));
                    
                    // 只处理非空值
                    if (cellValue != null && !cellValue.trim().isEmpty()) {
                        setProperty(computerModel, headerName, cellValue);
                    }
                }
                
                try {
                    // 查询数据库中是否存在该ciName（排除pcStatus为Scrapped的记录）
                    List<SysControlModel> existingComputers = sysControlService.selectNonScrappedComputersByCiName(ciName);
                    
                    if (existingComputers != null && !existingComputers.isEmpty()) {
                        // 存在记录，执行更新操作
                        SysControlModel existingComputer = existingComputers.get(0);
                        computerModel.setId(existingComputer.getId()); // 设置ID以便更新
                        
                        boolean updateResult = sysControlService.updateSysControl(computerModel);
                        if (updateResult) {
                            updateCount++;
                            log.info("第 {} 行: ciName={}, 更新成功", i + 1, ciName);
                        } else {
                            log.error("第 {} 行: ciName={}, 更新失败", i + 1, ciName);
                        }
                    } else {
                        // 不存在记录，执行插入操作
                        // 首先检查pcStatus是否为Scrapped，如果是则跳过
                        String pcStatus = computerModel.getPcStatus();
                        if (pcStatus != null && "Scrapped".equalsIgnoreCase(pcStatus)) {
                            log.info("第 {} 行: ciName={}, pcStatus为Scrapped，跳过插入", i + 1, ciName);
                            skipCount++;
                            continue;
                        }
                        
                        // 执行插入操作
                        boolean insertResult = sysControlService.insertSysControl(computerModel);
                        if (insertResult) {
                            insertCount++;
                            log.info("第 {} 行: ciName={}, 插入成功", i + 1, ciName);
                        } else {
                            log.error("第 {} 行: ciName={}, 插入失败", i + 1, ciName);
                        }
                    }
                } catch (Exception e) {
                    log.error("处理ciName={}的记录时出错: {}", ciName, e.getMessage());
                }
            }
            
            // 关闭工作簿
            workbook.close();
            
            log.info("导入完成，总共更新 {} 条记录，插入 {} 条记录，跳过 {} 条记录", updateCount, insertCount, skipCount);
            return Res.success("导入成功，总共更新 " + updateCount + " 条记录，插入 " + insertCount + " 条记录，跳过 " + skipCount + " 条记录");
        } catch (Exception e) {
            log.error("导入电脑信息失败", e);
            return Res.error("导入失败：" + e.getMessage());
        }
    }

    /**
     * 获取单元格的值并转换为字符串
     * @param cell Excel单元格
     * @return 单元格的字符串值
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        String cellValue = "";
        
        switch (cell.getCellType()) {
            case STRING:
                cellValue = cell.getStringCellValue();
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    cellValue = cell.getLocalDateTimeCellValue().toString();
                } else {
                    cellValue = String.valueOf(cell.getNumericCellValue());
                }
                break;
            case BOOLEAN:
                cellValue = String.valueOf(cell.getBooleanCellValue());
                break;
            case FORMULA:
                try {
                    cellValue = String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    cellValue = cell.getStringCellValue();
                }
                break;
            case BLANK:
                cellValue = "";
                break;
            default:
                cellValue = "";
        }
        
        return cellValue;
    }

    /**
     * 根据列名设置对象属性值
     * @param model 对象
     * @param propertyName 属性名
     * @param value 属性值
     */
    private void setProperty(SysControlModel model, String propertyName, String value) {
        try {
            // 将Excel表头转换为对象属性名称（可根据实际情况调整）
            switch(propertyName.toLowerCase()) {
                case "ciname":
                case "电脑名":
                case "电脑名称":
                    model.setCiName(value);
                    break;
                case "pcstatus":
                case "电脑状态":
                    model.setPcStatus(value);
                    break;
                case "serialnumber":
                case "序列号":
                case "电脑序列号":
                    model.setSerialNumber(value);
                    break;
                case "deviceclass":
                case "设备类型":
                    model.setDeviceClass(value);
                    break;
                case "manufacture":
                case "制造商":
                    model.setManufacture(value);
                    break;
                case "modelorversion":
                case "电脑型号":
                    model.setModelOrVersion(value);
                    break;
                case "ntaccount":
                case "nt账号":
                    model.setNtAccount(value);
                    break;
                case "pcclass":
                case "电脑归属情况":
                    model.setPcClass(value);
                    break;
                case "comment":
                case "备注":
                    model.setComment(value);
                    break;
                case "lastname":
                case "姓":
                    model.setLastName(value);
                    break;
                case "firstname":
                case "名":
                    model.setFirstName(value);
                    break;
                case "emailaddress":
                case "邮箱地址":
                    model.setEmailAddress(value);
                    break;
                case "department":
                case "所属部门":
                    model.setDepartment(value);
                    break;
                case "telephone":
                case "电话号码":
                    model.setTelephone(value);
                    break;
                case "costcenter":
                case "成本中心":
                    model.setCostCenter(value);
                    break;
                case "lifecyclestart":
                case "出厂时间":
                    model.setLifeCycleStart(value);
                    break;
                case "cpu":
                    model.setCpu(value);
                    break;
                case "memory":
                case "内存":
                    model.setMemory(value);
                    break;
                case "disk":
                case "硬盘":
                    model.setDisk(value);
                    break;
                case "graphic":
                case "显卡":
                    model.setGraphic(value);
                    break;
                case "hardwarestatus":
                case "硬件状态":
                    model.setHardwareStatus(value);
                    break;
                case "pr":
                case "下单号":
                    model.setPr(value);
                    break;
                case "po":
                case "订单号":
                    model.setPo(value);
                    break;
                case "vendor":
                case "供应商公司":
                    model.setVendor(value);
                    break;
                case "company":
                case "公司":
                    model.setCompany(value);
                    break;
                case "assetsno":
                case "wbs号":
                    model.setWbsNum(value);
                    break;
                case "price":
                case "价格":
                    model.setPrice(value);
                    break;
                case "temp":
                case "临时分配":
                    if ("是".equals(value) || "1".equals(value)) {
                        model.setTemp(1);
                    } else {
                        model.setTemp(0);
                    }
                    break;
            }
        } catch (Exception e) {
            log.error("设置属性{}时出错: {}", propertyName, e.getMessage());
        }
    }
}
