package jnpf.util.excel;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.ImmutableList;
import jnpf.enums.*;
import jnpf.univer.data.resource.*;
import jnpf.univer.model.UniverWorkBook;
import jnpf.univer.properties.UniverBody;
import jnpf.univer.properties.UniverBodyConfig;
import jnpf.univer.properties.UniverProperties;
import jnpf.univer.resources.UniverResource;
import jnpf.univer.resources.UniverResourceData;
import jnpf.univer.sheet.*;
import jnpf.univer.style.*;
import jnpf.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.PaneInformation;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.*;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/6/20 下午4:30
 */
public class UniverXSSFExcel extends ExcelParser {

    @Override
    public UniverWorkBook formFile(InputStream inputStream) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
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
            XSSFSheet sheet = workbook.getSheetAt(i);
            univerSheetMap.put(sheet.getSheetName(), sheetId);
        }
        Map<String, String> definedName = new HashMap<>();
        Map<String, UniverResourceData> definedNameMap = definedName(workbook, definedName, univerSheetMap);
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            XSSFSheet sheet = workbook.getSheetAt(i);
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
                XSSFRow row = sheet.getRow(rowCount);
                Map<Integer, UniverSheetCellData> cells = new HashMap<>();
                if (null != row) {
                    for (int columnCount = 0; columnCount <= row.getLastCellNum(); columnCount++) {
                        XSSFCell cell = row.getCell(columnCount);
                        UniverSheetCellData univerSheetCellData = new UniverSheetCellData();
                        if (null != cell) {
                            //数据
                            Object value = "";
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
                                    value = cell.getCellFormula();
                                    formula = "=" + value;
                                    type = CellDataTypeEnum.Formula.getCode();
                                    break;
                            }
                            univerSheetCellData.setV(value);
                            univerSheetCellData.setF(formula);
                            univerSheetCellData.setT(type);
                            //样式
                            XSSFCellStyle cellStyle = cell.getCellStyle();
                            UniverStyle style = getUniverStyle(cellStyle);
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

        Map<String, UniverStyle> styles = styleMap.entrySet().stream().collect(Collectors.toMap(t -> t.getValue(), t -> t.getKey()));
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
            XSSFSheet sheet = workbook.getSheet(key);
            if (sheet != null) {
                //图片
                drawing(sheet, value, unitId, drawingMap);
                //条件
                format(sheet, value, unitId, formattingMap);
                //数据管理
                dataValidation(sheet, value, unitId, dataValidationMap);
                //筛选
                filter(sheet, value, unitId, filterMap);
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

    private UniverStyle getUniverStyle(XSSFCellStyle cellStyle) {
        UniverStyle univerStyle = new UniverStyle();
        univerStyle.setHt(HorizontalEnum.getHorizontalCode(cellStyle.getAlignment()));
        univerStyle.setVt(VerticalEnum.getVerticalCode(cellStyle.getVerticalAlignment()));

        XSSFFont font = cellStyle.getFont();
        if (null != font) {
            univerStyle.setFf(font.getFontName());
            univerStyle.setFs((int) font.getFontHeightInPoints());
            univerStyle.setIt(font.getItalic() ? 1 : 0);
            univerStyle.setBl(font.getBold() ? 1 : 0);
            XSSFColor color = font.getXSSFColor();
            if (null != color) {
                UniverStyleColor cl = new UniverStyleColor();
                cl.setRgb(rgb(color.getARGBHex()));
                univerStyle.setCl(cl);
            }
            if (Objects.equals(font.getUnderline(), Font.U_SINGLE)) {
                UniverStyleTextDecoration ul = new UniverStyleTextDecoration();
                ul.setS(1);
                univerStyle.setUl(ul);
            }
            if (font.getStrikeout()) {
                UniverStyleTextDecoration st = new UniverStyleTextDecoration();
                st.setS(1);
                univerStyle.setSt(st);
            }
        }
        if (null != cellStyle.getDataFormatString()) {
            UniverStylePattern pattern = new UniverStylePattern();
            pattern.setPattern(cellStyle.getDataFormatString());
            univerStyle.setN(pattern);
        }
        univerStyle.setBd(getUniverStyleBorder(cellStyle));

        XSSFColor bgColor = cellStyle.getFillForegroundColorColor();
        FillPatternType pattern = cellStyle.getFillPattern();
        if (null != bgColor && Objects.equals(pattern, FillPatternType.SOLID_FOREGROUND)) {
            UniverStyleColor background = new UniverStyleColor();
            background.setRgb(rgb(bgColor.getARGBHex()));
            univerStyle.setBg(background);
        }

        UniverStyleTextRotation univerStyleTextRotation = new UniverStyleTextRotation();
        univerStyleTextRotation.setA(cellStyle.getRotation());
        univerStyle.setTr(univerStyleTextRotation);
        return univerStyle;
    }

    private String rgb(String color) {
        String rgb = null;
        if (StringUtil.isNotEmpty(color)) {
            rgb = "#" + color.substring(2, 8);
        }
        return rgb;
    }

    private UniverStyleBorder getUniverStyleBorder(XSSFCellStyle cellStyle) {
        UniverStyleBorder univerStyleBorder = new UniverStyleBorder();
        int num = 0;
        XSSFColor leftBorder = cellStyle.getLeftBorderXSSFColor();
        if (null != leftBorder) {
            UniverStyleBorderStyle left = new UniverStyleBorderStyle();
            UniverStyleColor leftColor = new UniverStyleColor();
            leftColor.setRgb(rgb(leftBorder.getARGBHex()));
            left.setCl(leftColor);
            univerStyleBorder.setL(left);
            num++;
        }
        XSSFColor topBorder = cellStyle.getTopBorderXSSFColor();
        if (null != topBorder) {
            UniverStyleBorderStyle top = new UniverStyleBorderStyle();
            UniverStyleColor topColor = new UniverStyleColor();
            topColor.setRgb(rgb(topBorder.getARGBHex()));
            top.setCl(topColor);
            univerStyleBorder.setT(top);
            num++;
        }
        XSSFColor rightBorder = cellStyle.getRightBorderXSSFColor();
        if (null != rightBorder) {
            UniverStyleBorderStyle right = new UniverStyleBorderStyle();
            UniverStyleColor rightColor = new UniverStyleColor();
            rightColor.setRgb(rgb(rightBorder.getARGBHex()));
            right.setCl(rightColor);
            univerStyleBorder.setR(right);
            num++;
        }
        XSSFColor bottomBorder = cellStyle.getBottomBorderXSSFColor();
        if (null != bottomBorder) {
            UniverStyleBorderStyle bottom = new UniverStyleBorderStyle();
            UniverStyleColor bottomColor = new UniverStyleColor();
            bottomColor.setRgb(rgb(bottomBorder.getARGBHex()));
            bottom.setCl(bottomColor);
            univerStyleBorder.setB(bottom);
            num++;
        }
        return num > 0 ? univerStyleBorder : null;
    }

    private List<UniverResource> getUniverResources(Map<String, String> resourceMap) {
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

    private void drawing(XSSFSheet sheet, String sheetId, String unitId, Map<String, UniverResourceData> drawingMap) {
        List<String> order = new ArrayList<>();
        Map<String, UniverDrawing> data = new HashMap<>();
        XSSFDrawing drawing = sheet.getDrawingPatriarch();
        if (drawing != null) {
            List<XSSFShape> shapes = drawing.getShapes();
            for (XSSFShape shape : shapes) {
                if (shape instanceof XSSFPicture) {
                    XSSFPicture picture = (XSSFPicture) shape;
                    byte[] pictureData = picture.getPictureData().getData();
                    if (pictureData.length > 0) {
                        String drawingId = RandomUtil.randomString(10);
                        order.add(drawingId);
                        BufferedImage image = ImgUtil.toImage(pictureData);
                        UniverTransform sheetTransform = new UniverTransform();

                        XSSFClientAnchor anchor = picture.getClientAnchor();
                        UniverOffset univerFrom = new UniverOffset();
                        univerFrom.setRowOffset(0);
                        univerFrom.setColumnOffset(0);
                        univerFrom.setRow(anchor.getRow1());
                        univerFrom.setColumn((int) anchor.getCol1());
                        sheetTransform.setFrom(univerFrom);
                        UniverOffset univerTo = new UniverOffset();
                        univerTo.setRowOffset(0);
                        univerTo.setColumnOffset(0);
                        univerTo.setRow(anchor.getRow2());
                        univerTo.setColumn((int) anchor.getCol2());
                        sheetTransform.setTo(univerTo);
                        UniverDrawing univerDrawing = new UniverDrawing();
                        univerDrawing.setSheetTransform(sheetTransform);
                        UniverTransform transform = new UniverTransform();
                        transform.setFlipX(false);
                        transform.setFlipY(false);
                        transform.setSkewX(0);
                        transform.setSkewY(0);
                        transform.setAngle(0);
                        transform.setLeft(500);
                        transform.setTop(500);
                        transform.setHeight(500);
                        transform.setWidth(500);
                        univerDrawing.setTransform(transform);
                        univerDrawing.setAllowTransform(true);
                        univerDrawing.setDrawingType(0);
                        univerDrawing.setUnitId(unitId);
                        univerDrawing.setSubUnitId(sheetId);
                        univerDrawing.setDrawingId(drawingId);
                        univerDrawing.setImageSourceType(ImageEnum.BASE64.name());
                        univerDrawing.setSource("data:image/jpeg;base64," + ImgUtil.toBase64(image, "jpeg"));
                        data.put(drawingId, univerDrawing);
                    }
                }
            }
        }
        if (!order.isEmpty()) {
            UniverResourceData resourceData = new UniverResourceData();
            resourceData.setData(data);
            resourceData.setOrder(order);
            drawingMap.put(sheetId, resourceData);
        }
    }

    private void format(XSSFSheet sheet, String sheetId, String unitId, Map<String, List<UniverResourceData>> formattingMap) {
        StylesTable styles = sheet.getWorkbook().getStylesSource();
        List<UniverResourceData> formatList = new ArrayList<>();
        List<CTConditionalFormatting> conditionalFormatting = sheet.getCTWorksheet().getConditionalFormattingList();
        for (CTConditionalFormatting conditional : conditionalFormatting) {
            UniverResourceData resourceData = new UniverResourceData();
            resourceData.setCfId(RandomUtil.randomString(10));
            List<UniverSheetRange> ranges = new ArrayList<>();
            resourceData.setRanges(ranges);
            List<String> formattingRanges = conditional.getSqref();
            for (String range : formattingRanges) {
                UniverSheetRange sheetRange = new UniverSheetRange();
                CellRangeAddress rangeAddress = CellRangeAddress.valueOf(range);
                sheetRange.setStartRow(rangeAddress.getFirstRow());
                sheetRange.setStartColumn(rangeAddress.getFirstColumn());
                sheetRange.setEndRow(rangeAddress.getLastRow());
                sheetRange.setEndColumn(rangeAddress.getLastColumn());
                sheetRange.setUnitId(unitId);
                sheetRange.setSheetId(sheetId);
                ranges.add(sheetRange);
            }
            List<CTCfRule> cfRuleList = conditional.getCfRuleList();
            for (CTCfRule cfRule : cfRuleList) {
                CTIconSet iconSet = cfRule.getIconSet();
                if (iconSet != null) {
                    boolean reverse = iconSet.getReverse();
                    UniverRule rule = new UniverRule();
                    List<UniverConfig> univerConfig = new ArrayList<>();
                    rule.setType(FormatTypeEnum.iconSet.name());
                    rule.setConfig(univerConfig);
                    rule.setIsShowValue(iconSet.getShowValue());
                    String iconType = iconSet.getIconSet().toString();
                    List<CTCfvo> cfvoList = iconSet.getCfvoList();
                    int iconId = 0;
                    for (int k = cfvoList.size() - 1; k >= 0; k--) {
                        CTCfvo cfvo = cfvoList.get(k);
                        UniverValue value = new UniverValue();
                        String type = cfvo.getType().toString();
                        String cfValue = cfvo.getVal() != null ? cfvo.getVal().toString() : "";
                        String val = (Objects.equals(SubTypeEnum.expression.getType(), type) ? "=" : "") + cfValue.replaceAll("\"", "");
                        value.setValue(val);
                        value.setType(type);
                        UniverConfig config = new UniverConfig();
                        config.setOperator(cfvo.getGte() ? OperatorEnum.greaterThanOrEqual.name() : OperatorEnum.greaterThan.name());
                        config.setIconId((reverse ? k : iconId) + "");
                        config.setIconType(iconType);
                        config.setValue(value);
                        univerConfig.add(config);
                        iconId++;
                    }
                    resourceData.setRule(rule);
                }
                CTDataBar dataBar = cfRule.getDataBar();
                if (dataBar != null) {
                    UniverRule rule = new UniverRule();
                    UniverConfig config = new UniverConfig();
                    rule.setConfig(config);
                    rule.setType(FormatTypeEnum.dataBar.name());
                    rule.setIsShowValue(dataBar.getShowValue());
                    List<CTCfvo> cfvoList = dataBar.getCfvoList();
                    UniverValue min = new UniverValue();
                    min.setType(OperatorEnum.min.name());
                    config.setMin(min);
                    UniverValue max = new UniverValue();
                    max.setType(OperatorEnum.max.name());
                    config.setMax(max);
                    for (int k = 0; k < cfvoList.size(); k++) {
                        CTCfvo cfvo = cfvoList.get(k);
                        String type = cfvo.getType().toString();
                        String cfValue = cfvo.getVal() != null ? cfvo.getVal().toString() : "";
                        String val = (Objects.equals(SubTypeEnum.expression.getType(), type) ? "=" : "") + cfValue.replaceAll("\"", "");
                        if (k == 0) {
                            min.setValue(val);
                            min.setType(type);
                        } else {
                            max.setValue(val);
                            max.setType(Objects.equals(type, OperatorEnum.min.name()) ? max.getType() : type);
                        }
                    }
                    config.setIsGradient(false);
                    CTColor ctColor = dataBar.getColor();
                    if (ctColor != null) {
                        XSSFColor color = new XSSFColor(ctColor.getRgb());
                        String colorRgb = rgb(color.getARGBHex());
                        config.setPositiveColor(colorRgb);
                        config.setNativeColor(colorRgb);
                    }
                    resourceData.setRule(rule);
                }
                CTColorScale colorScale = cfRule.getColorScale();
                if (colorScale != null) {
                    UniverRule rule = new UniverRule();
                    List<UniverConfig> univerConfig = new ArrayList<>();
                    rule.setType(FormatTypeEnum.colorScale.name());
                    rule.setConfig(univerConfig);
                    Map<Integer, UniverValue> colorValue = new HashMap<>();
                    List<CTCfvo> cfvoList = colorScale.getCfvoList();
                    for (int k = 0; k < cfvoList.size(); k++) {
                        CTCfvo cfvo = cfvoList.get(k);
                        UniverValue value = new UniverValue();
                        String type = cfvo.getType().toString();
                        String cfValue = cfvo.getVal() != null ? cfvo.getVal().toString() : "";
                        String val = (Objects.equals(SubTypeEnum.expression.getType(), type) ? "=" : "") + cfValue.replaceAll("\"", "");
                        value.setType(type);
                        value.setValue(val);
                        colorValue.put(k, value);
                    }
                    List<CTColor> colorList = colorScale.getColorList();
                    Map<Integer, String> colorColor = new HashMap<>();
                    for (int k = 0; k < colorList.size(); k++) {
                        CTColor ctColor = colorList.get(k);
                        XSSFColor color = new XSSFColor(ctColor.getRgb());
                        String colorRgb = rgb(color.getARGBHex());
                        colorColor.put(k, colorRgb);
                    }
                    for (Integer k : colorValue.keySet()) {
                        UniverConfig config = new UniverConfig();
                        config.setValue(colorValue.get(k));
                        config.setColor(colorColor.get(k));
                        config.setIndex(k);
                        univerConfig.add(config);
                    }
                    resourceData.setRule(rule);
                }
                CTCfRule highlightCell = cfRule;
                if (highlightCell != null) {
                    String type = highlightCell.getType().toString();
                    SubTypeEnum subTypeEnum = SubTypeEnum.getType(type);
                    if (subTypeEnum != null) {
                        UniverRule rule = new UniverRule();
                        rule.setType(FormatTypeEnum.highlightCell.name());
                        boolean isUnique = ImmutableList.of(SubTypeEnum.uniqueValues.getType(), SubTypeEnum.duplicateValues.getType()).contains(subTypeEnum.getType());
                        boolean isNumber = Objects.equals(SubTypeEnum.cellIs.getType(), subTypeEnum.getType());
                        boolean isAverage = Objects.equals(SubTypeEnum.aboveAverage.getType(), subTypeEnum.getType());
                        boolean isRank = Objects.equals(SubTypeEnum.top10.getType(), subTypeEnum.getType());
                        boolean isFormula = Objects.equals(SubTypeEnum.expression.getType(), subTypeEnum.getType());
                        boolean isTime = Objects.equals(SubTypeEnum.timePeriod.getType(), subTypeEnum.getType());
                        List<String> dataList = highlightCell.getFormulaList() != null ? highlightCell.getFormulaList() : new ArrayList<>();
                        List<BigDecimal> numberDataList = new ArrayList<>();
                        BigDecimal numberData = null;
                        String textValue = "";
                        for (String data : dataList) {
                            try {
                                BigDecimal bigDecimal = new BigDecimal(data);
                                numberDataList.add(bigDecimal);
                                numberData = bigDecimal;
                            } catch (Exception e) {
                                textValue = data;
                            }
                        }
                        if (isNumber) {
                            if (highlightCell.getOperator() != null) {
                                rule.setOperator(highlightCell.getOperator().toString());
                            }
                            boolean isText = numberDataList.isEmpty();
                            rule.setSubType(isText ? SubTypeEnum.text.getCode() : SubTypeEnum.cellIs.getCode());
                            rule.setValue(isText ? textValue : numberDataList.size() > 1 ? numberDataList : numberData);
                        } else if (isAverage) {
                            rule.setSubType(subTypeEnum.getCode());
                            rule.setOperator(highlightCell.getAboveAverage() ? OperatorEnum.greaterThan.name() : OperatorEnum.lessThan.name());
                        } else if (isRank) {
                            rule.setSubType(subTypeEnum.getCode());
                            rule.setIsPercent(highlightCell.getPercent());
                            rule.setIsBottom(highlightCell.getBottom());
                            rule.setValue(highlightCell.getRank() + "");
                        } else if (isFormula) {
                            rule.setSubType(SubTypeEnum.expression.getCode());
                            rule.setValue("=" + textValue.replaceAll("\"", ""));
                        } else if (isTime) {
                            rule.setSubType(subTypeEnum.getCode());
                            rule.setOperator(highlightCell.getTimePeriod().toString());
                        } else if (isUnique) {
                            rule.setSubType(subTypeEnum.getType());
                        } else {
                            rule.setSubType(SubTypeEnum.text.getCode());
                            rule.setOperator(highlightCell.getOperator() != null ? highlightCell.getOperator().toString() : subTypeEnum.getType());
                            if (highlightCell.getText() != null) {
                                rule.setValue(highlightCell.getText());
                            }
                        }
                        UniverStyle style = new UniverStyle();
                        CTDxf dxf = highlightCell.isSetDxfId() ? styles.getDxfAt((int) highlightCell.getDxfId()) : null;
                        if (dxf != null) {
                            CTFill ctFill = dxf.getFill();
                            if (ctFill != null) {
                                CTPatternFill patternFill = ctFill.getPatternFill();
                                if (patternFill != null) {
                                    XSSFColor baColor = XSSFColor.from(patternFill.getBgColor());
                                    if (baColor != null) {
                                        UniverStyleColor bg = new UniverStyleColor();
                                        bg.setRgb(rgb(baColor.getARGBHex()));
                                        style.setBg(bg);
                                    }
                                }
                            }
                            CTFont ctFont = dxf.getFont();
                            if (ctFont != null) {
                                XSSFColor fontColor = ctFont.sizeOfColorArray() > 0 ? XSSFColor.from(ctFont.getColorArray(0)) : null;
                                if (fontColor != null) {
                                    UniverStyleColor cl = new UniverStyleColor();
                                    cl.setRgb(rgb(fontColor.getARGBHex()));
                                    style.setCl(cl);
                                }
                                STUnderlineValues.Enum underlineType = ctFont.sizeOfUArray() == 1 ? ctFont.getUArray(0).getVal() : STUnderlineValues.NONE;
                                if (Objects.equals(underlineType, STUnderlineValues.SINGLE)) {
                                    UniverStyleTextDecoration ul = new UniverStyleTextDecoration();
                                    ul.setS(1);
                                    style.setUl(ul);
                                }
                                boolean strike = ctFont.sizeOfStrikeArray() > 0 && ctFont.getStrikeArray(0).getVal();
                                if (strike) {
                                    UniverStyleTextDecoration st = new UniverStyleTextDecoration();
                                    st.setS(1);
                                    style.setSt(st);
                                }
                                boolean isItalic = ctFont.sizeOfIArray() == 1 && ctFont.getIArray(0).getVal();
                                boolean isBold = ctFont.sizeOfBArray() == 1 && ctFont.getBArray(0).getVal();
                                style.setIt(isItalic ? 1 : 0);
                                style.setBl(isBold ? 1 : 0);
                            }
                        }
                        rule.setStyle(style);
                        resourceData.setRule(rule);
                    }
                }
                resourceData.setStopIfTrue(cfRule.getStopIfTrue());
                if (resourceData.getRule() == null) {
                    continue;
                }
                formatList.add(resourceData);
            }
        }
        formattingMap.put(sheetId, formatList);
    }

    private static void dataValidation(XSSFSheet sheet, String sheetId, String unitId, Map<String, List<UniverResourceData>> dataValidationMap) {
        List<UniverResourceData> dataValidationList = new ArrayList<>();
        List<XSSFDataValidation> dataValidations = sheet.getDataValidations();
        for (XSSFDataValidation dataValidation : dataValidations) {
            UniverResourceData resourceData = new UniverResourceData();
            List<UniverSheetRange> ranges = new ArrayList<>();
            resourceData.setRanges(ranges);
            resourceData.setUid(RandomUtil.randomString(10));
            CellRangeAddressList addressList = dataValidation.getRegions();
            for (CellRangeAddress address : addressList.getGenericChildren()) {
                UniverSheetRange sheetRange = new UniverSheetRange();
                sheetRange.setStartRow(address.getFirstRow());
                sheetRange.setStartColumn(address.getFirstColumn());
                sheetRange.setEndRow(address.getLastRow());
                sheetRange.setEndColumn(address.getLastColumn());
                sheetRange.setSheetId(sheetId);
                sheetRange.setUnitId(unitId);
                ranges.add(sheetRange);
            }
            DataValidationConstraint constraint = dataValidation.getValidationConstraint();
            ValidationType validationType = ValidationType.getType(constraint.getValidationType());
            resourceData.setType(validationType.getType());
            operatorTypeEnum operator = Objects.equals(validationType, ValidationType.none) ? null : operatorTypeEnum.getOperator(constraint.getOperator());
            resourceData.setOperator(operator != null ? operator.getOperatorType() : null);
            String value1 = constraint.getFormula1();
            String value2 = constraint.getFormula2();
            resourceData.setFormula1(value1 != null ? value1.replaceAll("\"", "") : value1);
            resourceData.setFormula2(value2 != null ? value2.replaceAll("\"", "") : value2);
            if (dataValidation.getShowErrorBox()) {
                resourceData.setShowErrorMessage(true);
                resourceData.setError(dataValidation.getErrorBoxTitle());
                resourceData.setErrorStyle(Objects.equals(dataValidation.getErrorStyle(), 0) ? 1 : 2);
            }
            resourceData.setAllowBlank(dataValidation.getEmptyCellAllowed());
            resourceData.setRenderMode(dataValidation.getSuppressDropDownArrow() ? null : 0);
            dataValidationList.add(resourceData);
        }
        dataValidationMap.put(sheetId, dataValidationList);
    }

    private void filter(XSSFSheet sheet, String sheetId, String unitId, Map<String, UniverResourceData> filterMap) {
        CTAutoFilter autoFilter = sheet.getCTWorksheet().getAutoFilter();
        UniverResourceData data = new UniverResourceData();
        if (autoFilter != null && StringUtil.isNotEmpty(autoFilter.getRef())) {
            CellRangeAddress addresses = CellRangeAddress.valueOf(autoFilter.getRef());
            UniverSheetRange ref = new UniverSheetRange();
            ref.setStartRow(addresses.getFirstRow());
            ref.setStartColumn(addresses.getFirstColumn());
            ref.setEndRow(addresses.getLastRow());
            ref.setEndColumn(addresses.getLastColumn());
            data.setRef(ref);
            List<Integer> cachedFilteredOut = new ArrayList<>();
            //隐藏行
            for (int rowIndex = addresses.getFirstRow(); rowIndex <= addresses.getLastRow(); rowIndex++) {
                XSSFRow row = sheet.getRow(rowIndex);
                if (row != null && row.getZeroHeight()) {
                    cachedFilteredOut.add(rowIndex);
                }
            }
            //筛选条件
            List<CTFilterColumn> filterColumnList = autoFilter.getFilterColumnList();
            List<UniverFilters> filterColumns = new ArrayList<>();
            for (CTFilterColumn filterColumn : filterColumnList) {
                UniverFilters univerFilters = new UniverFilters();
                univerFilters.setColId(filterColumn.getColId());

                //poi方法
                CTFilters ctFilters = filterColumn.getFilters();
                if (BeanUtil.isNotEmpty(ctFilters)) {
                    UniverCustomFilters univerCustomFilters = new UniverCustomFilters();
                    univerFilters.setFilters(univerCustomFilters);
                    univerCustomFilters.setAnd(ctFilters.getBlank() ? 1 : null);
                    List<CTFilter> filtersFilterList = ctFilters.getFilterList();
                    for (CTFilter ctFilter : filtersFilterList) {
                        String val = ctFilter.getVal();
                        ArrayList<String> strings = new ArrayList<>();
                        strings.add(val);
                        univerCustomFilters.setFilters(strings);
                    }
                    filterColumns.add(univerFilters);

                }

                CTCustomFilters filters = filterColumn.getCustomFilters();
                if (null != filters) {
                    UniverCustomFilters customFilters = new UniverCustomFilters();

                    univerFilters.setCustomFilters(customFilters);

                    List<UniverCustomFilters> customFilterList = new ArrayList<>();
                    customFilters.setCustomFilters(customFilterList);
                    customFilters.setAnd(filters.getAnd() ? 1 : null);
                    List<CTCustomFilter> filterList = filters.getCustomFilterList();
                    for (CTCustomFilter filter : filterList) {
                        UniverCustomFilters univerCustomFilter = new UniverCustomFilters();
                        univerCustomFilter.setVal(filter.getVal());
//                        univerCustomFilter.setOperator(filter.getOperator() != null ? filter.getOperator().toString() : null);
                        customFilterList.add(univerCustomFilter);
                    }
                    filterColumns.add(univerFilters);
                }
            }
            data.setFilterColumns(filterColumns);
            data.setCachedFilteredOut(cachedFilteredOut);
            filterMap.put(sheetId, data);
        }
    }

    private Map<String, UniverResourceData> definedName(XSSFWorkbook workbook, Map<String, String> definedName, Map<String, String> univerSheetMap) {
        Map<String, UniverResourceData> definedNameMap = new HashMap<>();
        for (XSSFName name : workbook.getAllNames()) {
            String definedId = RandomUtil.randomString(10);
            String localSheetId = "AllDefaultWorkbook";
            String nameName = name.getNameName();
            int sheetIndex = name.getSheetIndex();
            String sheetName = "";
            if (sheetIndex > -1) {
                XSSFSheet sheetAt = workbook.getSheetAt(sheetIndex);
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

    private void link(XSSFCell cell, Object value, Map<String, String> definedName, Map<String, String> univerSheetMap, UniverSheetCellData univerSheetCellData) {
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
}
