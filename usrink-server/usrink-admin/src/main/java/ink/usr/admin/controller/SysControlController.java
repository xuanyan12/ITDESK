package ink.usr.admin.controller;

import com.github.pagehelper.Page;
import ink.usr.admin.dao.DTO.*;
import ink.usr.admin.dao.VO.SysControlAssignVO;
import ink.usr.admin.dao.VO.SysControlBillListVO;
import ink.usr.admin.dao.VO.SysControlVO;
import ink.usr.admin.mapper.SysControlMapper;
import ink.usr.admin.service.SysControlService;
import ink.usr.admin.service.SysUserService;
import ink.usr.common.core.domain.Dict;
import ink.usr.common.core.domain.Res;
import ink.usr.common.core.utils.PageUtil;
import ink.usr.common.model.mysql.SysControlAssignModel;
import ink.usr.common.model.mysql.SysControlModel;
import ink.usr.common.model.mysql.SysUserModel;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

@RestController
@Slf4j
@RequestMapping("/sysControl")
public class SysControlController {

    @Autowired
    private SysControlService sysControlService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysControlMapper sysControlMapper;


    /**
     * 获取电脑设备列表
     * @param sysControlModel
     * @return
     */
    @PostMapping ("/selectSysControlList")
    @RequiresPermissions("sys:device:control:select")
    public Res selectSysControlList(@RequestBody SysControlDTO sysControlModel){
        // 设置limit的偏移量
        int pageNumSize = (sysControlModel.getPageNum() - 1) * sysControlModel.getPageSize();
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
     * 获取审批人管理的所有设备信息（基于其负责的成本中心）
     * @param userName 用户名
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param costCenterFilter 成本中心筛选条件
     * @return 包含设备信息和成本中心标识的结果
     */
    @GetMapping("/getApproverManagedComputers")
    public Res getApproverManagedComputers(
            @RequestParam("userName") String userName,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "costCenterFilter", required = false) String costCenterFilter) throws Exception {
        try {
            Dict result = sysControlService.getApproverManagedComputers(userName, pageNum, pageSize, costCenterFilter);
            return Res.success(result);
        } catch (Exception e) {
            throw new Exception("获取审批人管理设备失败", e);
        }
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
                
                // 如果有ntAccount，从sys_user表获取用户信息并补充
                if (computerModel.getNtAccount() != null && !computerModel.getNtAccount().trim().isEmpty()) {
                    try {
                        SysUserModel userInfo = sysUserService.getUserInfoByUserName(computerModel.getNtAccount());
                        if (userInfo != null) {
                            // 设置用户邮箱
                            if (userInfo.getEmail() != null && !userInfo.getEmail().isEmpty()) {
                                computerModel.setEmailAddress(userInfo.getEmail());
                            }
                            
                            // 设置用户电话
                            if (userInfo.getPhone() != null && !userInfo.getPhone().isEmpty()) {
                                computerModel.setTelephone(userInfo.getPhone());
                            }
                            
                            // 设置用户部门
                            if (userInfo.getDepartment() != null && !userInfo.getDepartment().isEmpty()) {
                                computerModel.setDepartment(userInfo.getDepartment());
                            }
                            
                            // 设置成本中心
                            if (userInfo.getCostCenter() != null && !userInfo.getCostCenter().isEmpty()) {
                                computerModel.setCostCenter(userInfo.getCostCenter());
                            }
                            
                            // 解析userNick设置firstName和lastName
                            if (userInfo.getUserNick() != null && !userInfo.getUserNick().isEmpty()) {
                                String userNick = userInfo.getUserNick();
                                
                                // 处理FIXED-TERM前缀
                                if (userNick.startsWith("FIXED-TERM ")) {
                                    userNick = userNick.substring("FIXED-TERM ".length()).trim();
                                }
                                // 处理EXTERNAL前缀，格式如: "EXTERNAL Chen, Zhanjun (XXXX)"
                                else if (userNick.startsWith("EXTERNAL ")) {
                                    userNick = userNick.substring("EXTERNAL ".length()).trim();
                                }
                                // 处理[Blue color]前缀，格式如: "[Blue color] CHENG Xiaoyu (SES-MFO-STG3)"
                                else if (userNick.startsWith("[Blue color] ")) {
                                    userNick = userNick.substring("[Blue color] ".length()).trim();
                                }
                                
                                // 先分离出括号部分
                                int bracketIndex = userNick.indexOf(" (");
                                if (bracketIndex > 0) {
                                    userNick = userNick.substring(0, bracketIndex);
                                }
                                
                                // 分离姓和名
                                if (userInfo.getUserNick().startsWith("[Blue color] ")) {
                                    // [Blue color] 格式使用空格分隔，FirstName LastName形式
                                    String[] nameParts = userNick.split(" ", 2);
                                    if (nameParts.length == 2) {
                                        // 姓氏是第二部分
                                        computerModel.setLastName(nameParts[1]);
                                        // 名字是第一部分
                                        computerModel.setFirstName(nameParts[0]);
                                    } else {
                                        // 如果不符合预期格式，使用整个userNick作为lastName
                                        computerModel.setLastName(userNick);
                                    }
                                } else {
                                    // 普通格式使用逗号分隔，LastName, FirstName形式
                                    String[] nameParts = userNick.split(", ");
                                    if (nameParts.length == 2) {
                                        // 姓氏是第一部分
                                        computerModel.setLastName(nameParts[0]);
                                        // 名字是第二部分
                                        computerModel.setFirstName(nameParts[1]);
                                    } else {
                                        // 如果不符合预期格式，使用整个userNick作为lastName
                                        computerModel.setLastName(userNick);
                                    }
                                }
                                
                                log.info("解析用户 {} 姓名: 姓={}, 名={}", userInfo.getUserName(), 
                                        computerModel.getLastName(), computerModel.getFirstName());
                            }
                        } else {
                            log.warn("第 {} 行: ntAccount={} 在sys_user表中未找到对应用户", i + 1, computerModel.getNtAccount());
                        }
                    } catch (Exception e) {
                        log.error("处理ntAccount={}的用户信息时出错: {}", computerModel.getNtAccount(), e.getMessage());
                    }
                }
                
                try {
                    // 查询数据库中是否存在该ciName（排除pcStatus为Scrapped的记录）
                    List<SysControlModel> existingComputers = sysControlService.selectNonScrappedComputersByCiName(ciName);
                    
                    if (existingComputers != null && !existingComputers.isEmpty()) {
                        // 存在记录，执行更新操作 - 使用现有记录作为基础，只更新Excel中的非空字段
                        SysControlModel existingComputer = existingComputers.get(0);
                        
                        // 将Excel中的非空字段更新到现有记录上
                        updateExistingComputerWithExcelData(existingComputer, computerModel, headerIndexMap);
                        
                        boolean updateResult = sysControlService.updateSysControl(existingComputer);
                        if (updateResult) {
                            updateCount++;
                            log.info("第 {} 行: ciName={}, 更新成功", i + 1, ciName);
                        } else {
                            log.error("第 {} 行: ciName={}, 更新失败", i + 1, ciName);
                        }
                    } else {
                        // 不存在记录，执行插入操作
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
                    // 处理大数字，避免科学计数法格式
                    double numericValue = cell.getNumericCellValue();
                    // 如果数字是整数且很大，使用BigDecimal避免科学计数法
                    if (numericValue == Math.floor(numericValue) && Math.abs(numericValue) >= 1e7) {
                        // 使用BigDecimal确保大整数不被转换为科学计数法
                        java.math.BigDecimal bd = new java.math.BigDecimal(numericValue);
                        cellValue = bd.toPlainString();
                    } else {
                        cellValue = String.valueOf(numericValue);
                    }
                }
                break;
            case BOOLEAN:
                cellValue = String.valueOf(cell.getBooleanCellValue());
                break;
            case FORMULA:
                try {
                    // 对于公式，也使用相同的数字处理逻辑
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.floor(numericValue) && Math.abs(numericValue) >= 1e7) {
                        java.math.BigDecimal bd = new java.math.BigDecimal(numericValue);
                        cellValue = bd.toPlainString();
                    } else {
                        cellValue = String.valueOf(numericValue);
                    }
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
     * 将Excel中的非空字段更新到现有的数据库记录上
     * @param existingComputer 数据库中的现有记录
     * @param excelData Excel中读取的数据（只包含非空字段）
     * @param headerIndexMap Excel表头到列索引的映射，用于检查某些字段是否在Excel中存在
     */
    private void updateExistingComputerWithExcelData(SysControlModel existingComputer, SysControlModel excelData, Map<String, Integer> headerIndexMap) {
        // 只更新Excel中非空的字段
        if (excelData.getPcStatus() != null && !excelData.getPcStatus().trim().isEmpty()) {
            existingComputer.setPcStatus(excelData.getPcStatus());
        }
        if (excelData.getSerialNumber() != null && !excelData.getSerialNumber().trim().isEmpty()) {
            existingComputer.setSerialNumber(excelData.getSerialNumber());
        }
        if (excelData.getDeviceClass() != null && !excelData.getDeviceClass().trim().isEmpty()) {
            existingComputer.setDeviceClass(excelData.getDeviceClass());
        }
        if (excelData.getManufacture() != null && !excelData.getManufacture().trim().isEmpty()) {
            existingComputer.setManufacture(excelData.getManufacture());
        }
        if (excelData.getModelOrVersion() != null && !excelData.getModelOrVersion().trim().isEmpty()) {
            existingComputer.setModelOrVersion(excelData.getModelOrVersion());
        }
        if (excelData.getNtAccount() != null && !excelData.getNtAccount().trim().isEmpty()) {
            existingComputer.setNtAccount(excelData.getNtAccount());
        }
        if (excelData.getPcClass() != null && !excelData.getPcClass().trim().isEmpty()) {
            existingComputer.setPcClass(excelData.getPcClass());
        }
        if (excelData.getComment() != null && !excelData.getComment().trim().isEmpty()) {
            existingComputer.setComment(excelData.getComment());
        }
        if (excelData.getLastName() != null && !excelData.getLastName().trim().isEmpty()) {
            existingComputer.setLastName(excelData.getLastName());
        }
        if (excelData.getFirstName() != null && !excelData.getFirstName().trim().isEmpty()) {
            existingComputer.setFirstName(excelData.getFirstName());
        }
        if (excelData.getEmailAddress() != null && !excelData.getEmailAddress().trim().isEmpty()) {
            existingComputer.setEmailAddress(excelData.getEmailAddress());
        }
        if (excelData.getDepartment() != null && !excelData.getDepartment().trim().isEmpty()) {
            existingComputer.setDepartment(excelData.getDepartment());
        }
        if (excelData.getTelephone() != null && !excelData.getTelephone().trim().isEmpty()) {
            existingComputer.setTelephone(excelData.getTelephone());
        }
        if (excelData.getCostCenter() != null && !excelData.getCostCenter().trim().isEmpty()) {
            existingComputer.setCostCenter(excelData.getCostCenter());
        }
        if (excelData.getLifeCycleStart() != null && !excelData.getLifeCycleStart().trim().isEmpty()) {
            existingComputer.setLifeCycleStart(excelData.getLifeCycleStart());
        }
        if (excelData.getYrsToDay() != null && !excelData.getYrsToDay().trim().isEmpty()) {
            existingComputer.setYrsToDay(excelData.getYrsToDay());
        }
        if (excelData.getCpu() != null && !excelData.getCpu().trim().isEmpty()) {
            existingComputer.setCpu(excelData.getCpu());
        }
        if (excelData.getMemory() != null && !excelData.getMemory().trim().isEmpty()) {
            existingComputer.setMemory(excelData.getMemory());
        }
        if (excelData.getDisk() != null && !excelData.getDisk().trim().isEmpty()) {
            existingComputer.setDisk(excelData.getDisk());
        }
        if (excelData.getGraphic() != null && !excelData.getGraphic().trim().isEmpty()) {
            existingComputer.setGraphic(excelData.getGraphic());
        }
        if (excelData.getHardwareStatus() != null && !excelData.getHardwareStatus().trim().isEmpty()) {
            existingComputer.setHardwareStatus(excelData.getHardwareStatus());
        }
        if (excelData.getPr() != null && !excelData.getPr().trim().isEmpty()) {
            existingComputer.setPr(excelData.getPr());
        }
        if (excelData.getPo() != null && !excelData.getPo().trim().isEmpty()) {
            existingComputer.setPo(excelData.getPo());
        }
        if (excelData.getVendor() != null && !excelData.getVendor().trim().isEmpty()) {
            existingComputer.setVendor(excelData.getVendor());
        }
        if (excelData.getCompany() != null && !excelData.getCompany().trim().isEmpty()) {
            existingComputer.setCompany(excelData.getCompany());
        }
        if (excelData.getWbsNum() != null && !excelData.getWbsNum().trim().isEmpty()) {
            existingComputer.setWbsNum(excelData.getWbsNum());
        }
        if (excelData.getPrice() != null && !excelData.getPrice().trim().isEmpty()) {
            existingComputer.setPrice(excelData.getPrice());
        }
        // temp字段是int类型，只有当Excel中包含temp相关列时才更新
        if (headerIndexMap.containsKey("temp") || headerIndexMap.containsKey("临时分配")) {
            existingComputer.setTemp(excelData.getTemp());
        }
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
                case "telephone":
                case "电话号码":
                    model.setTelephone(value);
                    break;
                case "department":
                case "所属部门":
                    model.setDepartment(value);
                    break;
                case "costcenter":
                case "成本中心":
                    model.setCostCenter(value);
                    break;
                case "lifecyclestart":
                case "出厂时间":
                case "manufacture date":
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
                case "wbsnum":
                case "wbs号":
                case "资产号":
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

    @RequestMapping("/getComputerByInfo")
    public Res getComputerByInfo(@RequestBody SysControlAssignDTO sysControlAssignModel){
        Page<Object> pages = PageUtil.startPage();
        List<SysControlModel> sysControlModel = sysControlService.getComputerByInfo(sysControlAssignModel);
        Dict result = Dict.create()
                .set("list", sysControlModel)
                .set("total", pages.getTotal());
        return Res.success(result);
    }

    /**
     * 获取电脑修改记录列表
     * @param queryDTO 查询参数
     * @return 记录列表和分页信息
     */
    @PostMapping("/getControlRecordList")
    public Res getControlRecordList(@RequestBody SysControlRecordQueryDTO queryDTO) {
        log.info("获取电脑修改记录列表 参数: {}", queryDTO);
        try {
            Dict result = sysControlService.getControlRecordList(queryDTO);
            return Res.success(result);
        } catch (Exception e) {
            log.error("获取电脑修改记录列表失败", e);
            return Res.error("获取电脑修改记录列表失败: " + e.getMessage());
        }
    }

    /**
     * 归还电脑
     * @param sysControlModel 电脑信息
     * @param returnStatus 归还后的状态
     * @return 归还结果
     */
    @RequestMapping("/returnComputer")
    @RequiresPermissions("sys:device:control:update")
    public Res returnComputer(@RequestBody SysControlModel sysControlModel, @RequestParam(value = "returnStatus", required = false) String returnStatus) {
        try {
            log.info("归还电脑，电脑信息: {}, 归还状态: {}", sysControlModel, returnStatus);
            
            // 获取原始电脑信息，保存用于记录日志
            SysControlModel originalComputer = sysControlService.getComputerInfoByCiName(sysControlModel.getCiName());
            if (originalComputer == null) {
                return Res.error("未找到该电脑信息");
            }

            // 保存初始电脑记录
            LocalDateTime now = LocalDateTime.now();
            // 格式化为字符串：yyyy-MM-dd HH:mm:ss
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTime = now.format(formatter);
            SysControlRecordDTO sysControlRecordDTO = new SysControlRecordDTO();
            BeanUtils.copyProperties(originalComputer, sysControlRecordDTO);
            sysControlRecordDTO.setUpdateTime(formattedTime);
            sysControlMapper.updateSysControlRecord(sysControlRecordDTO);
            
            // 计算电脑使用年限（仅用于日志记录）
            double yearsToDay = 0;
            if (originalComputer.getLifeCycleStart() != null && !originalComputer.getLifeCycleStart().isEmpty()) {
                String lifeCycleStart = originalComputer.getLifeCycleStart();
                
                // 解析日期，支持多种格式
                LocalDate manufactureDate = null;
                try {
                    // 尝试解析ISO格式的日期（带T的格式）
                    if (lifeCycleStart.contains("T")) {
                        manufactureDate = LocalDate.parse(lifeCycleStart.split("T")[0]);
                    } else {
                        // 尝试解析yyyy-MM-dd格式
                        manufactureDate = LocalDate.parse(lifeCycleStart, DateTimeFormatter.ISO_LOCAL_DATE);
                    }
                } catch (Exception e) {
                    try {
                        // 尝试解析自定义格式，如yyyy/MM/dd
                        manufactureDate = LocalDate.parse(lifeCycleStart, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                    } catch (Exception ex) {
                        log.error("无法解析出厂日期: {}", lifeCycleStart);
                    }
                }
                
                if (manufactureDate != null) {
                    // 计算年限
                    LocalDate today = LocalDate.now();
                    Period period = Period.between(manufactureDate, today);
                    yearsToDay = period.getYears() + (period.getMonths() / 12.0) + (period.getDays() / 365.25);
                    log.info("电脑 {} 使用年限: {} 年", sysControlModel.getCiName(), yearsToDay);
                }
            }
            
            // 更新电脑状态
            SysControlModel updatedComputer = new SysControlModel();
            updatedComputer.setId(originalComputer.getId());
            
            // 根据前端传递的returnStatus参数设置状态，如果未传递则按照使用年限判断
            if (returnStatus != null && !returnStatus.isEmpty()) {
                // 使用前端传递的状态
                updatedComputer.setPcStatus(returnStatus);
                updatedComputer.setPcClass(returnStatus);
                log.info("使用前端传递的状态: {}", returnStatus);
            } else {
                // 按照使用年限判断状态（兼容旧版本）
                if (yearsToDay > 6) {
                    // 超过6年，状态改为To be scrapped
                    updatedComputer.setPcStatus("To be scrapped");
                    updatedComputer.setPcClass("To be scrapped");
                } else {
                    // 未超过6年，状态改为To be assigned
                    updatedComputer.setPcStatus("To be assigned");
                    updatedComputer.setPcClass("To be assigned");
                }
                log.info("使用默认状态判断逻辑，基于使用年限: {} 年", yearsToDay);
            }
            
            // 清空用户相关信息
//            updatedComputer.setNtAccount("");
            updatedComputer.setLastName("");
            updatedComputer.setFirstName("");
            updatedComputer.setEmailAddress("");
            updatedComputer.setTelephone("");
            updatedComputer.setDepartment("");
            updatedComputer.setCostCenter("");
            updatedComputer.setTemp(0); // 重置临时分配标志

            // 更新电脑信息
            boolean updateResult = sysControlService.updateSysControl(updatedComputer);
            
            if (updateResult) {
                log.info("电脑 {} 归还成功, 更新后状态: {}", sysControlModel.getCiName(), updatedComputer.getPcStatus());
                return Res.success("电脑归还成功");
            } else {
                log.error("电脑 {} 归还失败", sysControlModel.getCiName());
                return Res.error("电脑归还失败");
            }
            
        } catch (Exception e) {
            log.error("归还电脑失败", e);
            return Res.error("归还电脑失败: " + e.getMessage());
        }
    }
}
