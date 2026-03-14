package jnpf.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import jnpf.constant.DataInterfaceVarConst;
import jnpf.constant.MsgCode;
import jnpf.enums.*;
import jnpf.exception.DataException;
import jnpf.model.*;
import jnpf.univer.chart.UniverChartModel;
import jnpf.univer.data.cell.UniverDataConfig;
import jnpf.univer.data.custom.UniverCustom;
import jnpf.univer.data.resource.UniverDrawing;
import jnpf.univer.model.UniverPreview;
import jnpf.univer.model.UniverWorkBook;
import jnpf.univer.properties.UniverBody;
import jnpf.univer.properties.UniverBodyConfig;
import jnpf.univer.properties.UniverProperties;
import jnpf.univer.resources.UniverResource;
import jnpf.univer.resources.UniverResourceData;
import jnpf.univer.sheet.*;
import jnpf.univer.zxing.UniverZxingModel;
import jnpf.ureport.build.Dataset;
import jnpf.ureport.build.ReportBuilder;
import jnpf.ureport.cell.down.DownCellbuilder;
import jnpf.ureport.cell.right.RightCellbuilder;
import jnpf.ureport.definition.*;
import jnpf.ureport.definition.value.AggregateType;
import jnpf.ureport.definition.value.DatasetValue;
import jnpf.ureport.definition.value.SimpleValue;
import jnpf.ureport.definition.value.Value;
import jnpf.ureport.model.*;
import jnpf.ureport.utils.DataUtils;
import jnpf.util.type.SortType;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/5/11 下午4:35
 */
public class UniverConvert {
    public static final int OFFSET = 0;
    public static final Set<String> SIMPLE_VALUE = ImmutableSet.of(
            CellDataEnum.text.name(),
            CellDataEnum.parameter.name()
    );


    //-------------------------------------解析数据------------------------------------------------

    public UniverPreview transform(String snapshot, String cells, String sort, String fence, Map<String, Map<String, List<Map<String, Object>>>> sheetData, Map<String, Map<String, Map<String, Object>>> parameterData) {
        UniverWorkBook univerWorkBook = StringUtil.isNotEmpty(snapshot) ? JsonUtil.getJsonToBean(snapshot, UniverWorkBook.class) : new UniverWorkBook();
        String unitId = RandomUtil.uuId();
        univerWorkBook.setId(unitId);
        UniverCustom custom = StringUtil.isNotEmpty(cells) ? JsonUtil.getJsonToBean(cells, UniverCustom.class) : new UniverCustom();
        List<DataQuery> sortListAll = StringUtil.isNotEmpty(sort) ? JsonUtil.getJsonToList(sort, DataQuery.class) : new ArrayList<>();
        List<Fence> fenceListAll = StringUtil.isNotEmpty(fence) ? JsonUtil.getJsonToList(fence, Fence.class) : new ArrayList<>();
        //排序
        Map<String, Map<String, List<DataSortModel>>> sortMap = new HashMap<>();
        for (DataQuery dataQuery : sortListAll) {
            String sheetId = dataQuery.getSheet();
            Map<String, List<DataSortModel>> dataBaseMap = sortMap.get(sheetId) != null ? sortMap.get(sheetId) : new HashMap<>();
            List<DataSortModel> sortList = dataQuery.getSortList();
            for (DataSortModel dataModel : sortList) {
                DataSortModel sortModel = JsonUtil.getJsonToBean(dataModel, DataSortModel.class);
                String[] params = sortModel.getVModel().split("\\.");
                if (params.length == 2) {
                    String datasetName = params[0];
                    String property = params[1];
                    sortModel.setVModel(property);
                    List<DataSortModel> dataSortModels = dataBaseMap.get(datasetName) != null ? dataBaseMap.get(datasetName) : new ArrayList<>();
                    dataSortModels.add(sortModel);
                    dataBaseMap.put(datasetName, dataSortModels);
                }
            }
            sortMap.put(sheetId, dataBaseMap);
        }
        Map<String, ColumnList> fenceMap = new HashMap<>();
        for (Fence fenceEntity : fenceListAll) {
            String sheetId = fenceEntity.getSheet();
            ColumnList columnList = fenceEntity.getColumnList();
            fenceMap.put(sheetId, columnList);
        }

        //悬浮图表
        List<UniverResource> resources = univerWorkBook.getResources() != null ? univerWorkBook.getResources() : new ArrayList<>();
        Map<String, UniverDataConfig> floatEcharts = custom.getFloatEcharts() != null ? custom.getFloatEcharts() : new HashMap<>();
        Map<String, List<UniverDataConfig>> floatEchartMap = floatEchart(univerWorkBook.getId(), resources, floatEcharts);
        //单元格图表
        Map<String, UniverDataConfig> cellEcharts = custom.getCellEcharts() != null ? custom.getCellEcharts() : new HashMap<>();
        Map<String, List<UniverDataConfig>> cellEchartMap = cellEchart(cellEcharts, univerWorkBook.getId());
        Map<String, UniverSheet> sheetMap = new HashMap<>();
        List<UniverChartModel> chartList = new ArrayList<>();
        for (String sheetOrder : univerWorkBook.getSheetOrder()) {
            UniverSheet univerSheet = univerWorkBook.getSheets().get(sheetOrder);
            Map<String, List<Map<String, Object>>> dataList = sheetData.get(sheetOrder) != null ? sheetData.get(sheetOrder) : new HashMap<>();
            Map<String, List<DataSortModel>> sortDataMap = sortMap.get(sheetOrder) != null ? sortMap.get(sheetOrder) : new HashMap<>();
            Map<String, Map<String, Object>> parameterMap = parameterData.get(sheetOrder) != null ? parameterData.get(sheetOrder) : new HashMap<>();
            if (ObjectUtil.isNotEmpty(univerSheet)) {
                List<UniverDataConfig> cellList = custom.getCells().stream().filter(t -> Objects.equals(sheetOrder, t.getSheet())).collect(Collectors.toList());
                List<UniverDataConfig> cellEchartList = cellEchartMap.get(sheetOrder) != null ? cellEchartMap.get(sheetOrder) : new ArrayList<>();
                cellList.addAll(cellEchartList);
                ColumnList columnList = fenceMap.get(sheetOrder) != null ? fenceMap.get(sheetOrder) : new ColumnList();
                Map<String, List<Map<String, Object>>> oneDataList = new HashMap<>();
                Map<String, String> sortInMap = new HashMap<>();
                for (String key : dataList.keySet()) {
                    List<Map<String, Object>> list = dataList.get(key) != null ? dataList.get(key) : new ArrayList<>();
                    List<DataSortModel> dataSortList = sortDataMap.get(key) != null ? sortDataMap.get(key) : new ArrayList<>();
                    StringJoiner joiner = new StringJoiner(",");
                    for (DataSortModel dataSortModel : dataSortList) {
                        String vModel = dataSortModel.getVModel();
                        boolean isDesc = Objects.equals(SortType.DESC, dataSortModel.getType());
                        joiner.add((isDesc ? "-" : "") + vModel);
                    }
                    sortInMap.put(key, joiner.toString());
                    List<Map<String, Object>> data = DataUtils.orderDataList(list, joiner.toString());

                    oneDataList.put(key, data);
                }
                UniverSheet sheet = new UniverSheet();


                sheet = buildLuckySheetDate(univerSheet, cellList, oneDataList, columnList, parameterMap, resources, sortInMap);
//                if (BeanUtil.isNotEmpty(columnList) && columnList.isColumnState()) {
//                    //函数处理
//                    Map<Integer, Map<Integer, UniverSheetCellData>> sheetCellData = sheet.getCellData();
//                    //map转大集合
//                    List<UniverSheetCellData> resultList = sheetCellData.values().stream()
//                            .flatMap(innerMap -> innerMap.values().stream())
//                            .filter(item -> null != item.getT() && item.getT().equals(CellDataTypeEnum.Formula.getCode()))
//                            .collect(Collectors.toList());
//                    for (UniverSheetCellData univerSheetCellData : resultList) {
//                        formulaListOut(univerSheetCellData, sheetCellData, columnList.getColumnStyle());
//                    }
//
//
//                }


                List<UniverDataConfig> echartsList = new ArrayList<>();
                echartsList.addAll(floatEchartMap.get(sheetOrder) != null ? floatEchartMap.get(sheetOrder) : new ArrayList<>());
                echartsList.addAll(cellEchartList);
                ChartUtil.chart(chartList, dataList, echartsList);

                sheetMap.put(sheet.getId(), sheet);
            }
        }
        univerWorkBook.setSheets(sheetMap);
        UniverPreview vo = new UniverPreview();
        vo.setSnapshot(JSON.toJSONString(JSON.toJSON(univerWorkBook)));
        vo.setCells(cells);

        vo.setChartData(JSON.toJSONString(JSON.toJSON(chartList)));
        return vo;
    }


    private UniverSheet buildLuckySheetDate(UniverSheet sheet, List<UniverDataConfig> cell, Map<String, List<Map<String, Object>>> stringListMap,
                                            ColumnList columnList, Map<String, Map<String, Object>> parameterDataMap
            , List<UniverResource> resources, Map<String, String> sortInMap) {
        Map<Integer, Map<Integer, UniverSheetCellData>> cellData = sheet.getCellData();
        List<UniverDataConfig> parameterData = cell.stream().filter(t -> CellDataEnum.parameter.name().equals(t.getType())).collect(Collectors.toList());
        Pattern pattern = Pattern.compile("#\\{(.*?)\\}");
        for (UniverDataConfig dataConfig : parameterData) {
            Integer row = dataConfig.getRow();
            Integer col = dataConfig.getCol();
            Map<Integer, UniverSheetCellData> dataMap = cellData.get(row) != null ? cellData.get(row) : new HashMap<>();
            UniverSheetCellData sheetCellData = dataMap.get(col);
            if (sheetCellData != null) {
                String value = "";
                Object v = sheetCellData.getV();
                if (v != null) {
                    value = v + "";
                }
                Object p = sheetCellData.getP();
                if (p != null) {
                    UniverProperties properties = JsonUtil.getJsonToBean(p, UniverProperties.class);
                    UniverBody body = properties.getBody();
                    if (body != null) {
                        String dataStream = body.getDataStream();
                        if (dataStream != null) {
                            value = dataStream;
                        }
                    }
                }
                Matcher matcher = pattern.matcher(value);
                Map<Integer, Map<Integer, String>> number = new HashMap<>();
                List<String> random = ImmutableList.of(DataInterfaceVarConst.ID_LOT);
                while (matcher.find()) {
                    String patternText = matcher.group(1);
                    String[] split = patternText.split("\\.");
                    String datasetName = split[0];
                    String property = split.length > 1 ? split[1] : split[0];
                    boolean isRandom = random.contains(property);
                    Map<String, Object> data = parameterDataMap.get(datasetName) != null ? parameterDataMap.get(datasetName) : new HashMap<>();
                    String textValue = data.get(property) != null ? data.get(property) + "" : isRandom ? RandomUtil.uuId() : "";
                    Map<Integer, String> name = new HashMap<>();
                    name.put(matcher.end(), textValue);
                    number.put(matcher.start(), name);
                }
                List<Integer> colList = new ArrayList<>(number.keySet()).stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
                for (Integer start : colList) {
                    Map<Integer, String> name = number.get(start);
                    for (Integer end : name.keySet()) {
                        value = value.substring(0, start) + name.get(end) + value.substring(end, value.length());
                    }
                }
                sheetCellData.setV(value);
                sheetCellData.setP(null);
            }
        }
        List<UniverDataConfig> datasourceData = cell.stream().filter(t -> CellDataEnum.dataSource.name().equals(t.getType())).collect(Collectors.toList());
        if (datasourceData.isEmpty()) {
            return sheet;
        }
        List<Integer> rowMax = new ArrayList<>();
        rowMax.add(0);
        List<Integer> columnMax = new ArrayList<>();
        columnMax.add(0);
        for (Integer rowKey : cellData.keySet()) {
            rowMax.add(rowKey);
            Map<Integer, UniverSheetCellData> cellDataMap = cellData.get(rowKey);
            columnMax.addAll(cellDataMap.keySet());
        }
        List<UniverSheetRange> mergeData = sheet.getMergeData();
        Map<Integer, UniverSheetRowData> sheetRowData = sheet.getRowData();
        Map<Integer, UniverSheetColumnData> sheetColumnData = sheet.getColumnData();
        ReportDefinition reportDefinition = new ReportDefinition();
        List<RowDefinition> rows = new ArrayList<>();
        List<ColumnDefinition> columns = new ArrayList<>();
        List<CellDefinition> cells = new ArrayList<>();
        Map<Integer, Map<Integer, Integer>> fillRowMap = new HashMap<>();
        reportDefinition.setRows(rows);
        reportDefinition.setColumns(columns);
        reportDefinition.setCells(cells);
        Map<String, Dataset> datasetMap = new HashMap<>();
        for (String key : stringListMap.keySet()) {
            Dataset dataset = new Dataset();
            dataset.setName(key);
            dataset.setData(stringListMap.get(key));
            datasetMap.put(key, dataset);
        }

        reportDefinition.setDatasetMap(datasetMap);
        Map<String, Formula> formulaMap = new HashMap<>();
        //冻结
        UniverSheetFreeze freeze = sheet.getFreeze() != null ? sheet.getFreeze() : new UniverSheetFreeze();
        int freezeRow = freeze.getStartRow();
        int freezeColumn = freeze.getStartColumn();

        zhouTest(sheet, cell, rowMax, sheetRowData, freezeRow, cellData, columnMax, sheetColumnData,
                freezeColumn, mergeData, formulaMap, cells, columns, fillRowMap, rows);
        Map<String, Integer> fillBlankRowsMap = fillEmptyRows(fillRowMap);
        reportDefinition.setFillBlankRowsMap(fillBlankRowsMap);
        rebuild(reportDefinition);

        rebuildReportDefinition(reportDefinition);

        ReportBuilder reportBuilder = new ReportBuilder();

        Map<String, Map<Integer, Map<Integer, String>>> nameMap = new HashMap<>();
        Report report = reportBuilder.buildReport(reportDefinition, sortInMap);
        UniverSheet result = sheet(sheet, report, formulaMap, columnList, nameMap);
        resource(sheet.getId(), nameMap, resources);
        return result;
    }


    private static void zhouTest(UniverSheet sheet, List<UniverDataConfig> cell, List<Integer> rowMax, Map<Integer, UniverSheetRowData> sheetRowData
            , int freezeRow, Map<Integer, Map<Integer, UniverSheetCellData>> cellData, List<Integer> columnMax, Map<Integer, UniverSheetColumnData> sheetColumnData
            , int freezeColumn, List<UniverSheetRange> mergeData, Map<String, Formula> formulaMap, List<CellDefinition> cells, List<ColumnDefinition> columns
            , Map<Integer, Map<Integer, Integer>> fillRowMap, List<RowDefinition> rows) {
        for (int i = 0; i <= Collections.max(rowMax); i++) {
            UniverSheetRowData rowData = sheetRowData.get(i);
            RowDefinition rowDefinition = new RowDefinition();
            rowDefinition.setRowNumber(i);
            rowDefinition.setSheetRowData(rowData);
            rowDefinition.setFreeze(Objects.equals(freezeRow - 1, i));
            Map<Integer, UniverSheetCellData> columnDataMap = cellData.get(i) != null ? cellData.get(i) : new HashMap<>();
            for (int j = 0; j <= Collections.max(columnMax); j++) {
                UniverSheetColumnData columnData = sheetColumnData.get(j);
                ColumnDefinition columnDefinition = new ColumnDefinition();
                columnDefinition.setColumnNumber(j);
                columnDefinition.setFreeze(Objects.equals(freezeColumn - 1, j));
                columnDefinition.setSheetColumnData(columnData);
                UniverSheetCellData sheetCellData = columnDataMap.get(j) != null ? columnDataMap.get(j) : new UniverSheetCellData();
                //合并
                int rowSpan = 0;
                int colSpan = 0;
                StyleModel styleModel = new StyleModel();
                UniverSheetRange sheetRange = mergeData.stream().filter(t -> t.getStartRow().equals(rowDefinition.getRowNumber()) && t.getStartColumn().equals(columnDefinition.getColumnNumber())).findFirst().orElse(null);
                boolean isRange = sheetRange != null;
                if (isRange) {
                    rowSpan = sheetRange.getEndRow() - sheetRange.getStartRow();
                    colSpan = sheetRange.getEndColumn() - sheetRange.getStartColumn();
                    if (rowSpan > 0) {
                        for (int row = sheetRange.getStartRow() + 1; row <= sheetRange.getEndRow() - 1; row++) {
                            Map<Integer, UniverSheetCellData> dataMap = cellData.get(row);
                            if (dataMap != null) {
                                UniverSheetCellData startData = dataMap.get(sheetRange.getStartColumn());
                                if (startData != null) {
                                    styleModel.setL(startData.getS());
                                }
                                UniverSheetCellData endData = dataMap.get(sheetRange.getEndColumn());
                                if (endData != null) {
                                    styleModel.setR(endData.getS());
                                }
                            }
                        }
                    }
                    if (colSpan > 0) {
                        Map<Integer, UniverSheetCellData> startDataMap = cellData.get(sheetRange.getStartRow());
                        Map<Integer, UniverSheetCellData> endDataMap = cellData.get(sheetRange.getEndRow());
                        for (int col = sheetRange.getStartColumn() + 1; col <= sheetRange.getEndColumn() - 1; col++) {
                            if (startDataMap != null) {
                                UniverSheetCellData startData = startDataMap.get(col);
                                if (startData != null) {
                                    styleModel.setT(startData.getS());
                                }
                            }
                            if (endDataMap != null) {
                                UniverSheetCellData endData = endDataMap.get(col);
                                if (endData != null) {
                                    styleModel.setB(endData.getS());
                                }
                            }
                        }
                    }
                    Map<Integer, UniverSheetCellData> startDataMap = cellData.get(sheetRange.getStartRow());
                    if (startDataMap != null) {
                        UniverSheetCellData leftTopData = startDataMap.get(sheetRange.getStartColumn());
                        if (leftTopData != null) {
                            styleModel.setLt(leftTopData.getS());
                        }
                        UniverSheetCellData rightTopData = startDataMap.get(sheetRange.getEndColumn());
                        if (rightTopData != null) {
                            styleModel.setRt(rightTopData.getS());
                        }
                    }
                    Map<Integer, UniverSheetCellData> endDataMap = cellData.get(sheetRange.getEndRow());
                    if (endDataMap != null) {
                        UniverSheetCellData leftBodData = endDataMap.get(sheetRange.getStartColumn());
                        if (leftBodData != null) {
                            styleModel.setLb(leftBodData.getS());
                        }
                        UniverSheetCellData rightBodData = endDataMap.get(sheetRange.getEndColumn());
                        if (rightBodData != null) {
                            styleModel.setRb(rightBodData.getS());
                        }
                    }
                }
                CellDefinition cellDefinition = new CellDefinition();
                String definitionName = new CellReference(i, j).formatAsString();
                cellDefinition.setName(definitionName);
                cellDefinition.setRowNumber(i + 1);
                cellDefinition.setColumnNumber(j + 1);
                cellDefinition.setRowSpan(rowSpan > 0 ? rowSpan + 1 : rowSpan);
                cellDefinition.setColSpan(colSpan > 0 ? colSpan + 1 : colSpan);
                cellDefinition.setSheetColumnData(columnData);
                cellDefinition.setSheetRowData(rowData);
                cellDefinition.setCellData(sheetCellData);
                cellDefinition.setStyleModel(styleModel);

                //判断函数
                String formulaValue = sheetCellData.getF() != null ? sheetCellData.getF() : "";
                if (StringUtil.isNotEmpty(formulaValue)) {
                    if (formulaValue.startsWith("=")) {
                        List<CellRangeAddress> list = new ArrayList<>();
                        formulaName(formulaValue, list);
                        Map<Integer, Map<Integer, String>> formulaCell = new HashMap<>();
                        for (CellRangeAddress rangeAddress : list) {
                            int startRow = rangeAddress.getFirstRow();
                            int startCol = rangeAddress.getFirstColumn();
                            int endRow = rangeAddress.getLastRow();
                            int endCol = rangeAddress.getLastColumn();
                            List<CellReference> cellsRange = getCellsRange(startRow, endRow, startCol, endCol);
                            List<String> rangeName = new ArrayList<>();
                            for (CellReference reference : cellsRange) {
                                int row = reference.getRow();
                                int col = reference.getCol();
                                Map<Integer, String> rowMap = formulaCell.get(row) != null ? formulaCell.get(row) : new HashMap<>();
                                rowMap.put(col, reference.formatAsString());
                                formulaCell.put(row, rowMap);
                                rangeName.add(reference.formatAsString());
                            }
                            if (cellsRange.size() > 1) {
                                formulaValue = formulaValue.replace(rangeAddress.formatAsString(), String.join(",", rangeName));
                            }
                        }
                        List<String> cellNameList = new ArrayList<>();
                        List<Integer> rowList = new ArrayList<>(formulaCell.keySet()).stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
                        for (Integer row : rowList) {
                            Map<Integer, String> rowMap = formulaCell.get(row) != null ? formulaCell.get(row) : new HashMap<>();
                            List<Integer> colList = new ArrayList<>(rowMap.keySet()).stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
                            for (Integer col : colList) {
                                cellNameList.add(rowMap.get(col));
                            }
                        }
                        Formula formula = new Formula();
                        formula.setFormula(formulaValue);
                        formula.setName(definitionName);
                        formula.setCellNameList(cellNameList);
                        formulaMap.put(definitionName, formula);
                        sheetCellData.setF(formulaValue);
                        sheetCellData.setV(null);
                    }
                }
                //数据类型
                Expand expand = Expand.None;
                UniverDataConfig univerCellData = cell.stream().filter(t -> Objects.equals(t.getRow(), rowDefinition.getRowNumber()) && Objects.equals(t.getCol(), columnDefinition.getColumnNumber())).findFirst().orElse(new UniverDataConfig());
                UniverDataConfig univerDataConfig = univerCellData.getCustom() != null ? univerCellData.getCustom() : new UniverDataConfig();
                String cellType = univerCellData.getType();
                cellDefinition.setType(cellType);
                String value = sheetCellData.getV() != null && SIMPLE_VALUE.contains(cellType) ? sheetCellData.getV().toString() : "";
                Value values = new SimpleValue(value);
                if (Objects.equals(CellDataEnum.dataSource.name(), cellType)) {
                    String datasetName = "";
                    String property = "";
                    String fillDirection = univerDataConfig.getFillDirection();
                    String fileName = univerDataConfig.getField();
                    String summaryType = univerDataConfig.getSummaryType();
                    if (StringUtils.isNotEmpty(fileName)) {
                        String[] params = fileName.split("\\.");
                        if (params.length == 2) {
                            datasetName = params[0];
                            property = params[1];
                        }
                    }
                    String displayType = univerDataConfig.getDisplayType();
                    boolean isQrCode = Objects.equals(ZxingEnum.QRCODE.getType(), displayType);
                    UniverZxingModel qrcode = univerDataConfig.getQrCodeOption();
                    UniverSheetRange range = new UniverSheetRange();
                    range.setStartRow(i);
                    range.setEndRow(i);
                    range.setStartColumn(j);
                    range.setEndColumn(j);
                    UniverZxingModel zxingModel = ZxingUtil.zxingImage(sheetRowData, sheetColumnData, sheet, isRange ? sheetRange : range, displayType);
                    zxingModel.setDisplayType(displayType);
                    if (isQrCode && qrcode != null && qrcode.getColor() != null) {
                        UniverZxingModel color = qrcode.getColor();
                        zxingModel.setBackground(color.getLight());
                        zxingModel.setLineColor(color.getDark());
                        zxingModel.setErrorCorrectionLevel(qrcode.getErrorCorrectionLevel());
                        cellDefinition.setZxing(zxingModel);
                    }
                    UniverZxingModel jsbarCode = univerDataConfig.getJsbarCodeOption();
                    boolean isBarCode = Objects.equals(ZxingEnum.BARCODE.getType(), displayType);
                    if (isBarCode && jsbarCode != null) {
                        zxingModel.setBackground(jsbarCode.getBackground());
                        zxingModel.setLineColor(jsbarCode.getLineColor());
                        zxingModel.setFormat(jsbarCode.getFormat());
                        cellDefinition.setZxing(zxingModel);
                    }
                    AggregateType aggregate = AggregateType.none;
                    Integer type = univerDataConfig.getPolymerizationType();
                    boolean isSelect = Objects.equals(type, UniverDataEnum.select.getCode());
                    boolean isGroup = Objects.equals(type, UniverDataEnum.group.getCode());
                    boolean isSummary = Objects.equals(type, UniverDataEnum.summary.getCode());
                    if (isSelect || isGroup) {
                        expand = Objects.equals(UniverDataEnum.cellDirection.getName(), fillDirection) ? Expand.Down : Expand.Right;
                        aggregate = isGroup ? AggregateType.group : AggregateType.select;
                    } else if (isSummary) {
                        aggregate = AggregateType.value(summaryType);
                    }
                    DatasetValue datasetValue = new DatasetValue();
                    datasetValue.setDatasetName(datasetName);
                    datasetValue.setProperty(property);
                    datasetValue.setAggregate(aggregate);
                    datasetValue.setGroupType(univerDataConfig.getGroupType());
                    values = datasetValue;
                }

                String leftName = "";
                String leftType = univerDataConfig.getLeftParentCellType();
                if (StringUtil.isNotEmpty(leftType)) {
                    UniverDataEnum dataType = UniverDataEnum.getData(leftType);
                    if (dataType != null) {
                        switch (dataType) {
                            case cellNone:
                                leftName = UniverDataEnum.cellRoot.getName();
                                break;
                            case cellCustom:
                                leftName = univerDataConfig.getLeftParentCellCustomRowName() + univerDataConfig.getLeftParentCellCustomColName();
                                break;
                        }
                    }
                }
                cellDefinition.setLeftType(leftType);
                cellDefinition.setLeftParentCellName(leftName);
                String topName = "";
                String topType = univerDataConfig.getTopParentCellType();
                if (StringUtil.isNotEmpty(topType)) {
                    UniverDataEnum dataType = UniverDataEnum.getData(topType);
                    if (dataType != null) {
                        switch (dataType) {
                            case cellNone:
                                topName = UniverDataEnum.cellRoot.getName();
                                break;
                            case cellCustom:
                                topName = univerDataConfig.getTopParentCellCustomRowName() + univerDataConfig.getTopParentCellCustomColName();
                                break;
                        }
                    }
                }
                cellDefinition.setTopType(topType);
                cellDefinition.setTopParentCellName(topName);
                cellDefinition.setExpand(expand);
                cellDefinition.setValue(values);
                boolean notAdd = mergeData.stream().anyMatch(t -> t.getStartRow() <= rowDefinition.getRowNumber() && t.getStartColumn() <= columnDefinition.getColumnNumber() && t.getEndRow() >= rowDefinition.getRowNumber() && t.getEndColumn() >= columnDefinition.getColumnNumber());
                if (!notAdd || isRange) {
                    cells.add(cellDefinition);
                }
                columnDefinition.setColumnNumber(columnDefinition.getColumnNumber() + 1);
                if (columns.stream().noneMatch(t -> Objects.equals(t.getColumnNumber(), columnDefinition.getColumnNumber()))) {
                    columns.add(columnDefinition);
                }
                Boolean fillEmptyRows = univerDataConfig.getFillEmptyRows();
                Integer fillEmptyNum = univerDataConfig.getFillEmptyNum();
                if (fillEmptyRows && fillEmptyNum > 0) {
                    Map<Integer, Integer> addRow = fillRowMap.get(i) != null ? fillRowMap.get(i) : new HashMap<>();
                    addRow.put(j, fillEmptyNum);
                    fillRowMap.put(i, addRow);
                }
            }
            rowDefinition.setRowNumber(rowDefinition.getRowNumber() + 1);
            rows.add(rowDefinition);
        }
    }

    private UniverSheet sheet(UniverSheet sheet, Report report, Map<String, Formula> formulaMap, ColumnList columnList, Map<String, Map<Integer, Map<Integer, String>>> cellNameMapAll) {
        int rowSize = report.getRows().size();
        int columnSize = report.getColumns().size();
        if (BeanUtil.isNotEmpty(columnList) && columnList.isColumnState()) {
            buildColumn(report, columnList);
//            buildFenceReport(report, columnList);
        }
        sheet.setCellData(new HashMap<>());
        sheet.setMergeData(new ArrayList<>());
        sheet.setRowData(new HashMap<>());
        sheet.setColumnData(new HashMap<>());
        Map<Integer, UniverSheetRowData> rowData = new HashMap<>();
        Map<Integer, UniverSheetColumnData> columnData = new HashMap<>();
        UniverSheet result = JsonUtil.getJsonToBean(sheet, UniverSheet.class);
        List<Row> rows = report.getRows();
        List<Column> columns = report.getColumns();
        Map<Row, Map<Column, Cell>> cellMap = report.getRowColCellMap();
        Map<Integer, Map<Integer, UniverSheetCellData>> cellData = new HashMap<>();
        Map<Integer, Map<Integer, Cell>> formulaCellMap = new HashMap<>();
        List<UniverSheetRange> mergeData = new ArrayList<>();
        //获取冻结位置
        UniverSheetFreeze sheetFreeze = sheet.getFreeze() != null ? sheet.getFreeze() : new UniverSheetFreeze();
        int freezeRow = sheetFreeze.getStartRow();
        List<Integer> freezeRowList = new ArrayList<>();
        freezeRowList.add(freezeRow);
        int freezeColumn = sheetFreeze.getStartColumn();
        List<Integer> freezeColumnList = new ArrayList<>();
        freezeColumnList.add(freezeColumn);
        //绘制格子
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            rowData.put(i, row.getSheetRowData());
            if (row.getFreeze()) {
                freezeRowList.add(i + 1);
            }
            for (int j = 0; j < columns.size(); j++) {
                Column col = columns.get(j);
                Cell cell = null;
                if (cellMap.containsKey(row)) {
                    Map<Column, Cell> colMap = cellMap.get(row);
                    if (colMap.containsKey(col)) {
                        cell = colMap.get(col);
                    }
                }
                if (cell == null) {
                    continue;
                }
                if (col.getFreeze()) {
                    freezeColumnList.add(j + 1);
                }
                cell.setReference(new CellReference(i, j));
                UniverSheetCellData sheetCellData = JsonUtil.getJsonToBean(cell.getCellData(), UniverSheetCellData.class);
                if (BeanUtil.isEmpty(sheetCellData)) {
                    sheetCellData = new UniverSheetCellData();
                }

                //获取函数格子
                String cellName = cell.getName();
                if (formulaMap.get(cellName) != null) {
                    Map<Integer, Cell> data = formulaCellMap.get(i) != null ? formulaCellMap.get(i) : new HashMap<>();
                    data.put(j, cell);
                    formulaCellMap.put(i, data);
                }

                //合并格子
                int startRow = i;
                int endRow = i + (cell.getRowSpan() > 0 ? cell.getRowSpan() - 1 : cell.getRowSpan());
                int startCol = j;
                int endCol = j + (cell.getColSpan() > 0 ? cell.getColSpan() - 1 : cell.getColSpan());
                int rowSpan = cell.getRowSpan();
                int colSpan = cell.getColSpan();
                StyleModel styleModel = cell.getStyleModel();
                if (colSpan > 0 || rowSpan > 0) {
                    //左、右边框
                    boolean isL = ObjectUtil.isNotEmpty(styleModel.getL());
                    boolean isR = ObjectUtil.isNotEmpty(styleModel.getR());
                    //当合并数据大于数据限制
//                    if (BeanUtil.isNotEmpty(columnList) && columnList.isColumnState()
//                            && columnList.getColumnStyle().equals("col")
//                            && endRow > columnList.getRowMaxNum()
//                            && startRow<= columnList.getRowMaxNum()
//                            && startRow>=columnList.getRowMinNum()) {
//                        //首先计算总共合并的选项
//                        int totalNum = endRow - startRow + 1;
//                        //当前行的合并数量
//                        int currentNum = columnList.getRowMaxNum() - startRow + 1;
//                        //分栏中某一栏的数据数量
//                        int fenceNum = columnList.getRowMaxNum() - columnList.getRowMinNum() + 1;
//                        //先填充当前栏,计算剩下的数据
//                        int remainNum = totalNum - currentNum;
//
//                        int resultNum = remainNum % fenceNum == 0 ? remainNum / fenceNum : remainNum / fenceNum + 1;
//                        if (resultNum == 1) {
//                            int needNum = 1;
//                            makeRowRange(cellData, j, columnSize, needNum, sheetCellData, remainNum, mergeData, columnList);
//                        } else {
//                            for (int m = 0; m < resultNum; m++) {
//
//                                if (m == resultNum - 1) {
//                                    int lastNum = remainNum % fenceNum;
//                                    makeRowRange(cellData, j, columnSize, m + 1, sheetCellData, lastNum, mergeData, columnList);
//                                } else {
//                                    makeRowRange(cellData, j, columnSize, m + 1, sheetCellData, fenceNum, mergeData, columnList);
//                                }
//                            }
//                        }
//                        endRow = columnList.getRowMaxNum();
//                    }
                    for (int rowCount = startRow; rowCount <= endRow; rowCount++) {
                        Map<Integer, UniverSheetCellData> dataMap = cellData.get(rowCount) != null ? cellData.get(rowCount) : new HashMap<>();
                        UniverSheetCellData startData = dataMap.get(startCol) != null ? dataMap.get(startCol) : new UniverSheetCellData();
                        startData.setS(isL ? styleModel.getL() : sheetCellData.getS());
                        dataMap.put(startCol, startData);

                        UniverSheetCellData endData = dataMap.get(endCol) != null ? dataMap.get(endCol) : new UniverSheetCellData();
                        endData.setS(isR ? styleModel.getR() : sheetCellData.getS());
                        dataMap.put(endCol, endData);

                        cellData.put(rowCount, dataMap);
                    }

                    //上、下边框
                    boolean isT = ObjectUtil.isNotEmpty(styleModel.getT());
                    boolean isB = ObjectUtil.isNotEmpty(styleModel.getB());
//                    if (BeanUtil.isNotEmpty(columnList) && columnList.isColumnState()
//                            && columnList.getColumnStyle().equals("row") && endCol > columnList.getColMaxNum()
//                            && startCol<= columnList.getColMaxNum()
//                    &&startCol>=columnList.getColMinNum()) {
//                        //首先计算总共合并的选项
//                        int totalNum = endCol - startCol + 1;
//                        //当前列的合并数量
//                        int currentNum = columnList.getColMaxNum() - startCol + 1;
//                        //分栏中某一栏的数据数量
//                        int fenceNum = columnList.getColMaxNum() - columnList.getColMinNum() + 1;
//                        //先填充当前栏,计算剩下的数据
//                        int remainNum = totalNum - currentNum;
//
//                        int resultNum = remainNum % fenceNum == 0 ? remainNum / fenceNum : remainNum / fenceNum + 1;
//                        if (resultNum == 1) {
//                            makeColRange(cellData, sheetCellData, remainNum, mergeData, columnList, i + rowSize);
//                        } else {
//                            for (int m = 0; m < resultNum; m++) {
//
//                                if (m == resultNum - 1) {
//                                    int lastNum = remainNum % fenceNum;
//                                    makeColRange(cellData, sheetCellData, lastNum, mergeData, columnList, i + rowSize * (m + 1));
//                                } else {
//                                    makeColRange(cellData, sheetCellData, fenceNum, mergeData, columnList, i + rowSize * (m + 1));
//                                }
//                            }
//                        }
//                        endCol = columnList.getColMaxNum();
//                    }
                    for (int columnCount = startCol; columnCount <= endCol; columnCount++) {
                        Map<Integer, UniverSheetCellData> startDataMap = cellData.get(startRow) != null ? cellData.get(startRow) : new HashMap<>();
                        UniverSheetCellData startData = startDataMap.get(columnCount) != null ? startDataMap.get(columnCount) : new UniverSheetCellData();
                        startData.setS(isT ? styleModel.getT() : sheetCellData.getS());
                        startDataMap.put(columnCount, startData);
                        cellData.put(startRow, startDataMap);

                        Map<Integer, UniverSheetCellData> endDataMap = cellData.get(endRow) != null ? cellData.get(endRow) : new HashMap<>();
                        UniverSheetCellData endData = endDataMap.get(columnCount) != null ? endDataMap.get(columnCount) : new UniverSheetCellData();
                        endData.setS(isB ? styleModel.getB() : sheetCellData.getS());
                        endDataMap.put(columnCount, endData);
                        cellData.put(endRow, endDataMap);
                    }
                    //角样式
                    boolean isLT = ObjectUtil.isNotEmpty(styleModel.getLt());
                    boolean isRT = ObjectUtil.isNotEmpty(styleModel.getRt());
                    Map<Integer, UniverSheetCellData> startDataMap = cellData.get(startRow) != null ? cellData.get(startRow) : new HashMap<>();
                    UniverSheetCellData leftTopData = startDataMap.get(startCol) != null ? startDataMap.get(startCol) : new UniverSheetCellData();
                    leftTopData.setS(isLT ? styleModel.getLt() : sheetCellData.getS());
                    startDataMap.put(startCol, leftTopData);
                    UniverSheetCellData rightTopData = startDataMap.get(endCol) != null ? startDataMap.get(endCol) : new UniverSheetCellData();
                    rightTopData.setS(isRT ? styleModel.getRt() : sheetCellData.getS());
                    startDataMap.put(endCol, rightTopData);
                    cellData.put(startRow, startDataMap);

                    boolean isLB = ObjectUtil.isNotEmpty(styleModel.getLb());
                    boolean isRB = ObjectUtil.isNotEmpty(styleModel.getRb());
                    Map<Integer, UniverSheetCellData> endDataMap = cellData.get(endRow) != null ? cellData.get(endRow) : new HashMap<>();
                    UniverSheetCellData leftBottomData = endDataMap.get(startCol) != null ? endDataMap.get(startCol) : new UniverSheetCellData();
                    leftBottomData.setS(isLB ? styleModel.getLb() : sheetCellData.getS());
                    endDataMap.put(startCol, leftBottomData);
                    UniverSheetCellData rightBottomData = endDataMap.get(endCol) != null ? endDataMap.get(endCol) : new UniverSheetCellData();
                    rightBottomData.setS(isRB ? styleModel.getRb() : sheetCellData.getS());
                    endDataMap.put(endCol, rightBottomData);
                    cellData.put(endRow, endDataMap);

                    Object sheetData = cell.getData();
                    String type = cell.getType();
                    boolean notRange = mergeData.stream().anyMatch(t -> t.getStartRow() <= startRow && t.getStartColumn() <= startCol && t.getEndRow() >= startRow && t.getEndColumn() >= startCol);
                    //如果是图表，sheetData值又是null，不合并
                    boolean notCellChart = CellDataEnum.cellChart.name().equals(type) && sheetData == null;
                    if (!notRange && !notCellChart) {
                        UniverSheetRange range = new UniverSheetRange();
                        range.setStartRow(startRow);
                        range.setEndRow(endRow);
                        range.setStartColumn(startCol);
                        range.setEndColumn(endCol);
                        mergeData.add(range);
                    }

                }

                //每个格子坐标
                Map<Integer, Map<Integer, String>> cellNameMap = cellNameMapAll.get(cellName) != null ? cellNameMapAll.get(cellName) : new HashMap<>();
                for (int rowNum = startRow; rowNum <= endRow; rowNum++) {
                    Map<Integer, String> rowMap = cellNameMap.get(rowNum) != null ? cellNameMap.get(rowNum) : new HashMap<>();
                    for (int colNum = startCol; colNum <= endCol; colNum++) {
                        rowMap.put(colNum, cell.getReference().formatAsString());
                    }
                    cellNameMap.put(rowNum, rowMap);
                }
                cellNameMapAll.put(cellName, cellNameMap);

                //赋值
                Object sheetData = cell.getData();
                int dataType = CellDataTypeEnum.String.getCode();
//                try {
//                    String value = String.valueOf(sheetData);
//                    new BigDecimal(value);
//                    if (value.length() < 16) {
//                        dataType = CellDataTypeEnum.Number.getCode();
//                    }
//                } catch (Exception ignored) {
//                }
                if (sheetData instanceof Number) {
                    dataType = CellDataTypeEnum.Number.getCode();
                }
                if (sheetData instanceof Boolean) {
                    dataType = CellDataTypeEnum.Boolean.getCode();
                }
                if (ObjectUtil.isNotEmpty(sheetCellData.getF())) {
                    dataType = CellDataTypeEnum.Formula.getCode();
                }
                sheetCellData.setT(dataType);
                sheetCellData.setV(sheetData);
                sheetCellData.setP(sheetData == null ? null : sheetCellData.getP());
                //判断超链接
                if (ObjectUtil.isNotEmpty(sheetCellData.getP()) && !("text").equals(cell.getType())) {
                    Object p = sheetCellData.getP();
                    UniverProperties jsonToBean = JsonUtil.getJsonToBean(p, UniverProperties.class);
                    if (BeanUtil.isNotEmpty(jsonToBean) && CollectionUtil.isEmpty(jsonToBean.getDrawings())) {
                        UniverBody body = jsonToBean.getBody();
                        if (BeanUtil.isNotEmpty(body)) {
                            body.setDataStream(ObjectUtil.isNotEmpty(sheetData) ? sheetData.toString() + "\r\n" : "\r\n");
                            //需要将数据进行修改
                            if (ObjectUtil.isNotEmpty(sheetData)) {
                                String string = sheetData.toString();
                                List<UniverBodyConfig> sectionBreaks = body.getSectionBreaks();
                                if (CollectionUtil.isNotEmpty(sectionBreaks)) {
                                    sectionBreaks.get(0).setStartIndex(string.length() + 1);
                                }
                                List<UniverBodyConfig> textRuns = body.getTextRuns();
                                if (CollectionUtil.isNotEmpty(textRuns)) {
                                    textRuns.get(0).setEd(string.length());
                                }
                                List<UniverBodyConfig> paragraphs = body.getParagraphs();
                                if (CollectionUtil.isNotEmpty(paragraphs)) {
                                    paragraphs.get(0).setStartIndex(string.length() + 1);
                                }
                                List<UniverBodyConfig> customRanges = body.getCustomRanges();
                                if (CollectionUtil.isNotEmpty(customRanges)) {
                                    customRanges.get(0).setEndIndex(string.length() - 1);
                                }
                            }
                            sheetCellData.setP(JSONObject.toJSON(jsonToBean));
                        }
                    }
                }
                UniverZxingModel zxing = cell.getZxing();
                if (zxing != null) {
                    UniverZxingModel zxingModel = JsonUtil.getJsonToBean(zxing, UniverZxingModel.class);
                    zxingModel.setText(sheetData + "");
                    UniverProperties properties = ImageUtil.zxing(zxingModel);
                    sheetCellData.setP(properties);
                    sheetCellData.setV("");
                }

                columnData.put(j, col.getSheetColumnData());
                Map<Integer, UniverSheetCellData> cellDataMap = cellData.get(i) != null ? cellData.get(i) : new HashMap<>();
                if (!cellDataMap.containsKey(j) || null == cellDataMap.get(j).getV()) {
                    cellDataMap.put(j, sheetCellData);
                }
                cellData.put(i, cellDataMap);
            }
        }


        //修改函数
        List<Integer> rowList = new ArrayList<>(formulaCellMap.keySet()).stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
        for (Integer row : rowList) {
            Map<Integer, Cell> rowMap = formulaCellMap.get(row) != null ? formulaCellMap.get(row) : new HashMap<>();
            List<Integer> colList = new ArrayList<>(rowMap.keySet()).stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
            for (Integer col : colList) {
                Cell cell = rowMap.get(col);
                Formula formula = formulaMap.get(cell.getName());
                if (formula != null) {
                    if (formula.getCellName().isEmpty()) {
                        List<String> functionName = formula.getCellNameList();
                        Map<String, Map<Integer, Map<Integer, String>>> cellName = new HashMap<>();
                        for (String name : functionName) {
                            Map<Integer, Map<Integer, String>> functionCellMap = cellNameMapAll.get(name) != null ? cellNameMapAll.get(name) : new HashMap<>();
                            Map<Integer, Map<Integer, String>> cellDataMap = new HashMap<>();
                            for (Integer rowNum : functionCellMap.keySet()) {
                                Map<Integer, String> colMap = new HashMap<>(functionCellMap.get(rowNum));
                                cellDataMap.put(rowNum, colMap);
                            }
                            cellName.put(name, cellDataMap);
                        }
                        formula.setCellName(cellName);
                    }
                    formula.setRowNumber(row);
                    formula.setColNumber(col);
                    formulaList(cell, formula, report);
                    //赋值
                    UniverSheetCellData data = JsonUtil.getJsonToBean(cell.getCellData(), UniverSheetCellData.class);
                    data.setT(CellDataTypeEnum.Formula.getCode());
                    Map<Integer, UniverSheetCellData> cellDataMap = cellData.get(row) != null ? cellData.get(row) : new HashMap<>();
                    cellDataMap.put(col, data);
                    cellData.put(row, cellDataMap);
                }
            }
        }
        //赋值数据
        result.setCellData(cellData);
        result.setMergeData(mergeData);
        result.setRowData(rowData);
        result.setColumnData(columnData);
        List<Integer> rowMax = new ArrayList<>();
        rowMax.add(sheet.getRowCount());
        rowMax.add(Collections.max(new ArrayList<>(rowData.keySet())) + 1);
        result.setRowCount(Collections.max(rowMax));
        List<Integer> columnMax = new ArrayList<>();
        columnMax.add(sheet.getColumnCount());
        columnMax.add(Collections.max(new ArrayList<>(columnData.keySet())) + 1);
        result.setColumnCount(Collections.max(columnMax));

        //赋值冻结数据
        UniverSheetFreeze freeze = new UniverSheetFreeze();
        freeze.setStartRow(Collections.max(freezeRowList));
        freeze.setStartColumn(Collections.max(freezeColumnList));
        freeze.setYSplit(freeze.getStartRow() > -1 ? freeze.getStartRow() : 0);
        freeze.setXSplit(freeze.getStartColumn() > -1 ? freeze.getStartColumn() : 0);
        result.setFreeze(freeze);
        return result;
    }

    private void makeColRange(Map<Integer, Map<Integer, UniverSheetCellData>> cellData
            , UniverSheetCellData sheetCellData, int remainNum, List<UniverSheetRange> mergeData
            , ColumnList columnList, int i) {
        int startNum = 0;
        int endNum = 0;
        int num = 0;


        Map<Integer, UniverSheetCellData> integerUniverSheetCellDataMap = new HashMap<>();
        int integer = 0;
        while (num < remainNum) {

            if (integer < columnList.getColMinNum() || integer > columnList.getColMaxNum()) {
                integer++;
                continue;
            }
            if (num == 0) {
                startNum = integer;
                integerUniverSheetCellDataMap.put(integer, sheetCellData);
            } else {
                UniverSheetCellData univerSheetCellData = new UniverSheetCellData();
                integerUniverSheetCellDataMap.put(integer, univerSheetCellData);
            }
            integer++;
            num++;
            if (num >= remainNum) {
                endNum = integer - 1;
            }
        }
        cellData.put(i, integerUniverSheetCellDataMap);

        UniverSheetRange univerSheetRange = new UniverSheetRange();
        univerSheetRange.setStartRow(i);
        univerSheetRange.setEndRow(i);
        univerSheetRange.setStartColumn(startNum);
        univerSheetRange.setEndColumn(endNum);
        mergeData.add(univerSheetRange);
    }

    /**
     * 分栏分组的情况下，创建合并数据
     *
     * @param cellData
     * @param j
     * @param columnSize
     * @param needNum
     * @param sheetCellData
     * @param max
     * @param mergeData
     */
    private static void makeRowRange(Map<Integer, Map<Integer, UniverSheetCellData>> cellData, int j, int columnSize, int needNum, UniverSheetCellData sheetCellData,
                                     int max, List<UniverSheetRange> mergeData, ColumnList columnList) {
        int startNum = 0;
        int endNum = 0;
        int num = 0;

        for (Integer integer = columnList.getRowMinNum(); integer <= columnList.getRowMaxNum(); integer++) {

            Map<Integer, UniverSheetCellData> integerUniverSheetCellDataMap = cellData.get(integer);
            if (CollectionUtil.isEmpty(integerUniverSheetCellDataMap)) {
                integerUniverSheetCellDataMap = new HashMap<>();
                cellData.put(integer, integerUniverSheetCellDataMap);
            }
            if (num == 0) {
                startNum = integer;
                integerUniverSheetCellDataMap.put(j + columnSize * needNum, sheetCellData);
            } else {
                UniverSheetCellData univerSheetCellData = new UniverSheetCellData();
                integerUniverSheetCellDataMap.put(j + columnSize * needNum, univerSheetCellData);
            }
            num++;
            if (num >= max) {
                endNum = integer;
                break;
            }
        }
        UniverSheetRange univerSheetRange = new UniverSheetRange();
        univerSheetRange.setStartRow(startNum);
        univerSheetRange.setEndRow(endNum);
        univerSheetRange.setStartColumn(j + columnSize * needNum);
        univerSheetRange.setEndColumn(j + columnSize * needNum);
        mergeData.add(univerSheetRange);
    }


    private void buildColumn(Report report, ColumnList columnModel) {
        if (columnModel == null) {
            return;
        }
        if (!columnModel.isColumnState()) {
            return;
        }
        Map<String, List<Cell>> cellsMap = report.getCellsMap();
        Map<Row, Map<Column, Cell>> rowColCellMap = report.getRowColCellMap();
        List<Column> columns = report.getColumns();
        List<Row> rows = report.getRows();
        String copyCol = columnModel.getCopyCol();
        String copyRow = columnModel.getCopyRow();
        Integer rowCount = columnModel.getRowCount();
        Integer maxCol = columnModel.getMaxCol();
        Integer colCount = columnModel.getColCount();
        Integer maxRow = columnModel.getMaxRow();
        boolean isCol = Objects.equals(ColumnEnum.col.getType(), columnModel.getColumnStyle());
        boolean isType = Objects.equals(ColumnEnum.columnType.getType(), columnModel.getColumnType());
        String copyNum = isCol ? copyCol : copyRow;
        Map<Integer, Integer> copyMap = new HashMap<>();
        if (StringUtil.isNotEmpty(copyNum)) {
            String[] numOne = copyNum.split(",");
            for (String string : numOne) {
                String[] numTwo = string.split("-");
                for (String s : numTwo) {
                    try {
                        Integer num = Integer.valueOf(s);
                        copyMap.put(num, num);
                    } catch (Exception e) {
                        System.out.println("复制的格子有问题");
                        return;
                    }
                }
            }
        }

        //分栏数据的处理
        String columnData = columnModel.getColumnData();
        CellRangeAddress cellAddresses;
        try {
            cellAddresses = CellRangeAddress.valueOf(columnData);
        } catch (Exception e) {
            System.out.println("分栏的格子有问题");
            return;
        }


        //获取分栏的数据格子
        Map<String, String> cellName = new HashMap<>();
        //处理数据分成多少栏
        List<Integer> dataList = new ArrayList<>();
        dataList.add(0);
        int firstRow = cellAddresses.getFirstRow();
        int lastRow = cellAddresses.getLastRow();
        int firstColumn = cellAddresses.getFirstColumn();
        int lastColumn = cellAddresses.getLastColumn();
        int columnOffset = lastColumn - firstColumn;
        int rowOffset = lastRow - firstRow;
        for (int rowNum = firstRow; rowNum <= lastRow; rowNum++) {
            for (int colNum = firstColumn; colNum <= lastColumn; colNum++) {
                CellReference reference = new CellReference(rowNum, colNum);
                String format = reference.formatAsString();
                List<Cell> cells = cellsMap.get(format) != null ? cellsMap.get(format) : new ArrayList<>();
                dataList.add(cells.size());
                cellName.put(format, format);
            }
        }

        //规定
        List<Column> beforeColList = new ArrayList<>();
        List<Column> afterColList = new ArrayList<>();
        List<Row> beforeRowList = new ArrayList<>();
        List<Row> afterRowList = new ArrayList<>();

        //最大数量
        int dataSize = Collections.max(dataList);
        //最大值
        int fenceNum = isCol ? maxCol : maxRow;
        if (isCol) {
            List<Column> beforeList = columns.subList(0, lastColumn + 1);
            beforeColList = new ArrayList<>(beforeList);
            List<Column> afterList = columns
                    .subList(Math.min((lastColumn + 1), (columns.size() - 1)), columns.size() - 1);
            afterColList = new ArrayList<>(afterList);
            beforeRowList = rows;
        } else {
            List<Row> beforeList = rows.subList(0, lastRow + 1);
            beforeRowList = new ArrayList<>(beforeList);
            List<Row> afterList = rows
                    .subList(Math.min((lastRow + 1), (rows.size() - 1)), rows.size() - 1);
            afterRowList = new ArrayList<>(afterList);
            beforeColList = columns;
        }
        if (isType) {
            if (isCol) {
                if (rowCount < 1) {
                    return;
                }
                fenceNum = (dataSize / rowCount) + (dataSize % rowCount == 0 ? 0 : 1);
            } else {
                if (colCount < 1) {
                    return;
                }
                fenceNum = (dataSize / colCount) + (dataSize % colCount == 0 ? 0 : 1);
            }
        }
        if (fenceNum < 1 || dataSize < fenceNum) {
            return;
        }
        //移动几行
        int fenceCount = (dataSize / fenceNum) + (dataSize % fenceNum == 0 ? 0 : 1);
        //格子的第一个位置
        Map<String, Map<String, Integer>> cellFirst = new HashMap<>();
        //格子的数量
        Map<String, Integer> count = new HashMap<>();

        //添加的格子
        Map<Integer, Column> addColumnList = new HashMap<>();
        Map<Integer, Row> addRowList = new HashMap<>();
        Map<Row, Map<Column, Cell>> rowColMap = new HashMap<>();
        //删除的格子
        Map<Row, Map<Column, Cell>> delColList = new HashMap<>();
        //添加第几行
        Map<String, Integer> numMap = new HashMap<>();
        Map<String, Integer> cellNum = new HashMap<>();

        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            for (int j = 0; j < columns.size(); j++) {
                Column col = columns.get(j);
                Cell cell = null;
                if (rowColCellMap.containsKey(row)) {
                    Map<Column, Cell> colMap = rowColCellMap.get(row);
                    if (colMap.containsKey(col)) {
                        cell = colMap.get(col);
                    }
                }
                if (cell == null) {
                    continue;
                }
                String name = cell.getName();
                Map<String, Integer> map = new HashMap<>();
                map.put(ColumnEnum.row.getType(), i);
                map.put(ColumnEnum.col.getType(), j);
                Map<String, Integer> firstMap = cellFirst.get(name) != null ? cellFirst.get(name) : map;
                cellFirst.put(name, firstMap);

                Value value = cell.getValue();
                //分栏数据
                if (cellName.get(name) != null) {
//                    if (!(value instanceof DatasetValue)) {
//                        continue;
//                    }
                    List<Integer> spanList = new ArrayList<>();
                    int countNum = count.get(name) != null ? count.get(name) : 0;
                    int num = numMap.get(name) != null ? numMap.get(name) : 0;
                    int addCount = cellNum.get(name) != null ? cellNum.get(name) : 0;
                    if (isCol) {
                        if (columns.indexOf(col) > lastColumn || columns.indexOf(col) < firstColumn) {
                            continue;
                        }
                        int rowSpan = cell.getRowSpan();
                        int total = rowSpan > 0 ? rowSpan : 1;

                        //重新计算合并格子
                        Map<Integer, Integer> addMap = new HashMap<>();
                        Map<Integer, Integer> spanMap = new HashMap<>();
                        span(total, num, fenceNum, addMap, spanMap, rowSpan);

                        //分栏数据调整
                        for (int k = 0; k < total; k++) {
                            spanList.add(addCount);
                            if (addMap.get(k) != null) {
                                int span = spanMap.get(k) != null ? spanMap.get(k) : rowSpan;
                                if (addCount > 0) {
                                    //获取数据
                                    int addRow = firstMap.get(ColumnEnum.row.getType()) + num;
                                    Row cellRow = rows.size() > addRow && rows.get(addRow) != null ? rows.get(addRow) : new Row(new ArrayList<>());
                                    Map<Column, Cell> addColumnMap = rowColMap.get(cellRow) != null ? rowColMap.get(cellRow) : new HashMap<>();
                                    int addColumn = firstMap.get(ColumnEnum.col.getType()) + ((columnOffset + 1) * addCount) + OFFSET * addCount;
                                    //添加数据
                                    Cell newCell = cell.newCell();
                                    newCell.setData(cell.getData());
                                    newCell.setRowSpan(span);
                                    Column columnsModel = addColumnList.get(addColumn) != null ? addColumnList.get(addColumn) : new Column(new ArrayList<>());
                                    if (null == addColumnList.get(addColumn)) {
                                        columnsModel.setSheetColumnData(col.getSheetColumnData());
                                    }
                                    columnsModel.getCells().add(newCell);
                                    addColumnList.put(addColumn, columnsModel);
                                    addColumnMap.put(columnsModel, newCell);
                                    rowColMap.put(cellRow, addColumnMap);
                                } else {
                                    cell.setRowSpan(span);
                                }
                            }
                            num += 1;
                            if (num >= fenceNum) {
                                addCount += 1;
                                num = 0;
                            }
                        }

                    } else {
                        if (rows.indexOf(row) > lastRow || rows.indexOf(row) < firstRow) {
                            continue;
                        }
                        int colSpan = cell.getColSpan();
                        int total = colSpan > 0 ? colSpan : 1;

                        //重新计算合并格子
                        Map<Integer, Integer> addMap = new HashMap<>();
                        Map<Integer, Integer> spanMap = new HashMap<>();
                        span(total, num, fenceNum, addMap, spanMap, colSpan);

                        //分栏数据调整
                        for (int k = 0; k < total; k++) {
                            spanList.add(addCount);
                            if (addMap.get(k) != null) {
                                int span = spanMap.get(k) != null ? spanMap.get(k) : colSpan;
                                if (addCount > 0) {
                                    //获取数据
                                    int addRow = firstMap.get(ColumnEnum.row.getType()) + ((rowOffset + 1) * addCount) + OFFSET * addCount;
                                    Row cellRow = addRowList.get(addRow) != null ? addRowList.get(addRow) : new Row(new ArrayList<>());
                                    if (null == addRowList.get(addRow)) {
                                        cellRow.setSheetRowData(row.getSheetRowData());
                                    }
                                    int addColumn = firstMap.get(ColumnEnum.col.getType()) + num;
                                    Map<Column, Cell> addColumnMap = rowColMap.get(cellRow) != null ? rowColMap.get(cellRow) : new HashMap<>();
                                    //添加数据
                                    Cell newCell = cell.newCell();
                                    newCell.setData(cell.getData());
                                    newCell.setColSpan(span);
                                    Column column = columns.size() > addColumn ? columns.get(addColumn) : new Column(new ArrayList<>());
                                    if (null == columns.get(addColumn)) {
                                        column.setSheetColumnData(col.getSheetColumnData());
                                    }
                                    Map<Column, Cell> columnMap = new HashMap<>();
                                    columnMap.put(column, newCell);
                                    addColumnMap.putAll(columnMap);
                                    addRowList.put(addRow, cellRow);
                                    rowColMap.put(cellRow, addColumnMap);
                                } else {
                                    cell.setColSpan(span);
                                }
                            }
                            num += 1;
                            if (num >= fenceNum) {
                                addCount += 1;
                                num = 0;
                            }
                        }

                    }
                    countNum += 1;
                    cellNum.put(name, addCount);
                    numMap.put(name, num);
                    count.put(name, countNum);

                    //删除超过的数据
                    if (!spanList.isEmpty()) {
                        int minAddCount = Collections.min(spanList);
                        if (minAddCount > 0) {
                            //删除数据
                            Map<Column, Cell> delete = delColList.get(row) != null ? delColList.get(row) : new HashMap<>();
                            delete.put(col, cell);
                            delColList.put(row, delete);
                        }
                    }

                    //补充空白行
                    if (columnModel.isFillEmptyRows()) {
                        //最后一行
                        List<Cell> cells = cellsMap.get(name) != null ? cellsMap.get(name) : new ArrayList<>();
                        if (cells.size() == countNum) {
                            int fillNum = numMap.get(name) != null ? numMap.get(name) : 0;
                            if (fillNum > 0) {
                                if (isCol) {
                                    for (int k = fillNum; k < fenceNum; k++) {
                                        //获取数据
                                        int addColumn = firstMap.get(ColumnEnum.col.getType()) + ((columnOffset + 1) * addCount) + OFFSET * addCount;
                                        int addRow = firstMap.get(ColumnEnum.row.getType()) + k;
                                        Row cellRow = rows.size() > addRow && rows.get(addRow) != null ? rows.get(addRow) : new Row(new ArrayList<>());
                                        Map<Column, Cell> addColumnMap = rowColMap.get(cellRow) != null ? rowColMap.get(cellRow) : new HashMap<>();
                                        //添加数据
                                        Cell filleCcell = cell.newCell();
                                        filleCcell.setColSpan(0);
                                        filleCcell.setRowSpan(0);
                                        filleCcell.setData(null);
                                        Column columnsModel = addColumnList.get(addColumn) != null ? addColumnList.get(addColumn) : new Column(new ArrayList<>());
                                        columnsModel.getCells().add(filleCcell);
                                        addColumnList.put(addColumn, columnsModel);
                                        addColumnMap.put(columnsModel, filleCcell);
                                        rowColMap.put(cellRow, addColumnMap);
                                    }
                                } else {
                                    for (int k = fillNum; k < fenceNum; k++) {
                                        //获取数据
                                        int addColumn = firstMap.get(ColumnEnum.col.getType()) + k;
                                        int addRow = firstMap.get(ColumnEnum.row.getType()) + ((rowOffset + 1) * addCount) + OFFSET * addCount;
                                        Row cellRow = addRowList.get(addRow) != null ? addRowList.get(addRow) : new Row(new ArrayList<>());
                                        Map<Column, Cell> addColumnMap = rowColMap.get(cellRow) != null ? rowColMap.get(cellRow) : new HashMap<>();
                                        Cell filleCcell = cell.newCell();
                                        filleCcell.setColSpan(0);
                                        filleCcell.setRowSpan(0);
                                        filleCcell.setData(null);
                                        //添加数据
                                        Column column = columns.size() > addColumn ? columns.get(addColumn) : new Column(new ArrayList<>());
                                        Map<Column, Cell> columnMap = new HashMap<>();
                                        columnMap.put(column, filleCcell);
                                        addColumnMap.putAll(columnMap);
                                        addRowList.put(addRow, cellRow);
                                        rowColMap.put(cellRow, addColumnMap);
                                    }
                                }
                            }
                        }
                    }
                }

                //复制数据
                if (!copyMap.isEmpty()) {
                    if (value instanceof SimpleValue) {
                        CellReference reference = new CellReference(name);
                        int referenceCol = reference.getCol() + 1;
                        int referenceRow = reference.getRow() + 1;
                        if (isCol) {
                            if (columns.indexOf(col) > lastColumn || columns.indexOf(col) < firstColumn) {
                                continue;
                            }
                            if (copyMap.get(referenceRow) != null) {
                                for (int k = 1; k < fenceCount; k++) {
                                    //获取数据
                                    Map<Column, Cell> addColumnMap = rowColMap.get(row) != null ? rowColMap.get(row) : new HashMap<>();
                                    int addColumn = j + ((columnOffset + 1) * k) + OFFSET * k;
                                    //添加数据
                                    Column columnsModel = addColumnList.get(addColumn) != null ? addColumnList.get(addColumn) : new Column(new ArrayList<>());
                                    if (null == addColumnList.get(addColumn)) {
                                        columnsModel.setSheetColumnData(col.getSheetColumnData());
                                    }
                                    columnsModel.getCells().add(cell);
                                    addColumnList.put(addColumn, columnsModel);
                                    addColumnMap.put(columnsModel, cell);
                                    rowColMap.put(row, addColumnMap);
                                }
                            }
                        } else {
                            if (rows.indexOf(row) > lastRow || rows.indexOf(row) < firstRow) {
                                continue;
                            }
                            if (copyMap.get(referenceCol) != null) {
                                for (int k = 1; k < fenceCount; k++) {
                                    //获取数据
                                    int addRow = i + ((rowOffset + 1) * k) + OFFSET * k;
                                    Row rowModel = addRowList.get(addRow) != null ? addRowList.get(addRow) : new Row(new ArrayList<>());
                                    if (null == addRowList.get(addRow)) {
                                        rowModel.setSheetRowData(row.getSheetRowData());
                                    }
                                    Map<Column, Cell> addColumnMap = rowColMap.get(rowModel) != null ? rowColMap.get(rowModel) : new HashMap<>();
                                    //添加数据
                                    Column column = columns.size() > j ? columns.get(j) : new Column(new ArrayList<>());
                                    Map<Column, Cell> columnMap = new HashMap<>();
                                    columnMap.put(column, cell);
                                    addRowList.put(addRow, rowModel);
                                    addColumnMap.putAll(columnMap);
                                    rowColMap.put(rowModel, addColumnMap);
                                }
                            }
                        }
                    }
                }
            }
        }
        List<Column> delColumns = new ArrayList<>();
        //删除没有用的格子
        for (Row row : delColList.keySet()) {
            Map<Column, Cell> columnMap = rowColCellMap.get(row);
            if (columnMap == null) {
                continue;
            }
            Map<Column, Cell> delColumnMap = delColList.get(row) != null ? delColList.get(row) : new HashMap<>();
            for (Column column : delColumnMap.keySet()) {
                columnMap.remove(column);
            }
            //特殊处理列

            //删除列的数据
            List<Column> deleteColumnList = columns.stream().filter(e -> new ArrayList<>(delColumnMap.keySet()).contains(e)).collect(Collectors.toList());
            for (Column column : deleteColumnList) {
                Cell delCell = delColumnMap.get(column);
                List<Cell> cells = column.getCells();
                cells.remove(delCell);
                if (!isCol
                        && cellName.get(delCell.getName()) != null
                        && delCell.getType().equals("text")) {
                    delColumns.add(column);
                }
                if (cells.isEmpty()) {
                    columns.remove(column);
                }
            }

            //删除行的数据


            if (columnMap.isEmpty()) {
                rows.remove(row);
            } else {
                List<Cell> cells = new ArrayList<>(columnMap.values());
                boolean b = cells.stream().allMatch(cell ->
                        cell.getType() == null || !cell.getType().equals("text") ||
                                StringUtil.isEmpty(ObjectUtil
                                        .defaultIfNull(cell.getData(), "")
                                        .toString()));
                if (b) {
                    rows.remove(row);
                }
            }
        }

        if (CollectionUtil.isNotEmpty(delColumns)) {
            for (Column delColumn : delColumns) {
                List<Cell> cells = delColumn.getCells();
                boolean b = cells.stream().allMatch(cell ->
                        (cell.getType() == null || cell.getType().equals("text")) &&
                                StringUtil.isEmpty(ObjectUtil
                                        .defaultIfNull(cell.getData(), "")
                                        .toString()));
                if (b) {
                    columns.remove(delColumn);
                }
            }
        }


        //新增行数据
        List<Integer> columList = new ArrayList<>(addColumnList.keySet()).stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
        int addColumnCount = columList.isEmpty() ? 0 : Collections.max(columList);
        for (int i = beforeColList.size(); i < addColumnCount; i++) {
            columns.add(new Column(new ArrayList<>()));
        }
        for (Integer col : columList) {
            Column addColumn = addColumnList.get(col);
            if (addColumn == null) {
                continue;
            }
            while ((col - beforeColList.size()) != 0) {
                beforeColList.add(beforeColList.size(), new Column(new ArrayList<>()));
            }
            beforeColList.add(col, addColumn);
        }

        //新增列数据
        List<Integer> rowList = new ArrayList<>(addRowList.keySet()).stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
        int addRowCount = rowList.isEmpty() ? 0 : Collections.max(rowList);
        for (int i = beforeRowList.size(); i < addRowCount; i++) {
            rows.add(new Row(new ArrayList<>()));
        }
        for (Integer row : rowList) {
            Row addRow = addRowList.get(row);
            if (addRow == null) {
                continue;
            }
            while ((row - beforeRowList.size()) != 0) {
                beforeRowList.add(beforeRowList.size(), new Row(new ArrayList<>()));
            }
            beforeRowList.add(row, addRow);
        }
        beforeRowList.addAll(afterRowList);
        beforeColList.addAll(afterColList);
        report.setRows(beforeRowList);
        report.setColumns(beforeColList);

        //行列对应的数据
        for (Row row : rowColMap.keySet()) {
            Map<Column, Cell> cellMap = rowColMap.get(row) != null ? rowColMap.get(row) : new HashMap<>();
            Map<Column, Cell> columnMap = rowColCellMap.get(row) != null ? rowColCellMap.get(row) : new HashMap<>();
            columnMap.putAll(cellMap);
            rowColCellMap.put(row, columnMap);
        }
    }


    private void span(int total, int num, int fenceNum, Map<Integer, Integer> addMap, Map<Integer, Integer> spanMap, int span) {
        addMap.put(0, 0);
        int firstData = fenceNum - num;
        spanMap.put(0, Collections.min(ImmutableList.of(span, firstData)));
        if (total > 1) {
            if (firstData <= total) {
                int dataSize = total - firstData;
                int size = (dataSize / fenceNum) + (dataSize % fenceNum == 0 ? 0 : 1);
                for (int j = 0; j < size; j++) {
                    int index = firstData + (j * fenceNum);
                    addMap.put(index, index);
                    if (j == size - 1) {
                        spanMap.put(index, total - index);
                    } else {
                        spanMap.put(index, fenceNum);
                    }
                }
            }
        }
    }

    /**
     * 分栏处理
     *
     * @param report
     * @param columnList
     */
    private void buildFenceReport(Report report, ColumnList columnList) {
        Map<String, List<Cell>> cellsMap = report.getCellsMap();
        Map<Row, Map<Column, Cell>> rowColCellMap = report.getRowColCellMap();
        List<Column> columns = report.getColumns();
        List<Row> rows = report.getRows();
        String copyCol = columnList.getCopyCol();
        String copyRow = columnList.getCopyRow();
        Integer rowCount = columnList.getRowCount();
        Integer maxCol = columnList.getMaxCol();
        Integer colCount = columnList.getColCount();
        Integer maxRow = columnList.getMaxRow();
        List<Integer> collect = getCopyCollect(columnList.getColumnStyle().equals("col") ? copyCol : copyRow);
        //分栏数据的处理
        String columnData = columnList.getColumnData();
        CellRangeAddress cellAddresses = null;
        try {
            cellAddresses = CellRangeAddress.valueOf(columnData);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }

        //不参加分栏的数据(不包括复制数据)
        List<String> strings = new ArrayList<>();
        for (String string : cellsMap.keySet()) {
            CellRangeAddress cellRangeAddress = CellRangeAddress.valueOf(string);
            if ((cellRangeAddress.getFirstRow() < cellAddresses.getFirstRow()
                    || cellRangeAddress.getFirstRow() > cellAddresses.getLastRow()
                    || cellRangeAddress.getFirstColumn() < cellAddresses.getFirstColumn()
                    || cellRangeAddress.getFirstColumn() > cellAddresses.getLastColumn())
                    && ((columnList.getColumnStyle().equals("col") && !collect.contains(cellRangeAddress.getFirstRow()))
                    || (columnList.getColumnStyle().equals("row") && !collect.contains(cellRangeAddress.getFirstColumn())))) {
                strings.add(string);
            }
        }
        //行分栏，分成n栏
        if (BeanUtil.isNotEmpty(columnList) && columnList.isColumnState() && columnList.getColumnStyle().equals("col")) {
            //未分栏数据集合
            Map<Row, Map<Column, Cell>> rowMapHashMap = new HashMap<>();
            //未分栏行集合
            List<Row> rowNoNeed = new ArrayList<>(rows.size());
            rowNoNeed.addAll(Collections.nCopies(rows.size(), null));
            for (Row row : rowColCellMap.keySet()) {

                Map<Column, Cell> columnCellMap = rowColCellMap.get(row);
                Row addRow = new Row(new ArrayList<>());
                int i = rows.indexOf(row);
                HashMap<Column, Cell> columnCellHashMap = new HashMap<>();
                Iterator<Column> iterator = columnCellMap.keySet().iterator();
                while (iterator.hasNext()) {
                    Column column = iterator.next();
                    Cell cell = columnCellMap.get(column);
                    //如果存在不分栏的数据
                    if (strings.contains(cell.getName())) {
                        columnCellHashMap.put(column, cell);
                        if (collect.contains(i)) {
                            continue;
                        }
                        iterator.remove();
                    }


                }
                if (!columnCellHashMap.isEmpty()) {
                    addRow.setSheetRowData(row.getSheetRowData());
                    rowNoNeed.set(i, addRow);
                    rowMapHashMap.put(addRow, columnCellHashMap);
                }

            }
            //复制行
            List<Row> copyRowList = collect.stream().map(item -> {
                for (Row row : rows) {
                    int i = parseRowNum(row);
                    if (i == item) {
                        return row;
                    }
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
            //删除数据
            for (Row row : rowColCellMap.keySet()) {
                if (copyRowList.contains(row)) {
                    continue;
                }
                Map<Column, Cell> columnCellMap = rowColCellMap.get(row);
                Iterator<Column> iterator = columnCellMap.keySet().iterator();
                while (iterator.hasNext()) {
                    Column column = iterator.next();
                    Cell cell = columnCellMap.get(column);
                    if (strings.contains(cell.getName())) {
                        iterator.remove();
                    }
                }
            }

            int firstRow = cellAddresses.getFirstRow();
            int lastRow = cellAddresses.getLastRow();
            int dataSize = (int) rows.stream().filter(item -> {
                int rowNum = parseRowNum(item);
                return rowNum >= firstRow && rowNum <= lastRow;
            }).count();
            //分栏数目
            int fenceNum = 0;
            //分栏长度
            int fenceDataSize = 0;
            if (columnList.getColumnType().equals("2")) {
                if (rowCount == 1) {
                    return;
                }
                fenceDataSize = dataSize % rowCount == 0 ? dataSize / rowCount : dataSize / rowCount + 1;
                fenceNum = rowCount;
            } else {
                if (maxCol < 1) {
                    return;
                }
                fenceDataSize = maxCol;
                fenceNum = dataSize % maxCol == 0 ? dataSize / maxCol : dataSize / maxCol + 1;
            }
            rowChoice(report, columnList, columns, fenceNum
                    , rows, copyRowList, rowColCellMap
                    , firstRow, lastRow, rowMapHashMap, rowNoNeed, fenceDataSize);
        } else if (BeanUtil.isNotEmpty(columnList) && columnList.isColumnState() && columnList.getColumnStyle().equals("row")) {

            //处理复制列
            List<Column> copyColumnList = collect.stream().map(item -> {
                for (Column column : columns) {
                    int i = column.getColumnKey();
                    if (i - 1 == item) {
                        return column;
                    }
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());


            int firstColumn = cellAddresses.getFirstColumn();
            int lastColumn = cellAddresses.getLastColumn();
            int dataSize = (int) columns.stream().filter(item -> {
                int columnKey = item.getColumnKey();
                return columnKey - 1 >= firstColumn && columnKey - 1 <= lastColumn;
            }).count();
            //分栏数目
            int fenceNum = 0;
            //分栏长度
            int fenceDataSize = 0;
            if (columnList.getColumnType().equals("2")) {
                if (colCount == 1) {
                    return;
                }
                fenceDataSize = dataSize % colCount == 0 ? dataSize / colCount : dataSize / colCount + 1;
                fenceNum = colCount;
            } else {
                if (maxRow < 1) {
                    return;
                }
                fenceDataSize = maxRow;
                fenceNum = dataSize % maxRow == 0 ? dataSize / maxRow : dataSize / maxRow + 1;
            }

            //未分栏数据集合
            Map<Row, Map<Column, Cell>> rowMapHashMap = new HashMap<>();
            //未分栏行集合
            List<Row> rowNoNeed = new ArrayList<>(rows.size());
            List<Column> newColumnList = new ArrayList<>();
            rowNoNeed.addAll(Collections.nCopies(rows.size(), null));
            for (Row row : rowColCellMap.keySet()) {

                Map<Column, Cell> columnCellMap = rowColCellMap.get(row);
                Row addRow = new Row(new ArrayList<>());
                int i = rows.indexOf(row);
                HashMap<Column, Cell> columnCellHashMap = new HashMap<>();
                Iterator<Column> iterator = columnCellMap.keySet().iterator();
                while (iterator.hasNext()) {
                    Column column = iterator.next();
                    Cell cell = columnCellMap.get(column);
                    int index = columns.indexOf(column);
                    //如果存在不分栏的数据
                    if (strings.contains(cell.getName())) {
                        if (collect.contains(index)) {
                            continue;
                        }
                        int tempColumnNumber = column.getTempColumnNumber();
                        int columnKey = column.getColumnKey();
                        int finalFenceDataSize = fenceDataSize;
                        List<Column> collected = newColumnList.stream()
                                .filter(t -> t.getTempColumnNumber()
                                        == tempColumnNumber + copyColumnList.size() + finalFenceDataSize
                                        && t.getColumnKey() == columnKey)
                                .collect(Collectors.toList());
                        if (collected.isEmpty()) {
                            Column newColumn = new Column(new ArrayList<>());
                            newColumn.setTempColumnNumber(tempColumnNumber + copyColumnList.size() + fenceDataSize);
                            newColumn.setColumnKey(columnKey);
                            newColumnList.add(newColumn);
                            columnCellHashMap.put(newColumn, cell);
                        } else {
                            columnCellHashMap.put(collected.get(0), cell);
                        }


                        iterator.remove();
                    }


                }
                if (!columnCellHashMap.isEmpty()) {
                    addRow.setSheetRowData(row.getSheetRowData());
                    rowNoNeed.set(i, addRow);
                    rowMapHashMap.put(addRow, columnCellHashMap);
                }

            }
            //删掉数据
            for (Row row : rowColCellMap.keySet()) {
                Map<Column, Cell> columnCellMap = rowColCellMap.get(row);
                Iterator<Column> iterator = columnCellMap.keySet().iterator();
                while (iterator.hasNext()) {
                    Column column = iterator.next();
                    Cell cell = columnCellMap.get(column);
                    if (strings.contains(cell.getName()) && !copyColumnList.contains(column)) {
                        iterator.remove();
                    }
                }
            }

            colChoice(report, columnList, rows, columns, firstColumn
                    , lastColumn, fenceDataSize, rowColCellMap, copyColumnList, rowMapHashMap, rowNoNeed, fenceNum, newColumnList);
        }


    }

    private static void colChoice(Report report, ColumnList columnList, List<Row> rows
            , List<Column> columns, int firstColumn, int lastColumn
            , int fenceDataSize, Map<Row, Map<Column, Cell>> rowColCellMap, List<Column> copyColumnList
            , Map<Row, Map<Column, Cell>> rowMapHashMap, List<Row> rowNoNeed, int fenceNum, List<Column> newColumnList) {
        //行数
        int rowSize = rows.size();
        //造行
        List<Row> rowArrayList = new ArrayList<>();
        for (int i = 0; i < (fenceNum - 1); i++) {
            for (int j = 0; j < rowSize; j++) {
                Row row = new Row(new ArrayList<>());
                row.setTempRowNumber(rowSize * (i + 1) + j);
                row.setSheetRowData(rows.get(j).getSheetRowData());
                rowArrayList.add(row);
            }
        }
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            row.setTempRowNumber(i);
        }
        rows.addAll(rowArrayList);
        List<Column> columnArrayList = new ArrayList<>();
        //获取非复制列的数据
        for (Column column : columns) {
            int columnKey = column.getColumnKey();
            if (columnKey - 1 >= firstColumn && columnKey - 1 <= lastColumn) {
                columnArrayList.add(column);
            }
        }

        List<List<Column>> partition = ListUtil.partition(columnArrayList, fenceDataSize);
        List<Column> headColumnList = partition.get(0);
        //处理数据
        for (int i = 1; i < partition.size(); i++) {
            List<Column> dataList = partition.get(i);
            Set<Column> dataSet = new HashSet<>(dataList);
            for (int j = 0; j < rowSize; j++) {
                Row row = rows.get(j);
                Map<Column, Cell> columnCellMap = rowColCellMap.get(row);
                Map<Column, Cell> newColumnCellMap = Optional.ofNullable(rowColCellMap.get(row))
                        .map(map -> map.entrySet().stream()
                                .filter(entry -> dataSet.contains(entry.getKey()))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                        )
                        .orElseGet(HashMap::new);
                for (Column column1 : columnCellMap.keySet()) {
                    if (copyColumnList.contains(column1)) {
                        newColumnCellMap.put(column1, columnCellMap.get(column1));
                    }
                }
                Map<Column, Cell> resultColumnCellMap = new HashMap<>();
                for (Column column : newColumnCellMap.keySet()) {
                    if (copyColumnList.contains(column)) {
                        resultColumnCellMap.put(column, newColumnCellMap.get(column));
                        continue;
                    }
                    int index = dataList.indexOf(column);
                    resultColumnCellMap.put(headColumnList.get(index), newColumnCellMap.get(column));
                }
                int finalI = i;

                List<Row> rowList = rows
                        .stream()
                        .filter(item -> item.getTempRowNumber() == row.getTempRowNumber() + rowSize * finalI)
                        .collect(Collectors.toList());
                rowColCellMap.put(rowList.get(0), resultColumnCellMap);
            }
        }

        //将复制行和复制数据组合
        List<Column> beforeFirst = new ArrayList<>();
        List<Column> afterLast = new ArrayList<>();
        for (Column column : copyColumnList) {
            int columnKey = column.getColumnKey() - 1;
            if (columnKey < firstColumn) beforeFirst.add(column);
            else if (columnKey > lastColumn) afterLast.add(column);
        }
        List<Column> resultColumn = new ArrayList<>(beforeFirst);

        int addedCount = 0;
        for (Column column : columns) {
            int columnKey = column.getColumnKey() - 1;
            if (columnKey >= firstColumn && columnKey <= lastColumn) {
                resultColumn.add(column);
                if (addedCount == 0) {
                    columnList.setColMinNum(resultColumn.indexOf(column));
                }
                if (++addedCount == fenceDataSize) {
                    columnList.setColMaxNum(resultColumn.indexOf(column));
                    break;
                }
            }
        }

        resultColumn.addAll(afterLast);
        Set<Row> rowSet = rowColCellMap.keySet();
        for (Row row : rowSet) {
            Map<Column, Cell> columnCellMap = rowColCellMap.get(row);
            columnCellMap.keySet().removeIf(column -> !resultColumn.contains(column));
        }
        ColFillBlank(columnList, rowColCellMap, resultColumn);

        List<Column> allColumn = newColumnList.stream()
                .sorted(Comparator.comparing(Column::getColumnKey)
                        .thenComparing(Column::getTempColumnNumber))
                .collect(Collectors.toList());

        List<Column> collected = allColumn.stream()
                .filter(item -> !resultColumn.contains(item))
                .collect(Collectors.toList());
        resultColumn.addAll(collected);

        rowColCellMap.putAll(rowMapHashMap);
        rows.addAll(rowNoNeed);
        report.setRows(rows.stream().filter(Objects::nonNull).collect(Collectors.toList()));
        report.setColumns(resultColumn);
        report.setRowColCellMap(rowColCellMap);
    }

    private void rowChoice(Report report, ColumnList columnList, List<Column> columns
            , int fenceNum, List<Row> rows, List<Row> copyRowList, Map<Row
                    , Map<Column, Cell>> rowColCellMap, int firstRow, int lastRow
            , Map<Row, Map<Column, Cell>> rowMapHashMap
            , List<Row> rowNoNeed, int fenceDataSize) {
        //分多少栏
        int columnSize = columns.size();
        List<Column> columnArrayList = new ArrayList<>();
        //造列
        for (int i = 0; i < fenceNum - 1; i++) {
            for (int j = 0; j < columnSize; j++) {
                Column column = new Column(new ArrayList<>());
                column.setTempColumnNumber(columns.size() * (i + 1) + j);
                columnArrayList.add(column);
            }
        }
        //给列添加标识
        for (int i = 0; i < columns.size(); i++) {
            Column column = columns.get(i);
            column.setTempColumnNumber(i);
        }
        columns.addAll(columnArrayList);
        //排除复制行
        List<Row> rowArrayList = new ArrayList<>();
        for (Row row : rows) {
            int i = parseRowNum(row);
            if (i >= firstRow && i <= lastRow) {
                rowArrayList.add(row);
            }
        }

        List<List<Row>> partition = ListUtil.partition(rowArrayList, fenceDataSize);
        List<Row> rowList = partition.get(0);

        for (Row row : copyRowList) {
            if (rowColCellMap.containsKey(row)) {
                Map<Column, Cell> columnCellMap = rowColCellMap.get(row);
                Set<Column> columnSet = columnCellMap.keySet();
                Map<Column, Cell> newColumnCellMap = new HashMap<>();
                for (int i = 0; i < fenceNum - 1; i++) {
                    int num;

                    for (Column column : columnSet) {
                        int finalI = i + 1;
                        num = columns.indexOf(column);
                        int finalNum = num;
                        List<Column> collected = columns.stream().filter(item -> item.getTempColumnNumber() == (columnSize * finalI + finalNum))
                                .collect(Collectors.toList());
                        newColumnCellMap.put(collected.get(0), columnCellMap.get(columns.get(num)));
                    }

                }
                columnCellMap.putAll(newColumnCellMap);
            }
        }

        //处理数据
        for (int i = 1; i < partition.size(); i++) {
            List<Row> list = partition.get(i);

            for (int j = 0; j < list.size(); j++) {
                Row row1 = rowList.get(j);
                Map<Column, Cell> cellMap = rowColCellMap.get(list.get(j));
                for (Column column : cellMap.keySet()) {
                    Map<Column, Cell> columnCellMap = rowColCellMap.get(row1);
                    int finalI = i;

                    List<Column> collected = columns.stream().filter(item -> item.getTempColumnNumber() == (columnSize * finalI + column.getTempColumnNumber()))
                            .collect(Collectors.toList());

                    columnCellMap.put(collected.get(0), cellMap.get(column));
                }


            }
        }
        //将复制行和复制数据组合
        List<Row> beforeFirst = new ArrayList<>();
        List<Row> afterLast = new ArrayList<>();
        for (Row row : copyRowList) {
            int rowNum = parseRowNum(row);
            if (rowNum < firstRow) beforeFirst.add(row);
            else if (rowNum > lastRow) afterLast.add(row);
        }
        List<Row> resultRow = new ArrayList<>(beforeFirst);

        int addedCount = 0;
        for (Row row : rows) {

            int rowNum = parseRowNum(row);
            if (rowNum >= firstRow && rowNum <= lastRow) {
                resultRow.add(row);
                if (addedCount == 0) {
                    columnList.setRowMinNum(resultRow.indexOf(row));
                }
                if (++addedCount == fenceDataSize) {
                    columnList.setRowMaxNum(resultRow.indexOf(row));
                    break;
                }
            }
        }

        resultRow.addAll(afterLast);
        rowColCellMap.keySet().removeIf(row -> !resultRow.contains(row));
        rowColCellMap.putAll(rowMapHashMap);
        resultRow.addAll(rowNoNeed.stream()
                .filter(item -> !copyRowList.contains(item))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        report.setRows(resultRow);
        report.setColumns(columns);
        report.setRowColCellMap(rowColCellMap);
    }

    private static void ColFillBlank(ColumnList columnList, Map<Row, Map<Column, Cell>> rowColCellMap, List<Column> resultColumn) {
        //补充空白格
        if (columnList.isFillEmptyRows()) {
            for (Row row : rowColCellMap.keySet()) {
                Map<Column, Cell> columnCellMap = rowColCellMap.get(row);
                Set<Column> columnSet = columnCellMap.keySet();
                List<Column> collected = resultColumn.stream()
                        .filter(column -> !columnSet.contains(column))
                        .collect(Collectors.toList());
                for (Column column : collected) {
                    columnCellMap.put(column, new Cell());
                }
            }
        }
    }

    @NotNull
    private static List<Integer> getCopyCollect(String copyCol) {
        if (StringUtil.isEmpty(copyCol)) {
            return Collections.emptyList();
        }
        String[] split = copyCol.split(",");
        List<String> list = new ArrayList<>(split.length);
        for (String string : split) {
            if (string.contains("-")) {
                String[] strings = string.split("-");
                list.addAll(Arrays.asList(strings));
            }
            list.add(string);
        }
        List<Integer> collect = new ArrayList<>();
        try {
            collect = list.stream()
                    .map(Integer::valueOf)
                    .map(item -> {

                        if (item >= 1) {
                            return item - 1;
                        }
                        return null;
                    })
                    .filter(Objects::nonNull).sorted().collect(Collectors.toList());

        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }
        //复制列的列数并排序
        return collect;
    }

    private int parseRowNum(Row row) {
        return Integer.parseInt(row.getRowKey().replace('r', ' ').trim()) - 1;
    }

    private void formulaList(Cell formulaCell, Formula formula, Report report) {
        String function = formula.getFormula();
        int endRow = formula.getRowNumber();
        int endCol = formula.getColNumber();
        Map<String, List<String>> functionName = new LinkedHashMap<>();
        Map<String, Map<Integer, Map<Integer, String>>> cellNameMap = formula.getCellName();
        for (String name : formula.getCellNameList()) {
            Map<Integer, Map<Integer, String>> cellName = cellNameMap.get(name) != null ? cellNameMap.get(name) : new HashMap<>();
            //函数只支持左上角和右上角
            List<Integer> rowList = new ArrayList<>(cellName.keySet()).stream().filter(e -> endRow >= e).collect(Collectors.toList());
            List<String> leftTopList = new ArrayList<>();
            List<String> rightTopList = new ArrayList<>();
            List<Integer> delRow = new ArrayList<>();
            for (Integer row : rowList) {
                Map<Integer, String> colMap = cellName.get(row) != null ? cellName.get(row) : new HashMap<>();
                List<Integer> leftTopCol = new ArrayList<>();
                List<Integer> rightTopCol = new ArrayList<>();
                for (Integer col : colMap.keySet()) {
                    String functionCellName = colMap.get(col);
                    if (StringUtil.isEmpty(functionCellName)) {
                        continue;
                    }
                    if (endCol >= col) {
                        leftTopList.add(functionCellName);
                        leftTopCol.add(col);
                    } else {
                        rightTopList.add(functionCellName);
                        rightTopCol.add(col);
                    }
                }
                if (!leftTopCol.isEmpty()) {
                    leftTopCol.forEach(colMap.keySet()::remove);
                } else {
                    rightTopCol.forEach(colMap.keySet()::remove);
                }
                cellName.put(row, colMap);
                if (colMap.isEmpty()) {
                    delRow.add(row);
                }
            }
            delRow.forEach(cellName.keySet()::remove);
            if (!leftTopList.isEmpty() || !rightTopList.isEmpty()) {
                functionName.put(name, !leftTopList.isEmpty() ? leftTopList : rightTopList);
            }
        }
        if (!functionName.isEmpty()) {
            for (String name : functionName.keySet()) {
                List<String> functionNameList = functionName.get(name);
                if (functionNameList.size() > 255) {
                    function = function.replaceAll(name, functionNameList.get(0) + ":" + functionNameList.get(functionNameList.size() - 1));
                } else {
                    function = function.replaceAll(name, String.join(",", functionNameList));
                }
            }
            formulaCell.getCellData().setF(function);
        }
    }

    private void rebuild(ReportDefinition report) {
        List<CellDefinition> cells = report.getCells();
        Map<String, CellDefinition> cellsMap = new HashMap<>();
        Map<String, CellDefinition> cellsRowColMap = new HashMap<>();
        for (CellDefinition cell : cells) {
            cellsMap.put(cell.getName(), cell);
            int rowNum = cell.getRowNumber();
            int colNum = cell.getColumnNumber();
            int rowSpan = cell.getRowSpan() > 0 ? cell.getRowSpan() : 1;
            int colSpan = cell.getColSpan() > 0 ? cell.getColSpan() : 1;
            int rowEnd = rowNum + rowSpan;
            int colEnd = colNum + colSpan;
            for (int i = rowNum; i < rowEnd; i++) {
                cellsRowColMap.put(i + "," + colNum, cell);
            }
            for (int i = colNum; i < colEnd; i++) {
                cellsRowColMap.put(rowNum + "," + i, cell);
            }
        }
        for (CellDefinition cell : cells) {
            int rowNumber = cell.getRowNumber();
            int colNumber = cell.getColumnNumber();
            Value value = cell.getValue();
            boolean isData = value instanceof DatasetValue;

            String leftType = cell.getLeftType();
            String leftParentCellName = cell.getLeftParentCellName();
            CellDefinition leftTargetCell = cellsMap.get(leftParentCellName);
            if (leftTargetCell == null) {
                if (colNumber > 1) {
                    leftTargetCell = cellsRowColMap.get(rowNumber + "," + (colNumber - 1));
                    boolean leftData = false;
                    if (isData) {
                        for (int i = colNumber; i > 0; i--) {
                            CellDefinition definition = cellsRowColMap.get(rowNumber + "," + (i - 1));
                            if (definition != null) {
                                Value definitionValue = definition.getValue();
                                if (definitionValue instanceof DatasetValue) {
                                    leftTargetCell = definition;
                                    leftData = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (!leftData && Objects.equals(UniverDataEnum.cellRoot.getName(), leftParentCellName)) {
                        leftTargetCell = null;
                    }
                }
            }
            cell.setLeftParentCell(leftTargetCell);

            String topType = cell.getTopType();
            String topParentCellName = cell.getTopParentCellName();
            CellDefinition topTargetCell = cellsMap.get(topParentCellName);
            if (topTargetCell == null) {
                if (rowNumber > 1) {
                    topTargetCell = cellsRowColMap.get((rowNumber - 1) + "," + colNumber);
                    boolean topData = false;
                    if (isData) {
                        for (int i = rowNumber; i > 0; i--) {
                            CellDefinition definition = cellsRowColMap.get((i - 1) + "," + colNumber);
                            if (definition != null) {
                                Value definitionValue = definition.getValue();
                                if (definitionValue instanceof DatasetValue) {
                                    topTargetCell = definition;
                                    topData = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (!topData && Objects.equals(UniverDataEnum.cellRoot.getName(), topParentCellName)) {
                        topTargetCell = null;
                    }
                }
            }
            cell.setTopParentCell(topTargetCell);

            if (isData) {
                DatasetValue datasetValue = (DatasetValue) value;
                AggregateType aggregate = datasetValue.getAggregate();
                List<AggregateType> aggregateType = ImmutableList.of(AggregateType.select, AggregateType.group);
                if (Objects.equals(topType, UniverDataEnum.cellNone.getName()) && Objects.equals(leftType, UniverDataEnum.cellNone.getName())) {
                    if (!aggregateType.contains(aggregate)) {
                        cell.setTopParentCell(null);
                        cell.setLeftParentCell(null);
                    }
                }
            }
        }
    }

    private Map<String, Integer> fillEmptyRows(Map<Integer, Map<Integer, Integer>> fillRowMap) {
        Map<String, Integer> fillRow = new HashMap<>();
        for (Integer row : fillRowMap.keySet()) {
            Map<Integer, Integer> columnMap = fillRowMap.get(row);
            int max = 0;
            String maxName = "";
            for (Integer column : columnMap.keySet()) {
                int rowNum = columnMap.get(column);
                if (rowNum >= max) {
                    CellReference reference = new CellReference(row, column);
                    maxName = reference.formatAsString();
                    max = rowNum;
                }
            }
            fillRow.put(maxName, max);
        }
        return fillRow;
    }

    //赋值左、上父格
    private void rebuildReportDefinition(ReportDefinition reportDefinition) {
        List<CellDefinition> cells = reportDefinition.getCells();
        for (CellDefinition cell : cells) {
            List<String> rowName = new ArrayList<>();
            addRowChildCell(cell, cell, rowName);
            Set<String> row = new HashSet<>(rowName);
            List<String> columnName = new ArrayList<>();
            addColumnChildCell(cell, cell, columnName);
            Set<String> column = new HashSet<>(columnName);
            if (row.size() < rowName.size() || column.size() < columnName.size()) {
                throw new DataException(MsgCode.FA106.get());
            }
        }
        for (CellDefinition cell : cells) {
            Expand expand = cell.getExpand();
            switch (expand) {
                case Down:
                    DownCellbuilder downCellbuilder = new DownCellbuilder();
                    downCellbuilder.buildParentCell(cell, cells);
                    break;
                case Right:
                    RightCellbuilder rightCellbuilder = new RightCellbuilder();
                    rightCellbuilder.buildParentCell(cell, cells);
                    break;
            }
        }
    }

    //添加上父格
    private static void addRowChildCell(CellDefinition cell, CellDefinition childCell, List<String> rowName) {
        CellDefinition leftCell = cell.getLeftParentCell();
        if (leftCell == null) {
            return;
        }
        if (rowName.contains(leftCell.getName())) {
            rowName.add(leftCell.getName());
            return;
        }
        rowName.add(leftCell.getName());
        List<CellDefinition> childrenCells = leftCell.getRowChildrenCells();
        childrenCells.add(childCell);
        addRowChildCell(leftCell, childCell, rowName);
    }

    //添加左父格
    private static void addColumnChildCell(CellDefinition cell, CellDefinition childCell, List<String> columnName) {
        CellDefinition topCell = cell.getTopParentCell();
        if (topCell == null) {
            return;
        }
        if (columnName.contains(topCell.getName())) {
            columnName.add(topCell.getName());
            return;
        }
        columnName.add(topCell.getName());
        List<CellDefinition> childrenCells = topCell.getColumnChildrenCells();
        childrenCells.add(childCell);
        addColumnChildCell(topCell, childCell, columnName);
    }

    //解析函数，获取所在的格子
    private static void formulaName(String formulaText, List<CellRangeAddress> list) {
        List<String> splitList = ImmutableList.of(",", "=", ">", "<", "\\*", "\\+", "-", "/", "\\(", "\\)", "<>");
        formulaName(formulaText, splitList, 0, list);
    }

    //函数，获取所在的格子
    private static void formulaName(String formulaText, List<String> splitList, int index, List<CellRangeAddress> list) {
        if (index > splitList.size() - 1) {
            return;
        }
        String formula = splitList.get(index);
        String[] strings = formulaText.split(formula);
        for (String cell : strings) {
            if (StringUtil.isEmpty(cell)) {
                continue;
            }
            try {
                CellRangeAddress cellRange = CellRangeAddress.valueOf(cell);
                if (!list.contains(cellRange)) {
                    list.add(cellRange);
                }
            } catch (Exception ignored) {
            }
            formulaName(cell, splitList, index + 1, list);
        }
    }

    //函数区间的格子
    private static List<CellReference> getCellsRange(int startRow, int endRow, int startCol, int endCol) {
        int rangeStartRow = startRow;
        int rangeStartCol = startCol;
        int rangeEndRow = endRow;
        int rangeEndCol = endCol;
        if (startRow > endRow || startCol > endCol) {
            rangeStartRow = endRow;
            rangeStartCol = endCol;
            rangeEndRow = startRow;
            rangeEndCol = startCol;
        }
        List<CellReference> referenceList = new ArrayList<>();
        if (rangeStartRow >= 0) {
            for (int rowNum = rangeStartRow; rowNum <= rangeEndRow; rowNum++) {
                for (int colNum = rangeStartCol; colNum <= rangeEndCol; colNum++) {
                    CellReference reference = new CellReference(rowNum, colNum);
                    referenceList.add(reference);
                }
            }
        }
        return referenceList;
    }

    private static void resource(String sheet, Map<String, Map<Integer, Map<Integer, String>>> nameMap, List<UniverResource> resources) {
        //条件
        format(sheet, nameMap, resources);
        //数据验证
        dataValidation(sheet, nameMap, resources);
        //数据验证
        filter(sheet, nameMap, resources);
    }

    //调整数据格式、数据管理的所在格子
    private static List<UniverSheetRange> resourceRange(Map<String, Map<Integer, Map<Integer, String>>> nameMap, List<UniverSheetRange> sheetRanges) {
        List<UniverSheetRange> rangeList = new ArrayList<>();
        for (UniverSheetRange sheetRange : sheetRanges) {
            int startRow = sheetRange.getStartRow();
            int endRow = sheetRange.getEndRow();
            int startCol = sheetRange.getStartColumn();
            int endCol = sheetRange.getEndColumn();
            List<CellReference> cellReferenceList = getCellsRange(startRow, endRow, startCol, endCol);
            for (CellReference cellReference : cellReferenceList) {
                String referenceName = cellReference.formatAsString();
                List<String> nameList = new ArrayList<>();
                Map<Integer, Map<Integer, String>> cellMap = nameMap.get(referenceName) != null ? nameMap.get(referenceName) : new HashMap<>();
                for (Integer row : cellMap.keySet()) {
                    Map<Integer, String> colMap = cellMap.get(row) != null ? cellMap.get(row) : new HashMap<>();
                    for (Integer col : colMap.keySet()) {
                        String name = colMap.get(col);
                        if (StringUtil.isEmpty(name)) {
                            continue;
                        }
                        if (!nameList.contains(name)) {
                            UniverSheetRange range = JsonUtil.getJsonToBean(sheetRange, UniverSheetRange.class);
                            range.setStartRow(row);
                            range.setStartColumn(col);
                            range.setEndRow(range.getStartRow());
                            range.setEndColumn(range.getStartColumn());
                            rangeList.add(range);
                            nameList.add(name);
                        }
                    }
                }
            }
        }
        return rangeList.isEmpty() ? sheetRanges : rangeList;
    }

    //处理悬浮图表
    private static Map<String, List<UniverDataConfig>> floatEchart(String unitId, List<UniverResource> resources, Map<String, UniverDataConfig> floatEchart) {
        UniverResource drawing = resources.stream().filter(t -> ResourceEnum.SHEET_DRAWING_PLUGIN.name().equals(t.getName())).findFirst().orElse(null);
        Map<String, UniverResourceData> drawingMap = new HashMap<>();
        Map<String, List<UniverDataConfig>> echartMap = new HashMap<>();
        if (drawing != null && ObjectUtil.isNotEmpty(drawing.getData())) {
            Map<String, Object> data = JsonUtil.stringToMap(drawing.getData());
            data.forEach((key, value) -> {
                drawingMap.put(key, JsonUtil.getJsonToBean(value, UniverResourceData.class));
            });
            for (String sheet : drawingMap.keySet()) {
                UniverResourceData resourceData = drawingMap.get(sheet);
                List<String> orderList = resourceData.getOrder();
                Map<String, UniverDrawing> dataMap = resourceData.getData();
                List<UniverDataConfig> echartList = new ArrayList<>();
                for (String order : orderList) {
                    UniverDrawing univerDrawing = dataMap.get(order);
                    if (univerDrawing == null) {
                        continue;
                    }
                    univerDrawing.setUnitId(unitId);
                    UniverDataConfig univerDataConfig = floatEchart.get(order);
                    if (univerDataConfig != null) {
                        UniverDataConfig option = univerDataConfig.getOption() != null ? univerDataConfig.getOption() : new UniverDataConfig();
                        univerDataConfig.setDrawingId(univerDrawing.getDrawingId());
                        univerDataConfig.setUnitId(univerDrawing.getUnitId());
                        univerDataConfig.setSubUnitId(univerDrawing.getSubUnitId());
                        univerDataConfig.setSummaryType(option.getSummaryType());
                        univerDataConfig.setClassifyNameField(option.getClassifyNameField());
                        univerDataConfig.setSeriesNameField(option.getSeriesNameField());
                        univerDataConfig.setSeriesDataField(option.getSeriesDataField());
                        univerDataConfig.setMaxField(option.getMaxField());
                        echartList.add(univerDataConfig);
                    }
                }
                echartMap.put(sheet, echartList);
            }
//            drawing.setData(JSON.toJSONString(JSON.toJSON(drawingMap)));
        }
        return echartMap;
    }

    //处理单元格图表
    private static Map<String, List<UniverDataConfig>> cellEchart(Map<String, UniverDataConfig> cellEchart, String unitId) {
        Map<String, List<UniverDataConfig>> echartMap = new HashMap<>();
        for (String key : cellEchart.keySet()) {
            UniverDataConfig univerDataConfig = cellEchart.get(key);
            if (univerDataConfig != null) {
                UniverDataConfig option = univerDataConfig.getOption();
                String sheet = univerDataConfig.getSheet();
                if (option != null) {
                    univerDataConfig.setUnitId(unitId);
                    univerDataConfig.setSubUnitId(sheet);
                    univerDataConfig.setSummaryType(option.getSummaryType());
                    univerDataConfig.setClassifyNameField(option.getClassifyNameField());
                    univerDataConfig.setSeriesNameField(option.getSeriesNameField());
                    univerDataConfig.setSeriesDataField(option.getSeriesDataField());
                    univerDataConfig.setMaxField(option.getMaxField());
                    univerDataConfig.setType(CellDataEnum.cellChart.name());
                    List<UniverDataConfig> echartList = echartMap.get(sheet) != null ? echartMap.get(sheet) : new ArrayList<>();
                    echartList.add(univerDataConfig);
                    echartMap.put(sheet, echartList);
                }
            }
        }
        return echartMap;
    }

    //解析数据格式
    private static void format(String sheet, Map<String, Map<Integer, Map<Integer, String>>> nameMap, List<UniverResource> resources) {
        UniverResource formatting = resources.stream().filter(t -> ResourceEnum.SHEET_CONDITIONAL_FORMATTING_PLUGIN.name().equals(t.getName())).findFirst().orElse(null);
        Map<String, List<UniverResourceData>> foramtMap = new HashMap<>();
        if (formatting != null && ObjectUtil.isNotEmpty(formatting.getData())) {
            Map<String, Object> data = JsonUtil.stringToMap(formatting.getData());
            data.forEach((key, value) -> {
                foramtMap.put(key, JsonUtil.getJsonToList(value, UniverResourceData.class));
            });
            if (foramtMap.get(sheet) != null) {
                List<UniverResourceData> dataList = foramtMap.get(sheet);
                for (UniverResourceData resourceData : dataList) {
                    List<UniverSheetRange> sheetRanges = resourceData.getRanges();
                    List<UniverSheetRange> rangesList = resourceRange(nameMap, sheetRanges);
                    resourceData.setRanges(rangesList);
                }
            }
            formatting.setData(JSON.toJSONString(JSON.toJSON(foramtMap)));
        }
    }

    //解析数据验证
    private static void dataValidation(String sheet, Map<String, Map<Integer, Map<Integer, String>>> nameMap, List<UniverResource> resources) {
        UniverResource validation = resources.stream().filter(t -> ResourceEnum.SHEET_DATA_VALIDATION_PLUGIN.name().equals(t.getName())).findFirst().orElse(null);
        Map<String, List<UniverResourceData>> validationMap = new HashMap<>();
        if (validation != null && ObjectUtil.isNotEmpty(validation.getData())) {
            Map<String, Object> data = JsonUtil.stringToMap(validation.getData());
            data.forEach((key, value) -> {
                validationMap.put(key, JsonUtil.getJsonToList(value, UniverResourceData.class));
            });
            if (validationMap.get(sheet) != null) {
                List<UniverResourceData> dataList = validationMap.get(sheet);
                for (UniverResourceData resourceData : dataList) {
                    List<UniverSheetRange> sheetRanges = resourceData.getRanges();
                    List<UniverSheetRange> rangesList = resourceRange(nameMap, sheetRanges);
                    resourceData.setRanges(rangesList);
                }
            }
            validation.setData(JSON.toJSONString(JSON.toJSON(validationMap)));
        }
    }


    private static void filter(String sheet, Map<String, Map<Integer, Map<Integer, String>>> nameMap, List<UniverResource> resources) {
        UniverResource filter = resources.stream().filter(t -> ResourceEnum.SHEET_FILTER_PLUGIN.name().equals(t.getName())).findFirst().orElse(null);
        Map<String, UniverResourceData> filterMap = new HashMap<>();
        if (filter != null && ObjectUtil.isNotEmpty(filter.getData())) {
            Map<String, Object> data = JsonUtil.stringToMap(filter.getData());
            data.forEach((key, value) -> {
                filterMap.put(key, JsonUtil.getJsonToBean(value, UniverResourceData.class));
            });
            if (filterMap.get(sheet) != null) {
                UniverResourceData resourceData = filterMap.get(sheet);
                if (resourceData != null) {
                    UniverSheetRange sheetRange = resourceData.getRef();
                    List<UniverSheetRange> rangesList = resourceRange(nameMap, ImmutableList.of(sheetRange));
                    Map<Integer, List<UniverSheetRange>> rangesMap = rangesList.stream().collect(Collectors.groupingBy(UniverSheetRange::getStartRow));
                    int startRow = Collections.min(rangesMap.keySet());
                    int startCol = Collections.min(rangesMap.get(startRow).stream().map(UniverSheetRange::getStartColumn).collect(Collectors.toList()));
                    int endRow = Collections.max(rangesMap.keySet());
                    int endCol = Collections.max(rangesMap.get(endRow).stream().map(UniverSheetRange::getEndColumn).collect(Collectors.toList()));
                    UniverSheetRange range = JsonUtil.getJsonToBean(sheetRange, UniverSheetRange.class);
                    range.setStartRow(startRow);
                    range.setStartColumn(startCol);
                    range.setEndRow(endRow);
                    range.setEndColumn(endCol);
                    resourceData.setRef(range);
                }
                filter.setData(JSON.toJSONString(JSON.toJSON(filterMap)));
            }
        }

    }
}
