package jnpf.util.excel;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import jnpf.enums.*;
import jnpf.univer.data.resource.*;
import jnpf.univer.resources.UniverResourceData;
import jnpf.univer.sheet.UniverSheetRange;
import jnpf.univer.style.*;
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.hssf.record.NameRecord;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;

import java.awt.image.BufferedImage;
import java.util.*;

public class ImportHssfExcel extends ImportExcelParser {

    @Override
    public UniverStyle getUniverStyle(CellStyle cellStyle, Workbook workbook) {
        UniverStyle univerStyle = new UniverStyle();
        if (cellStyle instanceof HSSFCellStyle && workbook instanceof HSSFWorkbook) {
            HSSFCellStyle hssfCellStyle = (HSSFCellStyle) cellStyle;
            HSSFWorkbook hssfWorkbook = (HSSFWorkbook) workbook;
            univerStyle.setHt(HorizontalEnum.getHorizontalCode(hssfCellStyle.getAlignment()));
            univerStyle.setVt(VerticalEnum.getVerticalCode(hssfCellStyle.getVerticalAlignment()));
            HSSFFont font = hssfCellStyle.getFont(workbook);
            if (null != font) {
                univerStyle.setFf(font.getFontName());
                univerStyle.setFs((int) font.getFontHeightInPoints());
                univerStyle.setIt(font.getItalic() ? 1 : 0);
                univerStyle.setBl(font.getBold() ? 1 : 0);
                HSSFColor color = font.getHSSFColor(hssfWorkbook);
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
            if (null != hssfCellStyle.getDataFormatString()) {
                UniverStylePattern pattern = new UniverStylePattern();
                pattern.setPattern(hssfCellStyle.getDataFormatString());
                univerStyle.setN(pattern);
            }
            univerStyle.setBd(getUniverStyleBorder(hssfCellStyle, hssfWorkbook));

            HSSFColor bgColor = hssfCellStyle.getFillForegroundColorColor();
            FillPatternType pattern = hssfCellStyle.getFillPattern();
            if (null != bgColor && Objects.equals(pattern, FillPatternType.SOLID_FOREGROUND)) {
                UniverStyleColor background = new UniverStyleColor();
                background.setRgb(rgb(bgColor.getTriplet()));
                univerStyle.setBg(background);
            }

            UniverStyleTextRotation univerStyleTextRotation = new UniverStyleTextRotation();
            univerStyleTextRotation.setA(hssfCellStyle.getRotation());
            univerStyle.setTr(univerStyleTextRotation);
        }
        return univerStyle;
    }

    @Override
    public Map<String, UniverResourceData> filter(Sheet sheet, String sheetId, String unitId) {
        Map<String, UniverResourceData> filterMap = new HashMap<>();
        //todo 只有获取筛选的格子，没有提供筛选的内容
        if (sheet instanceof HSSFSheet) {
            HSSFSheet hssfSheet = (HSSFSheet) sheet;
            HSSFWorkbook workbook = hssfSheet.getWorkbook();
            int sheetIndex = workbook.getSheetIndex(sheet) + 1;
            UniverResourceData data = new UniverResourceData();
            InternalWorkbook internalWorkbook = workbook.getWorkbook();
            if (internalWorkbook != null) {
                NameRecord name = internalWorkbook.getSpecificBuiltinRecord((byte) 13, sheetIndex);
                if (name != null) {
                    Ptg[] nameDefinition = name.getNameDefinition();
                    String formulaString = HSSFFormulaParser.toFormulaString(hssfSheet.getWorkbook(), nameDefinition);
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
        return filterMap;
    }

    @Override
    public Map<String, List<UniverResourceData>> dataValidation(Sheet sheet, String sheetId, String unitId) {
        Map<String, List<UniverResourceData>> dataValidationMap = new HashMap<>();
        if (sheet instanceof HSSFSheet) {
            HSSFSheet hssfSheet = (HSSFSheet) sheet;
            List<UniverResourceData> dataValidationList = new ArrayList<>();
            List<HSSFDataValidation> dataValidations = hssfSheet.getDataValidations();
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
        return dataValidationMap;
    }

    @Override
    public Map<String, List<UniverResourceData>> format(Sheet sheet, String sheetId, String unitId) {
        Map<String, List<UniverResourceData>> formattingMap = new HashMap<>();
        //todo 提供色阶、数据条、色阶方法，其他方法获取的数据有问题
        List<UniverResourceData> formatList = new ArrayList<>();
        if (sheet instanceof HSSFSheet) {
            HSSFSheet hssfSheet = (HSSFSheet) sheet;
            HSSFSheetConditionalFormatting formatting = hssfSheet.getSheetConditionalFormatting();
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
                            byte[] rgb = dataBar.getColor().getRGB();
                            config.setPositiveColor(rgb(rgb));
                            config.setNativeColor(rgb(rgb));
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
                            HSSFExtendedColor[] colorList = colorScale.getColors();
                            Map<Integer, String> colorColor = new HashMap<>();
                            for (int k = 0; k < colorList.length; k++) {
                                HSSFExtendedColor ctColor = colorList[k];
                                colorColor.put(k, rgb(ctColor.getRGB()));
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
        return formattingMap;
    }

    @Override
    public Map<String, UniverResourceData> drawing(Sheet sheet, String sheetId, String unitId) {
        Map<String, UniverResourceData> drawingMap = new HashMap<>();
        if (sheet instanceof HSSFSheet) {
            HSSFSheet hssfSheet = (HSSFSheet) sheet;
            List<String> order = new ArrayList<>();
            Map<String, UniverDrawing> data = new HashMap<>();
            HSSFPatriarch drawing = hssfSheet.getDrawingPatriarch();
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
                            sheetTransform.setFrom(univerFrom);
                            UniverOffset univerTo = new UniverOffset();
                            univerTo.setRow(anchor.getRow2());
                            univerTo.setColumn((int) anchor.getCol2());
                            sheetTransform.setTo(univerTo);
                            UniverDrawing univerDrawing = new UniverDrawing();
                            univerDrawing.setSheetTransform(sheetTransform);
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
        return drawingMap;
    }


    private String rgb(short[] color) {
        String rgb = null;
        if (null != color && color.length == 3) {
            rgb = String.format("#%02X%02X%02X", color[0], color[1], color[2]);
        }
        return rgb;
    }

    private String rgb(byte[] color) {
        String rgb = null;
        if (ObjectUtil.isNotEmpty(color)) {
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
}
