package jnpf.util.excel;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import jnpf.constant.MsgCode;
import jnpf.enums.CellDataTypeEnum;
import jnpf.enums.ResourceEnum;
import jnpf.exception.DataException;
import jnpf.univer.model.UniverWorkBook;
import jnpf.univer.properties.*;
import jnpf.univer.resources.UniverResource;
import jnpf.univer.resources.UniverResourceData;
import jnpf.univer.sheet.*;
import jnpf.univer.style.UniverStyle;
import jnpf.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.PaneInformation;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ImportExcel {

    private static Map<String, ImportExcelParser> excelMap = ImmutableMap.of(
            "xlsx", new ImportXssfExcel(),
            "xls", new ImportHssfExcel()
    );

    public static UniverWorkBook formFile(MultipartFile file) throws IOException {
        //todo 推荐使用xlsx方法
        String fileName = file.getOriginalFilename();
        String excelType = Files.getFileExtension(fileName);
        ImportExcelParser excelParser = excelMap.get(excelType);
        if (excelParser == null) {
            throw new DataException(MsgCode.ETD110.get());
        }
        UniverWorkBook univerWorkBook = ImportExcel.univerWorkBook(file.getInputStream(), excelParser);
        return univerWorkBook;
    }

    private static UniverWorkBook univerWorkBook(InputStream inputStream, ImportExcelParser excelParser) throws IOException {
        Workbook workbook = WorkbookFactory.create(inputStream);
        UniverWorkBook univerWorkBook = new UniverWorkBook();
        String unitId = RandomUtil.randomString(10);
        univerWorkBook.setId(unitId);
        univerWorkBook.setName("");
        Map<UniverStyle, String> styleMap = new HashMap<>();
        List<UniverSheet> sheetList = new ArrayList<>();
        //sheet名称对应的id
        Map<String, String> univerSheetMap = new HashMap<>();
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            String sheetId = RandomUtil.randomString(10);
            Sheet sheet = workbook.getSheetAt(i);
            univerSheetMap.put(sheet.getSheetName(), sheetId);
        }
        Map<String, String> definedName = new HashMap<>();
        Map<String, UniverResourceData> definedNameMap = definedName(workbook, definedName, univerSheetMap);
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            String sheetName = sheet.getSheetName();
            String sheetId = univerSheetMap.get(sheetName);
            UniverSheet univerSheet = new UniverSheet();
            univerSheet.setId(sheetId);
            univerSheet.setName(sheetName);
            univerSheet.setHidden(!workbook.isSheetHidden(i) ? 0 : 1);
            univerSheet.setTabColor("");
            //冻结位置
            UniverSheetFreeze sheetFreeze = new UniverSheetFreeze();
            PaneInformation freeze = sheet.getPaneInformation();
            if (freeze != null) {
                int freezeColumn = freeze.getVerticalSplitLeftColumn();
                if (freezeColumn > 0) {
                    sheetFreeze.setStartColumn(freezeColumn);
                    sheetFreeze.setXSplit(freezeColumn);
                }
                int freezeRow = freeze.getHorizontalSplitTopRow();
                if (freezeRow > 0) {
                    sheetFreeze.setStartRow(freezeRow);
                    sheetFreeze.setYSplit(freezeRow);
                }
            }
            univerSheet.setFreeze(sheetFreeze);

            Map<Integer, UniverSheetRowData> rowData = new HashMap<>();
            Map<Integer, UniverSheetColumnData> columnData = new HashMap<>();
            Map<Integer, Map<Integer, UniverSheetCellData>> cellData = new HashMap<>();
            for (int rowCount = 0; rowCount <= sheet.getLastRowNum(); rowCount++) {
                Row row = sheet.getRow(rowCount);
                Map<Integer, UniverSheetCellData> cells = new HashMap<>();
                if (null != row) {
                    for (int columnCount = 0; columnCount <= row.getLastCellNum(); columnCount++) {
                        Cell cell = row.getCell(columnCount);
                        UniverSheetCellData univerSheetCellData = new UniverSheetCellData();
                        if (null != cell) {
                            //数据
                            Object value = null;
                            String formula = null;
                            CellType cellType = cell.getCellType();
                            Integer type = null;
                            switch (cellType) {
                                case STRING:
                                    value = cell.getStringCellValue();
                                    type = CellDataTypeEnum.String.getCode();
                                    break;
                                case BOOLEAN:
                                    value = cell.getBooleanCellValue();
                                    type = CellDataTypeEnum.Boolean.getCode();
                                    break;
                                case NUMERIC:
                                    value = new BigDecimal(cell.getNumericCellValue());
                                    type = CellDataTypeEnum.Number.getCode();
                                    break;
                                case FORMULA:
                                    formula = "=" + cell.getCellFormula();
                                    type = CellDataTypeEnum.Formula.getCode();
                                    break;
                            }
                            univerSheetCellData.setV(value);
                            univerSheetCellData.setF(formula);
                            univerSheetCellData.setT(type);
                            //样式
                            CellStyle cellStyle = cell.getCellStyle();
                            UniverStyle style = excelParser.getUniverStyle(cellStyle, workbook);
                            String styleName = styleMap.get(style) != null ? styleMap.get(style) : RandomUtil.randomString(6);
                            styleMap.put(style, styleName);
                            univerSheetCellData.setS(styleName);
                            cells.put(columnCount, univerSheetCellData);
                            UniverSheetColumnData univerSheetColumnData = new UniverSheetColumnData();
                            univerSheetColumnData.setHd(!sheet.isColumnHidden(columnCount) ? 0 : 1);
                            List<Integer> colHeight = new ArrayList<>();
                            colHeight.add(100);
                            colHeight.add((int) sheet.getColumnWidthInPixels(columnCount));
                            univerSheetColumnData.setW(Collections.max(colHeight));
                            columnData.put(columnCount, univerSheetColumnData);
                            link(cell, value, definedName, univerSheetMap, univerSheetCellData);
                        }
                    }
                    cellData.put(rowCount, cells);
                    UniverSheetRowData univerSheetRowData = new UniverSheetRowData();
                    univerSheetRowData.setHd(!row.getZeroHeight() ? 0 : 1);
                    List<Integer> rowHeight = new ArrayList<>();
                    rowHeight.add(50);
                    rowHeight.add((int) row.getHeightInPoints());
                    univerSheetRowData.setH(Collections.max(rowHeight));
                    univerSheetRowData.setIa(0);
                    rowData.put(rowCount, univerSheetRowData);
                }
            }
            univerSheet.setCellData(cellData);
            univerSheet.setRowData(rowData);
            univerSheet.setColumnData(columnData);

            List<UniverSheetRange> mergeData = new ArrayList<>();
            for (CellRangeAddress region : sheet.getMergedRegions()) {
                UniverSheetRange univerSheetRange = new UniverSheetRange();
                univerSheetRange.setStartRow(region.getFirstRow());
                univerSheetRange.setStartColumn(region.getFirstColumn());
                univerSheetRange.setEndRow(region.getLastRow());
                univerSheetRange.setEndColumn(region.getLastColumn());
                mergeData.add(univerSheetRange);
            }
            univerSheet.setMergeData(mergeData);

            UniverSheetRowHeader univerSheetRowHeader = new UniverSheetRowHeader();
            univerSheetRowHeader.setHidden(0);
            univerSheetRowHeader.setWidth(46);
            univerSheet.setRowHeader(univerSheetRowHeader);

            UniverSheetColumnHeader univerSheetColumnHeader = new UniverSheetColumnHeader();
            univerSheetColumnHeader.setHidden(0);
            univerSheetColumnHeader.setHeight(20);
            univerSheet.setColumnHeader(univerSheetColumnHeader);

            univerSheet.setZoomRatio(1);
            univerSheet.setScrollTop(0);
            univerSheet.setScrollLeft(0);
            univerSheet.setDefaultColumnWidth(88);
            univerSheet.setDefaultRowHeight(24);
            List<Integer> rowCount = new ArrayList<>();
            rowCount.add(100);
            rowCount.add(rowData.size());
            univerSheet.setRowCount(Collections.max(rowCount));
            List<Integer> columnCount = new ArrayList<>();
            columnCount.add(30);
            columnCount.add(columnData.size());
            univerSheet.setColumnCount(Collections.max(columnCount));
            univerSheet.setShowGridlines(sheet.isDisplayGridlines() ? 1 : 0);

            univerSheet.setSelections(ImmutableList.of("A1"));
            univerSheet.setRightToLeft(0);
            sheetList.add(univerSheet);
        }

        Map<String, UniverStyle> styles = styleMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        univerWorkBook.setStyles(styles);
        Map<String, UniverSheet> sheetMap = sheetList.stream().collect(Collectors.toMap(UniverSheet::getId, Function.identity(), (s1, s2) -> s1, LinkedHashMap::new));
        univerWorkBook.setSheetOrder(new ArrayList<>(sheetMap.keySet()));
        univerWorkBook.setSheets(sheetMap);

        //图片
        Map<String, UniverResourceData> drawingMap = new HashMap<>();
        //条件
        Map<String, List<UniverResourceData>> formattingMap = new HashMap<>();
        //数据管理
        Map<String, List<UniverResourceData>> dataValidationMap = new HashMap<>();
        //筛选
        Map<String, UniverResourceData> filterMap = new HashMap<>();
        //处理前端要的数据格式
        univerSheetMap.forEach((key, value) -> {
            Sheet sheet = workbook.getSheet(key);
            if (sheet != null) {
                //图片
                drawingMap.putAll(excelParser.drawing(sheet, value, unitId));
                //条件
                formattingMap.putAll(excelParser.format(sheet, value, unitId));
                //数据管理
                dataValidationMap.putAll(excelParser.dataValidation(sheet, value, unitId));
                //筛选
                filterMap.putAll(excelParser.filter(sheet, value, unitId));
            }
        });
        Map<String, String> resourceMap = new HashMap<>();
        resourceMap.put(ResourceEnum.SHEET_DRAWING_PLUGIN.name(), JSONUtil.toJsonStr(drawingMap));
        resourceMap.put(ResourceEnum.SHEET_CONDITIONAL_FORMATTING_PLUGIN.name(), JSONUtil.toJsonStr(formattingMap));
        resourceMap.put(ResourceEnum.SHEET_DATA_VALIDATION_PLUGIN.name(), JSONUtil.toJsonStr(dataValidationMap));
        resourceMap.put(ResourceEnum.SHEET_FILTER_PLUGIN.name(), JSONUtil.toJsonStr(filterMap));
//        resourceMap.put(ResourceEnum.SHEET_DEFINED_NAME_PLUGIN.name(), JSONUtil.toJsonStr(definedNameMap));
        univerWorkBook.setResources(getUniverResources(resourceMap));
        return univerWorkBook;
    }

    private static List<UniverResource> getUniverResources(Map<String, String> resourceMap) {
        List<UniverResource> resourcesList = new ArrayList<>();
        for (ResourceEnum resource : ResourceEnum.values()) {
            UniverResource model = new UniverResource();
            String name = resource.name();
            model.setName(name);
            model.setData(resourceMap.get(name) != null ? resourceMap.get(name) : "{}");
            resourcesList.add(model);
        }
        return resourcesList;
    }

    private static void link(Cell cell, Object value, Map<String, String> definedName, Map<String, String> univerSheetMap, UniverSheetCellData univerSheetCellData) {
        Hyperlink link = cell.getHyperlink();
        if (link != null) {
            String address = link.getAddress();
            if (StringUtil.isEmpty(address)) {
                return;
            }
            //body
            UniverBody body = new UniverBody();
            String dataValue = value != null ? value.toString() : "";
            if (StringUtil.isEmpty(dataValue)) {
                return;
            }
            List<String> typeList = ImmutableList.of("\r\n", "\n", "\r");
            for (String type : typeList) {
                dataValue = StringUtils.removeEnd(dataValue, type);
            }
            int valueIndex = dataValue.length();
            String dataStream = dataValue + "\r\n";
            body.setDataStream(dataStream);
            //customRanges
            List<UniverBodyConfig> customRangesList = new ArrayList<>();
            UniverBodyConfig customRanges = new UniverBodyConfig();
            customRanges.setRangeId(RandomUtil.randomString(10));
            customRanges.setRangeType(0);
            customRanges.setStartIndex(0);
            UniverBodyConfig properties = new UniverBodyConfig();
            String[] split = address.split("!");
            String url = Objects.equals(link.getType(), HyperlinkType.URL) ? address : definedName.get(address) != null ? "#rangeid=" + definedName.get(address) : "";
            String linkSheetId = univerSheetMap.get(split[0]);
            if (linkSheetId != null && split.length > 1) {
                url = "#gid=" + linkSheetId + "&range=" + split[1];
            }
            properties.setUrl(url);
            properties.setRefId(customRanges.getRangeId());
            customRanges.setEndIndex(valueIndex > 0 ? valueIndex - 1 : valueIndex);
            customRanges.setProperties(properties);
            customRangesList.add(customRanges);
            body.setCustomRanges(customRangesList);
            //textRuns
            body.setTextRuns(new ArrayList<>());
            //customDecorations
            body.setCustomDecorations(new ArrayList<>());
            //sectionBreaks
            List<UniverBodyConfig> sectionBreaksList = new ArrayList<>();
            UniverBodyConfig sectionBreaks = new UniverBodyConfig();
            sectionBreaks.setStartIndex(valueIndex + 1);
            sectionBreaksList.add(sectionBreaks);
            body.setSectionBreaks(sectionBreaksList);
            //paragraphs
            List<UniverBodyConfig> paragraphsList = new ArrayList<>();
            UniverBodyConfig paragraphs = new UniverBodyConfig();
            paragraphs.setStartIndex(valueIndex);
            UniverBodyConfig paragraphStyle = new UniverBodyConfig();
            paragraphStyle.setHorizontalAlign(0);
            paragraphs.setParagraphStyle(paragraphStyle);
            paragraphsList.add(paragraphs);
            body.setParagraphs(paragraphsList);
            UniverProperties p = new UniverProperties();
            p.setBody(body);
            p.setId("d");
            univerSheetCellData.setP(p);
            univerSheetCellData.setV(null);
        }
    }

    private static Map<String, UniverResourceData> definedName(Workbook workbook, Map<String, String> definedName, Map<String, String> univerSheetMap) {
        Map<String, UniverResourceData> definedNameMap = new HashMap<>();
        for (Name name : workbook.getAllNames()) {
            String definedId = RandomUtil.randomString(10);
            String localSheetId = "AllDefaultWorkbook";
            String nameName = name.getNameName();
            int sheetIndex = name.getSheetIndex();
            String sheetName = "";
            if (sheetIndex > -1) {
                Sheet sheetAt = workbook.getSheetAt(sheetIndex);
                sheetName = sheetAt.getSheetName();
                localSheetId = univerSheetMap.get(sheetName);
            }
            definedName.put(nameName + (StringUtil.isNotEmpty(sheetName) ? "!" + sheetName : ""), definedId);
            UniverResourceData resourceData = new UniverResourceData();
            resourceData.setId(definedId);
            resourceData.setName(nameName);
            resourceData.setFormulaOrRefString(name.getRefersToFormula());
            resourceData.setComment(name.getComment());
            resourceData.setLocalSheetId(localSheetId);
            definedNameMap.put(definedId, resourceData);
        }
        return definedNameMap;
    }

}
