package ink.usr.admin.service.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import ink.usr.admin.dao.DTO.SysControlDTO;
import ink.usr.admin.dao.VO.PageVO;
import ink.usr.admin.dao.VO.SysControlBillListVO;
import ink.usr.admin.mapper.SysControlAssetMapper;
import ink.usr.admin.service.SysControlAssetService;
import ink.usr.common.core.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.ArrayList;

@Slf4j
@Service
public class SysControlAssetServiceImpl implements SysControlAssetService {

    @Autowired
    private SysControlAssetMapper sysControlAssetMapper;

    @Override
    public PageVO<SysControlBillListVO> selectSysControlAssetList(SysControlDTO sysControlDTO) {
        // 预处理WBS号，如果包含逗号则设置到wbsNumList属性
        if (sysControlDTO.getWbsNum() != null && sysControlDTO.getWbsNum().contains(",")) {
            // 处理多个WBS号的情况
            String[] wbsArray = sysControlDTO.getWbsNum().split(",");
            List<String> wbsList = new ArrayList<>();
            for (String wbs : wbsArray) {
                String trimmedWbs = wbs.trim();
                if (!trimmedWbs.isEmpty()) {
                    wbsList.add(trimmedWbs);
                }
            }
            sysControlDTO.setWbsNumList(wbsList);
            // 清空原始的wbsNum，避免在XML中重复处理
            sysControlDTO.setWbsNum(null);
        }

        // 开启分页
        PageHelper.startPage(sysControlDTO.getPageNum(), sysControlDTO.getPageSize());

        // 查询数据
        List<SysControlBillListVO> assetList = sysControlAssetMapper.getCoputerAssetList(sysControlDTO);

        // 处理业务逻辑
        for (SysControlBillListVO asset : assetList) {
            calculateAssetValues(asset);
        }

        // 获取分页信息
        PageInfo<SysControlBillListVO> pageInfo = new PageInfo<>(assetList);

        // 构建分页响应对象
        return PageVO.build(
                pageInfo.getList(),
                pageInfo.getTotal(),
                pageInfo.getPageNum(),
                pageInfo.getPageSize()
        );
    }

    /**
     * 计算资产相关值
     *
     * @param asset 资产对象
     */
    private void calculateAssetValues(SysControlBillListVO asset) {
        // 1. 计算价格的10%
        double tenPercent = asset.getPrice() * 0.1;
        asset.setTenPercentPrice(tenPercent);

        // 2. 计算WBS价值占比
        if (asset.getIfrsValue() > 0) {
            double percent = (asset.getPrice() / asset.getIfrsValue()) * 100;

            asset.setWbsPercent(String.format("%.2f%%", percent));
        } else {
            asset.setWbsPercent("0.00%");
        }
        asset.setPrcResidualValue(asset.getPrcAllResidualValue() * (asset.getPrice() / asset.getIfrsValue()));
        asset.setIfrsResidualValue(asset.getIfrsAllResidualValue() * (asset.getPrice() / asset.getIfrsValue()));
        // 3. 计算转卖价（取 PRC残值 和 价格10% 中的较大值）
        asset.setSellPrice(Math.max(asset.getPrcResidualValue(), tenPercent));

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importIfrsData(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        try (InputStream is = file.getInputStream()) {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);


            // 获取标题行
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new BusinessException("Excel文件格式错误：第一行（标题行）不能为空");
            }

            Map<String, Integer> headerMap = new HashMap<>();
            try {
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    Cell cell = headerRow.getCell(i);
                    if (cell != null) {
                        String headerValue = getCellValueAsString(cell);
                        if (headerValue != null && !headerValue.trim().isEmpty()) {
                            headerMap.put(headerValue.trim(), i);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("解析Excel标题行失败", e);
                throw new BusinessException("Excel文件格式错误：请确保第一行为标题行，且不包含公式或特殊格式");
            }


            // 验证必要列是否存在
            validateHeaders(headerMap);

            List<Map<String, Object>> dataList = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, Object> rowData = new HashMap<>();
                rowData.put("asset", getCellValueAsString(row.getCell(headerMap.get("Asset"))));
                rowData.put("costCenter", getCellValueAsString(row.getCell(headerMap.get("Cost Center"))));
                rowData.put("capitalizedOn", getCellValueAsString(row.getCell(headerMap.get("Capitalized on"))));
                rowData.put("wbsElement", getCellValueAsString(row.getCell(headerMap.get("WBS element"))));
                rowData.put("assetClass", getCellValueAsString(row.getCell(headerMap.get("Asset Class"))));
                rowData.put("currency", getCellValueAsString(row.getCell(headerMap.get("Currency"))));
                rowData.put("acquisVal", getCellValueAsDecimal(row.getCell(headerMap.get("Acquis.val."))));
                rowData.put("accumDep", getCellValueAsDecimal(row.getCell(headerMap.get("Accum.dep."))));
                rowData.put("bookVal", getCellValueAsDecimal(row.getCell(headerMap.get("Book val."))));

                dataList.add(rowData);
            }

            // 分批处理数据
            int batchSize = 1000; // 每批处理1000条
            for (int i = 0; i < dataList.size(); i += batchSize) {
                int end = Math.min(i + batchSize, dataList.size());
                List<Map<String, Object>> batch = dataList.subList(i, end);
                if (!batch.isEmpty()) {
                    sysControlAssetMapper.batchUpdateIfrsData(batch);
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importPrcData(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        try (InputStream is = file.getInputStream()) {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheetAt(0);

            // 获取标题行
            Row headerRow = sheet.getRow(0);
            Map<String, Integer> headerMap = new HashMap<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    headerMap.put(cell.getStringCellValue().trim(), i);
                }
            }

            // 验证必要的列是否存在
            validateHeaders(headerMap);

            List<Map<String, Object>> dataList = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, Object> rowData = new HashMap<>();
                rowData.put("asset", getCellValueAsString(row.getCell(headerMap.get("Asset"))));
                rowData.put("costCenter", getCellValueAsString(row.getCell(headerMap.get("Cost Center"))));
                rowData.put("capitalizedOn", getCellValueAsString(row.getCell(headerMap.get("Capitalized on"))));
                rowData.put("wbsElement", getCellValueAsString(row.getCell(headerMap.get("WBS element"))));
                rowData.put("assetClass", getCellValueAsString(row.getCell(headerMap.get("Asset Class"))));
                rowData.put("currency", getCellValueAsString(row.getCell(headerMap.get("Currency"))));
                rowData.put("acquisVal", getCellValueAsDecimal(row.getCell(headerMap.get("Acquis.val."))));
                rowData.put("accumDep", getCellValueAsDecimal(row.getCell(headerMap.get("Accum.dep."))));
                rowData.put("bookVal", getCellValueAsDecimal(row.getCell(headerMap.get("Book val."))));

                dataList.add(rowData);
            }

            // 批量更新PRC数据
            if (!dataList.isEmpty()) {
                sysControlAssetMapper.batchUpdatePrcData(dataList);
            }
        }
    }

    private void validateHeaders(Map<String, Integer> headerMap) {
        List<String> requiredHeaders = Arrays.asList(
                "Asset", "Cost Center", "Capitalized on", "WBS element",
                "Asset Class", "Currency", "Acquis.val.", "Accum.dep.", "Book val."
        );

        for (String header : requiredHeaders) {
            if (!headerMap.containsKey(header)) {
                throw new BusinessException("Excel文件缺少必要的列：" + header);
            }
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }

    private BigDecimal getCellValueAsDecimal(Cell cell) {
        if (cell == null) return BigDecimal.ZERO;

        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return BigDecimal.valueOf(cell.getNumericCellValue());
                case STRING:
                    String value = cell.getStringCellValue().trim();
                    return value.isEmpty() ? BigDecimal.ZERO : new BigDecimal(value);
                default:
                    return BigDecimal.ZERO;
            }
        } catch (Exception e) {
            log.warn("转换数值失败: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }
}
