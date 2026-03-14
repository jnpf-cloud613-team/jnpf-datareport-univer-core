package jnpf.util.excel;

import cn.hutool.core.img.ImgUtil;
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
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.hssf.record.NameRecord;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.PaneInformation;

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
public class UniverHSSFExcel extends ExcelParser {

    @Override
    public UniverWorkBook formFile(InputStream inputStream) throws IOException {
        HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
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
            HSSFSheet sheet = workbook.getSheetAt(i);
            univerSheetMap.put(sheet.getSheetName(), sheetId);
        }
        Map<String, String> definedName = new HashMap<>();
        Map<String, UniverResourceData> definedNameMap = definedName(workbook, definedName, univerSheetMap);
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            HSSFSheet sheet = workbook.getSheetAt(i);
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
                HSSFRow row = sheet.getRow(rowCount);
                Map<Integer, UniverSheetCellData> cells = new HashMap<>();
                if (null != row) {
                    for (int columnCount = 0; columnCount <= row.getLastCellNum(); columnCount++) {
                        HSSFCell cell = row.getCell(columnCount);
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
                            HSSFCellStyle cellStyle = cell.getCellStyle();
                            UniverStyle style = getUniverStyle(cellStyle, workbook);
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
            HSSFSheet sheet = workbook.getSheet(key);
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

    private UniverStyle getUniverStyle(HSSFCellStyle cellStyle, HSSFWorkbook workbook) {
        UniverStyle univerStyle = new UniverStyle();
        univerStyle.setHt(HorizontalEnum.getHorizontalCode(cellStyle.getAlignment()));
        univerStyle.setVt(VerticalEnum.getVerticalCode(cellStyle.getVerticalAlignment()));

        HSSFFont font = cellStyle.getFont(workbook);
        if (null != font) {
            univerStyle.setFf(font.getFontName());
            univerStyle.setFs((int) font.getFontHeightInPoints());
            univerStyle.setIt(font.getItalic() ? 1 : 0);
            univerStyle.setBl(font.getBold() ? 1 : 0);
            HSSFColor color = font.getHSSFColor(workbook);
            if (null != color) {
                UniverStyleColor cl = new UniverStyleColor();
                cl.setRgb(rgb(color.getTriplet()));
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
        univerStyle.setBd(getUniverStyleBorder(cellStyle, workbook));

        HSSFColor bgColor = cellStyle.getFillForegroundColorColor();
        FillPatternType pattern = cellStyle.getFillPattern();
        if (null != bgColor && Objects.equals(pattern, FillPatternType.SOLID_FOREGROUND)) {
            UniverStyleColor background = new UniverStyleColor();
            background.setRgb(rgb(bgColor.getTriplet()));
            univerStyle.setBg(background);
        }

        UniverStyleTextRotation univerStyleTextRotation = new UniverStyleTextRotation();
        univerStyleTextRotation.setA(cellStyle.getRotation());
        univerStyle.setTr(univerStyleTextRotation);
        return univerStyle;
    }

    private String rgb(short[] color) {
        String rgb = null;
        if (null != color && color.length == 3) {
            rgb = String.format("#%02X%02X%02X", color[0], color[1], color[2]);
        }
        return rgb;
    }

    private UniverStyleBorder getUniverStyleBorder(HSSFCellStyle cellStyle, HSSFWorkbook workbook) {
        UniverStyleBorder univerStyleBorder = new UniverStyleBorder();
        int num = 0;
        HSSFPalette palette = workbook.getCustomPalette();
        HSSFColor leftBorder = palette.getColor(cellStyle.getLeftBorderColor());
        if (null != leftBorder) {
            UniverStyleBorderStyle left = new UniverStyleBorderStyle();
            UniverStyleColor leftColor = new UniverStyleColor();
            leftColor.setRgb(rgb(leftBorder.getTriplet()));
            left.setCl(leftColor);
            univerStyleBorder.setL(left);
            num++;
        }
        HSSFColor topBorder = palette.getColor(cellStyle.getTopBorderColor());
        if (null != topBorder) {
            UniverStyleBorderStyle top = new UniverStyleBorderStyle();
            UniverStyleColor topColor = new UniverStyleColor();
            topColor.setRgb(rgb(topBorder.getTriplet()));
            top.setCl(topColor);
            univerStyleBorder.setT(top);
            num++;
        }
        HSSFColor rightBorder = palette.getColor(cellStyle.getRightBorderColor());
        if (null != rightBorder) {
            UniverStyleBorderStyle right = new UniverStyleBorderStyle();
            UniverStyleColor rightColor = new UniverStyleColor();
            rightColor.setRgb(rgb(rightBorder.getTriplet()));
            right.setCl(rightColor);
            univerStyleBorder.setR(right);
            num++;
        }
        HSSFColor bottomBorder = palette.getColor(cellStyle.getBottomBorderColor());
        if (null != bottomBorder) {
            UniverStyleBorderStyle bottom = new UniverStyleBorderStyle();
            UniverStyleColor bottomColor = new UniverStyleColor();
            bottomColor.setRgb(rgb(bottomBorder.getTriplet()));
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

    private void drawing(HSSFSheet sheet, String sheetId, String unitId, Map<String, UniverResourceData> drawingMap) {
        List<String> order = new ArrayList<>();
        Map<String, UniverDrawing> data = new HashMap<>();
        HSSFPatriarch drawing = sheet.getDrawingPatriarch();
        if (drawing != null) {
            List<HSSFShape> shapes = drawing.getChildren();
            for (HSSFShape shape : shapes) {
                if (shape instanceof HSSFPicture) {
                    HSSFPicture picture = (HSSFPicture) shape;
                    byte[] pictureData = picture.getPictureData().getData();
                    if (pictureData.length > 0) {
                        String drawingId = RandomUtil.randomString(10);
                        order.add(drawingId);
                        BufferedImage image = ImgUtil.toImage(pictureData);
                        UniverTransform sheetTransform = new UniverTransform();
                        HSSFClientAnchor anchor = picture.getClientAnchor();
                        UniverOffset univerFrom = new UniverOffset();
                        univerFrom.setRow(anchor.getRow1());
                        univerFrom.setColumn((int) anchor.getCol1());
                        univerFrom.setRowOffset(0);
                        univerFrom.setColumnOffset(0);
                        sheetTransform.setFrom(univerFrom);
                        UniverOffset univerTo = new UniverOffset();
                        univerTo.setRow(anchor.getRow2());
                        univerTo.setColumn((int) anchor.getCol2());
                        univerTo.setRowOffset(0);
                        univerTo.setColumnOffset(0);
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

    private void format(HSSFSheet sheet, String sheetId, String unitId, Map<String, List<UniverResourceData>> formattingMap) {
        //todo 提供色阶、数据条、色阶方法，其他方法获取的数据有问题
        List<UniverResourceData> formatList = new ArrayList<>();
        HSSFSheetConditionalFormatting formatting = sheet.getSheetConditionalFormatting();
        if (null != formatting) {
            for (int i = 0; i < formatting.getNumConditionalFormattings(); i++) {
                HSSFConditionalFormatting format = formatting.getConditionalFormattingAt(i);
                UniverResourceData resourceData = new UniverResourceData();
                resourceData.setCfId(RandomUtil.randomString(10));
                CellRangeAddress[] formattingRanges = format.getFormattingRanges();
                List<UniverSheetRange> ranges = new ArrayList<>();
                resourceData.setRanges(ranges);
                for (CellRangeAddress rangeAddress : formattingRanges) {
                    UniverSheetRange range = new UniverSheetRange();
                    range.setStartRow(rangeAddress.getFirstRow());
                    range.setStartColumn(rangeAddress.getFirstColumn());
                    range.setEndRow(rangeAddress.getLastRow());
                    range.setEndColumn(rangeAddress.getLastColumn());
                    range.setUnitId(unitId);
                    range.setSheetId(sheetId);
                    ranges.add(range);
                }
                for (int num = 0; num < format.getNumberOfRules(); num++) {
                    HSSFConditionalFormattingRule formatRule = format.getRule(num);
                    HSSFIconMultiStateFormatting iconSet = formatRule.getMultiStateFormatting();
                    if (iconSet != null) {
                        boolean reverse = iconSet.isReversed();
                        UniverRule rule = new UniverRule();
                        List<UniverConfig> univerConfig = new ArrayList<>();
                        rule.setType(FormatTypeEnum.iconSet.name());
                        rule.setConfig(univerConfig);
                        //显示数据条没有属性
                        rule.setIsShowValue(false);
                        String iconType = iconSet.getIconSet().name;
                        HSSFConditionalFormattingThreshold[] cfvoList = iconSet.getThresholds();
                        int iconId = 0;
                        for (int k = cfvoList.length - 1; k >= 0; k--) {
                            HSSFConditionalFormattingThreshold cfvo = cfvoList[k];
                            UniverValue value = new UniverValue();
                            String type = cfvo.getRangeType().name;
                            String val = Objects.equals(SubTypeEnum.expression.getCode(), type) ? "=" + cfvo.getFormula().replaceAll("\"", "") : cfvo.getValue() + "";
                            value.setValue(val);
                            value.setType(type);
                            UniverConfig config = new UniverConfig();
                            config.setOperator(OperatorEnum.greaterThan.name());
                            config.setIconId((reverse ? k : iconId) + "");
                            config.setIconType(iconType);
                            config.setValue(value);
                            univerConfig.add(config);
                            iconId++;
                        }
                        resourceData.setRule(rule);
                    }

                    HSSFDataBarFormatting dataBar = formatRule.getDataBarFormatting();
                    if (dataBar != null) {
                        UniverRule rule = new UniverRule();
                        UniverConfig config = new UniverConfig();
                        rule.setConfig(config);
                        rule.setType(FormatTypeEnum.dataBar.name());
                        //显示数据条没有属性
                        rule.setIsShowValue(false);
                        HSSFConditionalFormattingThreshold minThreshold = dataBar.getMinThreshold();
                        UniverValue min = new UniverValue();
                        min.setType(minThreshold.getRangeType().name);
                        min.setValue(Objects.equals(SubTypeEnum.expression.getCode(), min.getType()) ? "=" + minThreshold.getFormula().replaceAll("\"", "") : minThreshold.getValue() + "");
                        config.setMin(min);
                        HSSFConditionalFormattingThreshold maxThreshold = dataBar.getMaxThreshold();
                        UniverValue max = new UniverValue();
                        max.setType(maxThreshold.getRangeType().name);
                        max.setValue(Objects.equals(SubTypeEnum.expression.getCode(), max.getType()) ? "=" + maxThreshold.getFormula().replaceAll("\"", "") : maxThreshold.getValue() + "");
                        config.setMax(max);
                        config.setIsGradient(true);
                        resourceData.setRule(rule);
                    }

                    HSSFColorScaleFormatting colorScale = formatRule.getColorScaleFormatting();
                    if (colorScale != null) {
                        UniverRule rule = new UniverRule();
                        List<UniverConfig> univerConfig = new ArrayList<>();
                        rule.setType(FormatTypeEnum.colorScale.name());
                        rule.setConfig(univerConfig);
                        Map<Integer, UniverValue> colorValue = new HashMap<>();
                        HSSFConditionalFormattingThreshold[] cfvoList = colorScale.getThresholds();
                        for (int k = 0; k < cfvoList.length; k++) {
                            HSSFConditionalFormattingThreshold cfvo = cfvoList[k];
                            UniverValue value = new UniverValue();
                            String type = cfvo.getRangeType().name;
                            String val = Objects.equals(SubTypeEnum.expression.getCode(), type) ? "=" + cfvo.getFormula().replaceAll("\"", "") : cfvo.getValue() + "";
                            value.setType(type);
                            value.setValue(val);
                            colorValue.put(k, value);
                        }
                        for (Integer k : colorValue.keySet()) {
                            UniverConfig config = new UniverConfig();
                            config.setValue(colorValue.get(k));
                            config.setIndex(k);
                            univerConfig.add(config);
                        }
                        resourceData.setRule(rule);
                    }

                    resourceData.setStopIfTrue(formatRule.getStopIfTrue());
                    if (resourceData.getRule() == null) {
                        continue;
                    }
                    formatList.add(resourceData);
                }
            }
        }
        formattingMap.put(sheetId, formatList);
    }

    private void filter(HSSFSheet sheet, String sheetId, String unitId, Map<String, UniverResourceData> filterMap) {
        //todo 只有获取筛选的格子，没有提供筛选的内容
        int sheetIndex = sheet.getWorkbook().getSheetIndex(sheet) + 1;
        UniverResourceData data = new UniverResourceData();
        InternalWorkbook workbook = sheet.getWorkbook().getWorkbook();
        if (workbook != null) {
            NameRecord name = workbook.getSpecificBuiltinRecord((byte) 13, sheetIndex);
            if (name != null) {
                Ptg[] nameDefinition = name.getNameDefinition();
                String formulaString = HSSFFormulaParser.toFormulaString(sheet.getWorkbook(), nameDefinition);
                CellRangeAddress addresses = CellRangeAddress.valueOf(formulaString);
                UniverSheetRange ref = new UniverSheetRange();
                ref.setStartRow(addresses.getFirstRow());
                ref.setStartColumn(addresses.getFirstColumn());
                ref.setEndRow(addresses.getLastRow());
                ref.setEndColumn(addresses.getLastColumn());
                data.setRef(ref);
                List<UniverFilters> filterColumns = new ArrayList();
                data.setFilterColumns(filterColumns);
                List<Integer> cachedFilteredOut = new ArrayList<>();
                data.setCachedFilteredOut(cachedFilteredOut);
                filterMap.put(sheetId, data);
            }
        }
    }

    private static void dataValidation(HSSFSheet sheet, String sheetId, String unitId, Map<String, List<UniverResourceData>> dataValidationMap) {
        List<UniverResourceData> dataValidationList = new ArrayList<>();
        List<HSSFDataValidation> dataValidations = sheet.getDataValidations();
        for (HSSFDataValidation dataValidation : dataValidations) {
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
            DVConstraint constraint = (DVConstraint) dataValidation.getValidationConstraint();
            ValidationType validationType = ValidationType.getType(constraint.getValidationType());
            resourceData.setType(validationType.getType());
            operatorTypeEnum operator = Objects.equals(validationType, ValidationType.none) ? null : operatorTypeEnum.getOperator(constraint.getOperator());
            resourceData.setOperator(operator != null ? operator.getOperatorType() : null);
            String value1 = constraint.getValue1() != null ? constraint.getValue1().toString() : constraint.getFormula1();
            String value2 = constraint.getValue2() != null ? constraint.getValue2().toString() : constraint.getFormula2();
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

    private Map<String, UniverResourceData> definedName(HSSFWorkbook workbook, Map<String, String> definedName, Map<String, String> univerSheetMap) {
        Map<String, UniverResourceData> definedNameMap = new HashMap<>();
        for (HSSFName name : workbook.getAllNames()) {
            String definedId = RandomUtil.randomString(10);
            String localSheetId = "AllDefaultWorkbook";
            String nameName = name.getNameName();
            int sheetIndex = name.getSheetIndex();
            String sheetName = "";
            if (sheetIndex > -1) {
                HSSFSheet sheetAt = workbook.getSheetAt(sheetIndex);
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

    private void link(HSSFCell cell, Object value, Map<String, String> definedName, Map<String, String> univerSheetMap, UniverSheetCellData univerSheetCellData) {
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
