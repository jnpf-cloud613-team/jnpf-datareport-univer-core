package jnpf.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.Method;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import jnpf.constant.MsgCode;
import jnpf.entity.FileParameter;
import jnpf.enums.*;
import jnpf.exception.DataException;
import jnpf.univer.chart.UniverChartModel;
import jnpf.univer.data.resource.*;
import jnpf.univer.model.UniverWorkBook;
import jnpf.univer.properties.UniverBody;
import jnpf.univer.properties.UniverBodyConfig;
import jnpf.univer.properties.UniverProperties;
import jnpf.univer.resources.UniverResource;
import jnpf.univer.resources.UniverResourceData;
import jnpf.univer.sheet.*;
import jnpf.univer.style.UniverStyle;
import jnpf.univer.style.UniverStyleBorder;
import jnpf.univer.style.UniverStyleTextDecoration;
import jnpf.univer.style.UniverStyleTextRotation;
import jnpf.util.excel.ExcelParser;
import jnpf.util.excel.UniverHSSFExcel;
import jnpf.util.excel.UniverXSSFExcel;
import jnpf.util.type.RequestType;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.*;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.Color;
import java.io.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/9/29 下午5:17
 */
@Slf4j
public class UniverExcel {

    private static Map<String, ExcelParser> excelMap = ImmutableMap.of(
            "xlsx", new UniverXSSFExcel(),
            "xls", new UniverHSSFExcel()
    );

    public static UniverWorkBook formFile(MultipartFile file) throws IOException {
        //todo 推荐使用xlsx方法
        String fileName = file.getOriginalFilename();
        String type = Files.getFileExtension(fileName);
        ExcelParser excelParser = excelMap.get(type);
        if (excelParser == null) {
            throw new DataException(MsgCode.ETD110.get());
        }
        UniverWorkBook univerWorkBook = excelParser.formFile(file.getInputStream());
        return univerWorkBook;
    }

    public static void downExcel(String snapshot, List<UniverChartModel> chartList, XSSFWorkbook workbook, List<String> sheetList) {
        Map<UniverStyle, XSSFCellStyle> styleMap = new HashMap<>();
        UniverWorkBook univerWorkBook = JsonUtil.getJsonToBean(snapshot, UniverWorkBook.class);

        Map<String, UniverSheet> univerSheetMap = univerWorkBook.getSheets();
        List<UniverResource> resources = univerWorkBook.getResources() != null ? univerWorkBook.getResources() : new ArrayList<>();
        Map<String, String> sheetMap = new HashMap<>();
        Map<XSSFCell, String> linkMap = new HashMap<>();
        Map<String, List<UniverDrawing>> sheetDrawingMap = new HashMap<>();
        Map<String, String> sheetMapReversed = univerSheetMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getName()
                ));

        for (String sheetOrder : univerWorkBook.getSheetOrder()) {
            UniverSheet univerSheet = univerSheetMap.get(sheetOrder);
            if (ObjectUtil.isNotEmpty(univerSheet)) {
                if (!sheetList.contains(sheetOrder)) {
                    continue;
                }
                List<UniverSheetRange> mergeData = univerSheet.getMergeData();
                String sheetName = univerSheet.getName();
                XSSFSheet sheet = workbook.createSheet(sheetName);
                sheetMap.put(sheetName, sheetOrder);
                sheetMapReversed.put(sheetOrder, sheetName);
                workbook.setSheetHidden(workbook.getSheetIndex(sheet), Objects.equals(univerSheet.getHidden(), 1));
                Map<Integer, UniverSheetRowData> rowData = univerSheet.getRowData();
                Map<Integer, UniverSheetColumnData> colData = univerSheet.getColumnData();
                XSSFDrawing drawing = sheet.createDrawingPatriarch();
                //冻结位置
                UniverSheetFreeze freeze = univerSheet.getFreeze() != null ? univerSheet.getFreeze() : new UniverSheetFreeze();
                sheet.createFreezePane(freeze.getStartColumn(), freeze.getStartRow());
                List<UniverDrawing> drawingList = new ArrayList<>();
                AtomicBoolean isHyperLink = new AtomicBoolean(false);
                //遍历数据
                univerSheet.getCellData().forEach((rowKey, sheetRow) -> {
                    XSSFRow row = sheet.createRow(rowKey);
                    UniverSheetRowData sheetRowData = rowData.get(rowKey);
                    if (ObjectUtil.isNotEmpty(sheetRowData)) {
                        row.setZeroHeight(Objects.equals(sheetRowData.getHd(), 1));
                        if (ObjectUtil.isNotEmpty(sheetRowData.getH())) {
                            row.setHeightInPoints(sheetRowData.getH());
                        }
                    }
                    sheetRow.forEach((colKey, sheetCol) -> {
                        UniverSheetRange range = mergeData.stream().filter(t -> Objects.equals(t.getStartRow(), rowKey) && Objects.equals(t.getStartColumn(), colKey)).findFirst().orElse(null);
                        Object value = sheetCol.getV();
                        Object formula = sheetCol.getF();
                        Object style = sheetCol.getS();
                        Object p = sheetCol.getP();
                        UniverProperties properties = p != null ? JsonUtil.getJsonToBean(p, UniverProperties.class) : null;
                        XSSFCell cell = row.createCell(colKey);
                        UniverSheetColumnData columnData = colData.get(colKey);
                        if (ObjectUtil.isNotEmpty(columnData)) {
                            if (ObjectUtil.isNotEmpty(columnData.getW())) {
                                sheet.setColumnWidth(colKey, (short) (columnData.getW() * 1.33 * 35));
                            }
                            sheet.setColumnHidden(colKey, Objects.equals(columnData.getHd(), 1));
                        }
                        boolean zxing = true;
                        if (properties != null) {
                            List<String> drawingsOrder = properties.getDrawingsOrder() != null ? properties.getDrawingsOrder() : new ArrayList<>();
                            if (!drawingsOrder.isEmpty()) {
                                zxing = false;
                                //单元格图片、图表
                                Map<String, UniverDrawing> drawings = properties.getDrawings() != null ? properties.getDrawings() : new HashMap<>();
                                for (String order : drawingsOrder) {
                                    UniverDrawing univerDrawing = drawings.get(order);
                                    if (univerDrawing != null) {
                                        UniverTransform sheetTransform = new UniverTransform();
                                        UniverOffset form = new UniverOffset();
                                        form.setRow(rowKey);
                                        form.setColumn(colKey);
                                        sheetTransform.setFrom(form);
                                        UniverOffset to = new UniverOffset();
                                        UniverSheetRange sheetRange = mergeData.stream().filter(t -> Objects.equals(t.getStartRow(), rowKey) && Objects.equals(t.getStartColumn(), colKey)).findFirst().orElse(null);
                                        int rowSpan = sheetRange != null ? sheetRange.getEndRow() - sheetRange.getStartRow() + 1 : 1;
                                        int colSpan = sheetRange != null ? sheetRange.getEndColumn() - sheetRange.getStartColumn() + 1 : 1;
                                        to.setRow(form.getRow() + rowSpan);
                                        to.setColumn(form.getColumn() + colSpan);
                                        sheetTransform.setTo(to);
                                        univerDrawing.setSheetTransform(sheetTransform);
                                        drawingList.add(univerDrawing);
                                    }
                                }
                            } else {
                                UniverBody body = properties.getBody();
                                if (body != null) {
                                    isHyperLink.set(true);
                                    cell.setCellType(CellType.STRING);
                                    cell.setCellValue(body.getDataStream());
                                    List<UniverBodyConfig> customRangesList = body.getCustomRanges() != null ? body.getCustomRanges() : new ArrayList<>();
                                    for (UniverBodyConfig customRanges : customRangesList) {
                                        UniverBodyConfig custom = customRanges.getProperties();
                                        if (custom == null) {
                                            continue;
                                        }
                                        linkMap.put(cell, custom.getUrl());
                                    }
                                }
                            }
                        }

                        Integer type = ObjectUtil.isNotEmpty(formula) ? CellDataTypeEnum.Formula.getCode() : sheetCol.getT();
                        Object dataValue = ObjectUtil.isNotEmpty(formula) ? formula : value;
                        if (zxing) {
                            if (ObjectUtil.isNotEmpty(dataValue)) {
                                CellDataTypeEnum dataType = CellDataTypeEnum.getDataType(type);
                                switch (dataType) {
                                    case Number:
                                        cell.setCellValue(Double.valueOf(dataValue.toString()));
                                        cell.setCellType(CellType.NUMERIC);
                                        break;
                                    case Boolean:
                                        cell.setCellValue(Boolean.valueOf(dataValue.toString()));
                                        cell.setCellType(CellType.BOOLEAN);
                                        break;
                                    case Formula:
                                        try {
                                            cell.setCellFormula(dataValue.toString().substring(1));
                                            cell.setCellType(CellType.FORMULA);
                                        } catch (Exception e) {
                                            cell.setCellValue(dataValue.toString());
                                            cell.setCellType(CellType.STRING);
                                        }
                                        break;
                                    default:
                                        cell.setCellValue(dataValue.toString());
                                        cell.setCellType(CellType.STRING);
                                        break;
                                }
                            }
                        }

                        if (ObjectUtil.isNotEmpty(style)) {
                            UniverStyle basicStyle = univerWorkBook.getStyles().get(style);
                            if (ObjectUtil.isEmpty(basicStyle)) {
                                basicStyle = JsonUtil.getJsonToBean(style, UniverStyle.class);
                            }
                            if (ObjectUtil.isNotEmpty(basicStyle)) {
                                UniverStyleBorder bd = basicStyle.getBd();
                                if (ObjectUtil.isNotEmpty(styleMap.get(basicStyle))) {
                                    bdAnchor(bd, rowKey, colKey, range, drawing);
                                    cell.setCellStyle(styleMap.get(basicStyle));
                                } else {
                                    XSSFCellStyle cellStyle = workbook.createCellStyle();
                                    if (ObjectUtil.isNotEmpty(basicStyle.getBg()) && ObjectUtil.isNotEmpty(basicStyle.getBg().getRgb())) {
                                        String bgGrb = basicStyle.getBg().getRgb();
                                        XSSFColor color = color(bgGrb);
                                        if (color != null) {
                                            cellStyle.setFillForegroundColor(color);
                                            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                                        }
                                    }
                                    if (ObjectUtil.isNotEmpty(bd)) {
                                        if (ObjectUtil.isNotEmpty(bd.getT()) && ObjectUtil.isNotEmpty(bd.getT().getCl())) {
                                            String bdt = bd.getT().getCl().getRgb();
                                            if (ObjectUtil.isNotEmpty(bdt)) {
                                                XSSFColor color = color(bdt);
                                                if (color != null) {
                                                    cellStyle.setTopBorderColor(color);
                                                }
                                            }
                                            StyleTypeEnum bdStyle = StyleTypeEnum.getStyle(bd.getT().getS());
                                            if (ObjectUtil.isNotEmpty(bdStyle)) {
                                                cellStyle.setBorderTop(bdStyle.getBorderStyle());
                                            }
                                        }
                                        if (ObjectUtil.isNotEmpty(bd.getL()) && ObjectUtil.isNotEmpty(bd.getL().getCl())) {
                                            String bdl = bd.getL().getCl().getRgb();
                                            if (ObjectUtil.isNotEmpty(bdl)) {
                                                XSSFColor color = color(bdl);
                                                if (color != null) {
                                                    cellStyle.setLeftBorderColor(color);
                                                }
                                            }
                                            StyleTypeEnum bdStyle = StyleTypeEnum.getStyle(bd.getL().getS());
                                            if (ObjectUtil.isNotEmpty(bdStyle)) {
                                                cellStyle.setBorderLeft(bdStyle.getBorderStyle());
                                            }
                                        }
                                        if (ObjectUtil.isNotEmpty(bd.getB()) && ObjectUtil.isNotEmpty(bd.getB().getCl())) {
                                            String bdb = bd.getB().getCl().getRgb();
                                            if (ObjectUtil.isNotEmpty(bdb)) {
                                                XSSFColor color = color(bdb);
                                                if (color != null) {
                                                    cellStyle.setBottomBorderColor(color);
                                                }
                                            }
                                            StyleTypeEnum bdStyle = StyleTypeEnum.getStyle(bd.getB().getS());
                                            if (ObjectUtil.isNotEmpty(bdStyle)) {
                                                cellStyle.setBorderBottom(bdStyle.getBorderStyle());
                                            }
                                        }
                                        if (ObjectUtil.isNotEmpty(bd.getR()) && ObjectUtil.isNotEmpty(bd.getR().getCl())) {
                                            String bdr = bd.getR().getCl().getRgb();
                                            if (ObjectUtil.isNotEmpty(bdr)) {
                                                XSSFColor color = color(bdr);
                                                if (color != null) {
                                                    cellStyle.setRightBorderColor(color);
                                                }
                                            }
                                            StyleTypeEnum bdStyle = StyleTypeEnum.getStyle(bd.getR().getS());
                                            if (ObjectUtil.isNotEmpty(bdStyle)) {
                                                cellStyle.setBorderRight(bdStyle.getBorderStyle());
                                            }
                                        }
                                        bdAnchor(bd, rowKey, colKey, range, drawing);
                                    }
                                    HorizontalAlignment alignment = ObjectUtil.isNotEmpty(basicStyle.getHt()) ? HorizontalEnum.getHorizontalValue(basicStyle.getHt()) : null;
                                    if (ObjectUtil.isNotEmpty(alignment)) {
                                        cellStyle.setAlignment(alignment);
                                    }
                                    VerticalAlignment verticalValue = ObjectUtil.isNotEmpty(basicStyle.getVt()) ? VerticalEnum.getVerticalValue(basicStyle.getVt()) : null;
                                    if (ObjectUtil.isNotEmpty(verticalValue)) {
                                        cellStyle.setVerticalAlignment(verticalValue);
                                    }
                                    if (ObjectUtil.isNotEmpty(basicStyle.getN()) && ObjectUtil.isNotEmpty(basicStyle.getN().getPattern())) {
                                        DataFormat format = workbook.createDataFormat();
                                        cellStyle.setDataFormat(format.getFormat(basicStyle.getN().getPattern()));
                                    }
                                    if (ObjectUtil.isNotEmpty(basicStyle.getTb()) && Objects.equals(basicStyle.getTb(), 3)) {
                                        cellStyle.setWrapText(true);
                                    }
                                    if (ObjectUtil.isNotEmpty(basicStyle.getTr())) {
                                        UniverStyleTextRotation tr = basicStyle.getTr();
                                        int v = tr.getV();
                                        int rotation = tr.getA();
                                        if (Objects.equals(v, 1)) {
                                            rotation = -255;
                                        }
                                        cellStyle.setRotation((short) -rotation);
                                    }
                                    XSSFFont font = workbook.createFont();
                                    if (ObjectUtil.isNotEmpty(basicStyle.getFf())) {
                                        font.setFontName(basicStyle.getFf());
                                    }
                                    if (ObjectUtil.isNotEmpty(basicStyle.getFs())) {
                                        font.setFontHeight(basicStyle.getFs());
                                    }
                                    if (ObjectUtil.isNotEmpty(basicStyle.getIt())) {
                                        font.setItalic(Objects.equals(basicStyle.getIt(), 1));
                                    }
                                    if (ObjectUtil.isNotEmpty(basicStyle.getBl())) {
                                        font.setBold(Objects.equals(basicStyle.getBl(), 1));
                                    }
                                    if (ObjectUtil.isNotEmpty(basicStyle.getCl()) && ObjectUtil.isNotEmpty(basicStyle.getCl().getRgb())) {
                                        XSSFColor color = color(basicStyle.getCl().getRgb());
                                        if (color != null) {
                                            font.setColor(color);
                                        }
                                    }
                                    if (ObjectUtil.isNotEmpty(basicStyle.getUl()) && ObjectUtil.isNotEmpty(basicStyle.getUl().getS())) {
                                        font.setUnderline(Objects.equals(basicStyle.getUl().getS(), 1) ? Font.U_SINGLE : Font.U_NONE);
                                    }
                                    if (ObjectUtil.isNotEmpty(basicStyle.getSt()) && ObjectUtil.isNotEmpty(basicStyle.getSt().getS())) {
                                        font.setStrikeout(Objects.equals(basicStyle.getSt().getS(), 1));
                                    }
                                    cellStyle.setFont(font);
                                    cell.setCellStyle(cellStyle);
                                    styleMap.put(basicStyle, cellStyle);
                                }
                            }
                        }
                    });
                });
                //合并单元格
                for (UniverSheetRange region : new HashSet<>(mergeData)) {
                    if (region.getEndRow() - region.getStartRow() == 0 && region.getEndColumn() - region.getStartColumn() == 0) {
                        continue;
                    }
                    sheet.addMergedRegion(new CellRangeAddress(region.getStartRow(), region.getEndRow(), region.getStartColumn(), region.getEndColumn()));
                }
                sheetDrawingMap.put(sheetName, drawingList);
            }
        }
        sheetMap.forEach((key, value) -> {
            XSSFSheet sheet = workbook.getSheet(key);
            if (sheet != null) {
                List<UniverDrawing> drawingList = sheetDrawingMap.get(key) != null ? sheetDrawingMap.get(key) : new ArrayList<>();
                XSSFDrawing drawing = sheet.getDrawingPatriarch();
                //图片
                drawing(value, resources, drawingList, drawing, workbook);
                //条件格式
                format(value, resources, sheet);
                //数据有效性
                dataValidation(value, resources, sheet);
                //筛选
                filter(value, resources, sheet);


            }
        });
        //定义名称
        definedName(resources, univerSheetMap, linkMap, workbook);
    }


    private static HyperlinkType getType(String url) {
        // 兼容性检查：通过地址格式二次验证
        if (url == null) return HyperlinkType.NONE;

        if (url.startsWith("#")) {
            return HyperlinkType.DOCUMENT;
        } else if (url.startsWith("http://") || url.startsWith("https://")) {
            return HyperlinkType.URL;
        } else if (url.startsWith("mailto:")) {
            return HyperlinkType.EMAIL;
        } else if (url.contains("\\") || url.contains("/")) {
            return HyperlinkType.FILE;
        }
        return HyperlinkType.NONE;
    }

    private static void bdAnchor(UniverStyleBorder bd, int rowKey, int colKey, UniverSheetRange range, XSSFDrawing drawing) {
        if (ObjectUtil.isNotEmpty(bd)) {
            if (ObjectUtil.isNotEmpty(bd.getTl_br()) && ObjectUtil.isNotEmpty(bd.getTl_br().getCl())) {
                int startRow = rowKey;
                int startCol = colKey;
                int endRow = rowKey + 1;
                int endCol = colKey + 1;
                if (range != null) {
                    endRow = range.getEndRow() + 1;
                    endCol = range.getEndColumn() + 1;
                }
                XSSFClientAnchor anchor = new XSSFClientAnchor();
                anchor.setRow1(startRow);
                anchor.setCol1(startCol);
                anchor.setRow2(endRow);
                anchor.setCol2(endCol);
                XSSFSimpleShape shape = drawing.createSimpleShape(anchor);
                StyleTypeEnum bdStyle = StyleTypeEnum.getStyle(bd.getTl_br().getS());
                if (ObjectUtil.isNotEmpty(bdStyle)) {
                    shape.setShapeType(bdStyle.getBorderStyle().getCode());
                }
                String bdr = bd.getTl_br().getCl().getRgb();
                if (ObjectUtil.isNotEmpty(bdr)) {
                    XSSFColor color = color(bdr);
                    if (color != null) {
                        shape.setLineStyleColor(color.getRGB()[0], color.getRGB()[1], color.getRGB()[2]);
                    }
                }
                shape.setShapeType(ShapeTypes.LINE);
            }
            if (ObjectUtil.isNotEmpty(bd.getTl_mr()) && ObjectUtil.isNotEmpty(bd.getTl_mr().getCl())) {
                if (range != null) {
                    int startRow = rowKey;
                    int startCol = colKey;
                    int endRow = (rowKey + range.getEndRow() + 1) / 2;
                    int endCol = range.getEndColumn() + 1;
                    XSSFClientAnchor anchor = new XSSFClientAnchor();
                    anchor.setRow1(startRow);
                    anchor.setCol1(startCol);
                    anchor.setRow2(endRow);
                    anchor.setCol2(endCol);
                    XSSFSimpleShape shape = drawing.createSimpleShape(anchor);
                    StyleTypeEnum bdStyle = StyleTypeEnum.getStyle(bd.getTl_mr().getS());
                    if (ObjectUtil.isNotEmpty(bdStyle)) {
                        shape.setShapeType(bdStyle.getBorderStyle().getCode());
                    }
                    String bdr = bd.getTl_mr().getCl().getRgb();
                    if (ObjectUtil.isNotEmpty(bdr)) {
                        XSSFColor color = color(bdr);
                        if (color != null) {
                            shape.setLineStyleColor(color.getRGB()[0], color.getRGB()[1], color.getRGB()[2]);
                        }
                    }
                    shape.setShapeType(ShapeTypes.LINE);
                }
            }
            if (ObjectUtil.isNotEmpty(bd.getTl_bc()) && ObjectUtil.isNotEmpty(bd.getTl_bc().getCl())) {
                if (range != null) {
                    int startRow = rowKey;
                    int startCol = colKey;
                    int endRow = range.getEndRow() + 1;
                    int endCol = (colKey + range.getEndColumn() + 1) / 2;
                    XSSFClientAnchor anchor = new XSSFClientAnchor();
                    anchor.setRow1(startRow);
                    anchor.setCol1(startCol);
                    anchor.setRow2(endRow);
                    anchor.setCol2(endCol);
                    XSSFSimpleShape shape = drawing.createSimpleShape(anchor);
                    StyleTypeEnum bdStyle = StyleTypeEnum.getStyle(bd.getTl_bc().getS());
                    if (ObjectUtil.isNotEmpty(bdStyle)) {
                        shape.setShapeType(bdStyle.getBorderStyle().getCode());
                    }
                    String bdr = bd.getTl_bc().getCl().getRgb();
                    if (ObjectUtil.isNotEmpty(bdr)) {
                        XSSFColor color = color(bdr);
                        if (color != null) {
                            shape.setLineStyleColor(color.getRGB()[0], color.getRGB()[1], color.getRGB()[2]);
                        }
                    }
                    shape.setShapeType(ShapeTypes.LINE);
                }
            }
            if (ObjectUtil.isNotEmpty(bd.getBl_tr()) && ObjectUtil.isNotEmpty(bd.getBl_tr().getCl())) {
                int startRow = rowKey;
                int startCol = colKey;
                int endRow = rowKey + 1;
                int endCol = colKey + 1;
                if (range != null) {
                    endRow = range.getEndRow() + 1;
                    endCol = range.getEndColumn() + 1;
                }
                XSSFClientAnchor anchor = new XSSFClientAnchor();
                anchor.setRow1(startRow);
                anchor.setCol1(startCol);
                anchor.setRow2(endRow);
                anchor.setCol2(endCol);
                XSSFSimpleShape shape = drawing.createSimpleShape(anchor);
                StyleTypeEnum bdStyle = StyleTypeEnum.getStyle(bd.getBl_tr().getS());
                if (ObjectUtil.isNotEmpty(bdStyle)) {
                    shape.setShapeType(bdStyle.getBorderStyle().getCode());
                }
                String bdr = bd.getBl_tr().getCl().getRgb();
                if (ObjectUtil.isNotEmpty(bdr)) {
                    XSSFColor color = color(bdr);
                    if (color != null) {
                        shape.setLineStyleColor(color.getRGB()[0], color.getRGB()[1], color.getRGB()[2]);
                    }
                }
                shape.setShapeType(ShapeTypes.LINE_INV);
            }
            if (ObjectUtil.isNotEmpty(bd.getBc_tr()) && ObjectUtil.isNotEmpty(bd.getBc_tr().getCl())) {
                if (range != null) {
                    int startRow = rowKey;
                    int startCol = range.getStartColumn();
                    int endRow = (rowKey + range.getEndRow() + 1) / 2;
                    int endCol = range.getEndColumn() + 1;
                    XSSFClientAnchor anchor = new XSSFClientAnchor();
                    anchor.setRow1(startRow);
                    anchor.setCol1(startCol);
                    anchor.setRow2(endRow);
                    anchor.setCol2(endCol);
                    XSSFSimpleShape shape = drawing.createSimpleShape(anchor);
                    StyleTypeEnum bdStyle = StyleTypeEnum.getStyle(bd.getBc_tr().getS());
                    if (ObjectUtil.isNotEmpty(bdStyle)) {
                        shape.setShapeType(bdStyle.getBorderStyle().getCode());
                    }
                    String bdr = bd.getBc_tr().getCl().getRgb();
                    if (ObjectUtil.isNotEmpty(bdr)) {
                        XSSFColor color = color(bdr);
                        if (color != null) {
                            shape.setLineStyleColor(color.getRGB()[0], color.getRGB()[1], color.getRGB()[2]);
                        }
                    }
                    shape.setShapeType(ShapeTypes.LINE_INV);
                }
            }
            if (ObjectUtil.isNotEmpty(bd.getMl_tr()) && ObjectUtil.isNotEmpty(bd.getMl_tr().getCl())) {
                if (range != null) {
                    int startRow = rowKey;
                    int startCol = (colKey + range.getEndColumn() + 1) / 2;
                    int endRow = range.getEndRow() + 1;
                    int endCol = range.getEndColumn() + 1;
                    XSSFClientAnchor anchor = new XSSFClientAnchor();
                    anchor.setRow1(startRow);
                    anchor.setCol1(startCol);
                    anchor.setRow2(endRow);
                    anchor.setCol2(endCol);
                    XSSFSimpleShape shape = drawing.createSimpleShape(anchor);
                    StyleTypeEnum bdStyle = StyleTypeEnum.getStyle(bd.getMl_tr().getS());
                    if (ObjectUtil.isNotEmpty(bdStyle)) {
                        shape.setShapeType(bdStyle.getBorderStyle().getCode());
                    }
                    String bdr = bd.getMl_tr().getCl().getRgb();
                    if (ObjectUtil.isNotEmpty(bdr)) {
                        XSSFColor color = color(bdr);
                        if (color != null) {
                            shape.setLineStyleColor(color.getRGB()[0], color.getRGB()[1], color.getRGB()[2]);
                        }
                    }
                    shape.setShapeType(ShapeTypes.LINE_INV);
                }
            }
        }
    }

    private static XSSFColor color(String rgbCole) {
        rgbCole = rgbCole.replaceAll("\\s*", "").toUpperCase();
        byte[] rgb = null;
        try {
            if (rgbCole.startsWith("#")) {
                Color color = new Color(Integer.parseInt(rgbCole.substring(1), 16));
                rgb = new byte[]{(byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue()};
            } else if (rgbCole.startsWith("RGB(") && rgbCole.endsWith(")")) {
                rgbCole = rgbCole.substring(4, rgbCole.length() - 1);
                String[] rgbArray = rgbCole.split(",");
                if (rgbArray.length >= 3) {
                    int red = Integer.parseInt(rgbArray[0]);
                    int green = Integer.parseInt(rgbArray[1]);
                    int blue = Integer.parseInt(rgbArray[2]);
                    rgb = new byte[]{(byte) red, (byte) green, (byte) blue};
                }
            } else if (rgbCole.startsWith("RGBA(") && rgbCole.endsWith(")")) {
                rgbCole = rgbCole.substring(5, rgbCole.length() - 1);
                String[] rgbArray = rgbCole.split(",");
                if (rgbArray.length >= 3) {
                    int red = Integer.parseInt(rgbArray[0]);
                    int green = Integer.parseInt(rgbArray[1]);
                    int blue = Integer.parseInt(rgbArray[2]);
                    rgb = new byte[]{(byte) red, (byte) green, (byte) blue};
                }
            }
        } catch (Exception e) {

        }
        XSSFColor xssfColor = rgb == null ? null : new XSSFColor(rgb);
        return xssfColor;
    }

    /**
     * 图片
     */
    private static void drawing(String sheetOrder, List<UniverResource> resources, List<UniverDrawing> drawingList, XSSFDrawing drawing, XSSFWorkbook workbook) {
        UniverResource univerResource = resources.stream().filter(t -> ResourceEnum.SHEET_DRAWING_PLUGIN.name().equals(t.getName())).findFirst().orElse(null);
        Map<String, UniverResourceData> drawingMap = new HashMap<>();
        if (ObjectUtil.isNotEmpty(univerResource) && ObjectUtil.isNotEmpty(univerResource.getData())) {
            Map<String, Object> data = JsonUtil.stringToMap(univerResource.getData());
            data.forEach((key, value) -> {
                drawingMap.put(key, JsonUtil.getJsonToBean(value, UniverResourceData.class));
            });
        }
        UniverResourceData resourceData = drawingMap.get(sheetOrder);
        if (ObjectUtil.isNotEmpty(resourceData)) {
            List<String> orderList = resourceData.getOrder();
            Map<String, UniverDrawing> data = resourceData.getData();
            for (String order : orderList) {
                UniverDrawing univerDrawing = data.get(order);
                if (univerDrawing != null) {
                    drawingList.add(univerDrawing);
                }
            }
        }
        //导出图片
        for (UniverDrawing univerDrawing : drawingList) {
            try {
                String source = univerDrawing.getSource();
                String imageType = univerDrawing.getImageSourceType();
                byte[] bytes = null;
                if (Objects.equals(ImageEnum.BASE64.name(), imageType)) {
                    String regex = "data:image/\\w+;base64,";
                    String base64Img = source;
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(source);
                    if (matcher.find()) {
                        base64Img = source.replace(matcher.group(), "");
                    }
                    bytes = Base64.decode(base64Img);
                } else {
                    if (source.startsWith(RequestType.HTTP)) {
                        HttpRequest request = HttpRequest.of(source).method(Method.GET);
                        bytes = request.execute().bodyBytes();
                    } else {
                        String[] split = source.split("=");
                        String fileNameAll = DesUtil.aesDecode(split[split.length - 1]);
                        String[] fileData = fileNameAll.split("#");
                        String fileName = fileData.length > 1 ? fileData[1] : "";
                        String type = fileData.length > 2 ? fileData[2] : "";
                        String typePath = FilePathUtil.getFilePath(type.toLowerCase());
                        bytes = FileUploadUtils.downloadFile(new FileParameter(typePath, fileName));
                    }
                }
                if (bytes != null && bytes.length > 0) {
                    UniverTransform sheetTransform = univerDrawing.getSheetTransform();
                    UniverOffset from = sheetTransform.getFrom();
                    UniverOffset to = sheetTransform.getTo();
                    //图片导出
                    XSSFClientAnchor anchor = new XSSFClientAnchor();
                    if (from.getColumn().equals(to.getColumn())) {
                        to.setColumn(from.getColumn() + 5);
                    }
                    anchor.setRow1(from.getRow());
                    anchor.setCol1(from.getColumn());
                    anchor.setRow2(to.getRow());
                    anchor.setCol2(to.getColumn());
                    anchor.setAnchorType(ClientAnchor.AnchorType.DONT_MOVE_DO_RESIZE);
                    int imageIndex = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_JPEG);
                    drawing.createPicture(anchor, imageIndex);
                }
            } catch (Exception e) {
                log.error("图片导出失败:{}", e.getMessage());
            }
        }
    }

    /**
     * 条件格式
     */
    private static void format(String sheetOrder, List<UniverResource> resources, XSSFSheet sheet) {
        StylesTable styles = sheet.getWorkbook().getStylesSource();
        UniverResource univerResource = resources.stream().filter(t -> ResourceEnum.SHEET_CONDITIONAL_FORMATTING_PLUGIN.name().equals(t.getName())).findFirst().orElse(null);
        Map<String, List<UniverResourceData>> foramtMap = new HashMap<>();
        if (ObjectUtil.isNotEmpty(univerResource) && ObjectUtil.isNotEmpty(univerResource.getData())) {
            Map<String, Object> data = JsonUtil.stringToMap(univerResource.getData());
            data.forEach((key, value) -> {
                foramtMap.put(key, JsonUtil.getJsonToList(value, UniverResourceData.class));
            });
        }
        List<UniverResourceData> data = foramtMap.get(sheetOrder) != null ? foramtMap.get(sheetOrder) : new ArrayList<>();
        CTWorksheet ctWorksheet = sheet.getCTWorksheet();
        List<CTConditionalFormatting> conditionalList = new ArrayList<>();
        for (UniverResourceData resourceData : data) {
            UniverRule rule = resourceData.getRule();
            Boolean stopIfTrue = resourceData.getStopIfTrue();
            List<UniverSheetRange> ranges = resourceData.getRanges();
            if (ObjectUtil.isNotEmpty(rule)) {
                List<CTCfRule> ruleList = new ArrayList<>();
                List<UniverConfig> configList = new ArrayList<>();
                Object ruleConfig = rule.getConfig();
                if (ObjectUtil.isNotEmpty(ruleConfig)) {
                    if (ruleConfig instanceof List) {
                        configList.addAll(JsonUtil.getJsonToList(ruleConfig, UniverConfig.class));
                    } else if (ruleConfig instanceof Map) {
                        configList.add(JsonUtil.getJsonToBean(ruleConfig, UniverConfig.class));
                    }
                }
                List<String> regions = new ArrayList<>();
                for (UniverSheetRange range : ranges) {
                    regions.add(new CellRangeAddress(range.getStartRow(), range.getEndRow(), range.getStartColumn(), range.getEndColumn()).formatAsString());
                }
                String type = rule.getType();
                String subType = rule.getSubType();
                Boolean showValue = rule.getIsShowValue();
                String operator = rule.getOperator();
                FormatTypeEnum formatTypeEnum = FormatTypeEnum.getFormat(type);
                CTCfRule cfRule = CTCfRule.Factory.newInstance();
                cfRule.setStopIfTrue(stopIfTrue);
                STCfType.Enum typeEnum = STCfType.Enum.forString(type);
                cfRule.setType(typeEnum);
                switch (formatTypeEnum) {
                    case colorScale:
                        CTColorScale scale = cfRule.addNewColorScale();
                        for (UniverConfig univerConfig : configList) {
                            UniverValue univerValue = univerConfig.getValue();
                            if (univerValue == null) {
                                continue;
                            }
                            String valueType = univerValue.getType();
                            String value = univerValue.getValue() != null ? univerValue.getValue().toString() : "";
                            if (Objects.equals(SubTypeEnum.expression.getCode(), valueType)) {
                                value = value.replace("=", "");
                            }
                            CTCfvo ctCfvo = scale.addNewCfvo();
                            ctCfvo.setType(STCfvoType.Enum.forString(valueType));
                            ctCfvo.setVal(value);
                            XSSFColor color = color(univerConfig.getColor());
                            if (color != null) {
                                CTColor ctColor = scale.addNewColor();
                                ctColor.setRgb(color.getRGB());
                            }
                        }
                        ruleList.add(cfRule);
                        break;
                    case iconSet:
                        CTIconSet icons = cfRule.addNewIconSet();
                        icons.setShowValue(showValue);
                        for (int i = configList.size() - 1; i >= 0; i--) {
                            UniverConfig univerConfig = configList.get(i);
                            UniverValue value = univerConfig.getValue();
                            if (value == null) {
                                continue;
                            }
                            String iconType = value.getType();
                            String icon = univerConfig.getIconType();
                            String valueOperator = univerConfig.getOperator();
                            String iconValue = value.getValue() != null ? value.getValue().toString() : "";
                            if (Objects.equals(SubTypeEnum.expression.getCode(), iconType)) {
                                iconValue = iconValue.replace("=", "");
                            }
                            CTCfvo ctCfvo = icons.addNewCfvo();
                            ctCfvo.setVal(iconValue);
                            ctCfvo.setGte(Objects.equals(valueOperator, OperatorEnum.greaterThanOrEqual.name()));
                            ctCfvo.setType(STCfvoType.Enum.forString(iconType));
                            icons.setIconSet(STIconSetType.Enum.forString(icon));
                        }
                        boolean reverse = configList.size() > 0 && !Objects.equals(configList.get(0).getIconId(), "0");
                        icons.setReverse(reverse);
                        ruleList.add(cfRule);
                        break;
                    case dataBar:
                        CTDataBar bar = cfRule.addNewDataBar();
                        bar.setShowValue(showValue);
                        for (UniverConfig config : configList) {
                            UniverValue min = config.getMin();
                            UniverValue max = config.getMax();
                            if (min != null && max != null) {
                                String minType = min.getType();
                                String minValue = min.getValue() != null ? min.getValue().toString() : "";
                                if (Objects.equals(SubTypeEnum.expression.getCode(), minType)) {
                                    minValue = minValue.replace("=", "");
                                }
                                String maxType = max.getType();
                                String maxValue = max.getValue() != null ? max.getValue().toString() : "";
                                if (Objects.equals(SubTypeEnum.expression.getCode(), maxType)) {
                                    maxValue = maxValue.replace("=", "");
                                }
                                List<String> dataBarType = ImmutableList.of(OperatorEnum.min.name(), OperatorEnum.max.name());
                                for (int i = 0; i < dataBarType.size(); i++) {
                                    String barType = i == 0 ? minType : maxType;
                                    String barValue = i == 0 ? minValue : maxValue;
                                    CTCfvo ctCfvo = bar.addNewCfvo();
                                    ctCfvo.setType(STCfvoType.Enum.forString(barType));
                                    ctCfvo.setVal(barValue);

                                    XSSFColor positiveColor = color(config.getPositiveColor());
                                    if (positiveColor != null) {
                                        CTColor ctColor = bar.addNewColor();
                                        ctColor.setRgb(positiveColor.getRGB());
                                    }

                                    XSSFColor nativeColor = color(config.getNativeColor());
                                    if (nativeColor == null) {
                                        CTColor ctColor = bar.addNewColor();
                                        ctColor.setRgb(nativeColor.getRGB());
                                    }
                                }
                            }
                        }
                        ruleList.add(cfRule);
                        break;
                    default:
                        List<String> cell = ImmutableList.of(SubTypeEnum.cellIs.getCode(), SubTypeEnum.equal.getCode(), SubTypeEnum.notEqual.getCode());
                        boolean isNumber = cell.contains(subType) || cell.contains(operator);
                        //平均值
                        boolean isAverage = Objects.equals(SubTypeEnum.aboveAverage.getCode(), subType);
                        //TOP
                        boolean isRank = Objects.equals(SubTypeEnum.top10.getCode(), subType);
                        //公式
                        boolean isFormula = Objects.equals(SubTypeEnum.expression.getCode(), subType);
                        //日期
                        boolean isTime = Objects.equals(SubTypeEnum.timePeriod.getCode(), subType);
                        String ruleType = operator;
                        if (isNumber) {
                            ruleType = SubTypeEnum.cellIs.getType();
                        } else if (isAverage) {
                            ruleType = SubTypeEnum.aboveAverage.getType();
                        } else if (isRank) {
                            ruleType = SubTypeEnum.top10.getType();
                        } else if (isFormula) {
                            ruleType = SubTypeEnum.expression.getType();
                        } else if (isTime) {
                            ruleType = SubTypeEnum.timePeriod.getType();
                        }
                        if (StringUtil.isEmpty(ruleType)) {
                            ruleType = subType;
                        }
                        Object value = rule.getValue();
                        String data1 = null;
                        String data2 = null;
                        String text = null;
                        Long rank = null;
                        Boolean bottom = null;
                        Boolean percent = null;
                        if (value != null) {
                            if (isNumber) {
                                if (value instanceof List) {
                                    List<Object> valueList = (List<Object>) value;
                                    for (int i = 0; i < valueList.size(); i++) {
                                        Object numberValu = valueList.get(i);
                                        if (numberValu != null) {
                                            if (i == 0) {
                                                data1 = valueList.get(i).toString();
                                            } else {
                                                data2 = valueList.get(i).toString();
                                            }
                                        }
                                    }
                                } else {
                                    data1 = value.toString();
                                }
                            } else if (isRank) {
                                rank = Long.valueOf(value.toString());
                                bottom = rule.getIsBottom();
                                percent = rule.getIsPercent();
                            } else if (isFormula) {
                                data1 = value.toString().replace("=", "");
                            } else {
                                text = value.toString();
                            }
                        }
                        cfRule.setType(STCfType.Enum.forString(ruleType));
                        if (data1 != null) {
                            cfRule.addFormula(data1);
                        }
                        if (data2 != null) {
                            cfRule.addFormula(data2);
                        }
                        if (operator != null) {
                            STConditionalFormattingOperator.Enum anOperator = STConditionalFormattingOperator.Enum.forString(operator);
                            if (anOperator != null) {
                                cfRule.setOperator(anOperator);
                            }
                            STTimePeriod.Enum timePeriod = STTimePeriod.Enum.forString(operator);
                            if (timePeriod != null) {
                                cfRule.setTimePeriod(timePeriod);
                            }
                        }
                        if (text != null) {
                            cfRule.setText(text);
                        }
                        if (rank != null) {
                            cfRule.setRank(rank);
                        }
                        if (bottom != null) {
                            cfRule.setBottom(bottom);
                        }
                        if (percent != null) {
                            cfRule.setPercent(percent);
                        }
                        if (isAverage) {
                            cfRule.setAboveAverage(Objects.equals(OperatorEnum.greaterThan.name(), operator));
                        }
                        UniverStyle style = rule.getStyle();
                        if (style != null) {
                            CTDxf ctDxf = CTDxf.Factory.newInstance();
                            XSSFColor bgColor = color(style.getBg().getRgb() != null ? style.getBg().getRgb() : "");
                            if (bgColor != null) {
                                CTFill ctFill = CTFill.Factory.newInstance();
                                CTPatternFill patternFill = CTPatternFill.Factory.newInstance();
                                patternFill.setBgColor(bgColor.getCTColor());
                                ctFill.setPatternFill(patternFill);
                                ctDxf.setFill(ctFill);
                            }

                            CTFont ctFont = CTFont.Factory.newInstance();
                            XSSFColor fontColor = color(style.getCl().getRgb() != null ? style.getCl().getRgb() : "");
                            if (fontColor != null) {
                                CTColor ctColor = fontColor.getCTColor();
                                ctFont.setColorArray(new CTColor[]{ctColor});
                            }
                            CTUnderlineProperty underline = CTUnderlineProperty.Factory.newInstance();
                            underline.setVal(style.getUl() != null && Objects.equals(style.getUl().getS(), 1) ? STUnderlineValues.SINGLE : STUnderlineValues.NONE);
                            ctFont.setUArray(new CTUnderlineProperty[]{underline});

                            CTBooleanProperty italic = CTBooleanProperty.Factory.newInstance();
                            italic.setVal(Objects.equals(style.getIt(), 1));
                            ctFont.setIArray(new CTBooleanProperty[]{italic});

                            CTBooleanProperty bold = CTBooleanProperty.Factory.newInstance();
                            bold.setVal(Objects.equals(style.getBl(), 1));
                            ctFont.setBArray(new CTBooleanProperty[]{bold});

                            UniverStyleTextDecoration st = style.getSt();
                            CTBooleanProperty strike = CTBooleanProperty.Factory.newInstance();
                            strike.setVal(st != null && Objects.equals(st.getS(), 1));
                            ctFont.setStrikeArray(new CTBooleanProperty[]{strike});
                            ctDxf.setFont(ctFont);
                            int dxfId = styles.putDxf(ctDxf);
                            cfRule.setDxfId(dxfId - 1);
                        }
                        ruleList.add(cfRule);
                        break;
                }
                CTConditionalFormatting conditionalFormatting = ctWorksheet.addNewConditionalFormatting();
                conditionalFormatting.setCfRuleArray(ruleList.toArray(new CTCfRule[ruleList.size()]));
                conditionalFormatting.setSqref(regions);
                conditionalList.add(conditionalFormatting);
            }
        }
        ctWorksheet.setConditionalFormattingArray(conditionalList.toArray(new CTConditionalFormatting[conditionalList.size()]));
    }

    /**
     * 数据有效性
     */
    private static void dataValidation(String sheetOrder, List<UniverResource> resources, XSSFSheet sheet) {
        UniverResource univerResource = resources.stream().filter(t -> ResourceEnum.SHEET_DATA_VALIDATION_PLUGIN.name().equals(t.getName())).findFirst().orElse(null);
        Map<String, List<UniverResourceData>> dataValidationMap = new HashMap<>();
        if (ObjectUtil.isNotEmpty(univerResource) && ObjectUtil.isNotEmpty(univerResource.getData())) {
            Map<String, Object> data = JsonUtil.stringToMap(univerResource.getData());
            data.forEach((key, value) -> {
                dataValidationMap.put(key, JsonUtil.getJsonToList(value, UniverResourceData.class));
            });
        }
        XSSFDataValidationHelper dataValidation = new XSSFDataValidationHelper(sheet);
        List<UniverResourceData> data = dataValidationMap.get(sheetOrder) != null ? dataValidationMap.get(sheetOrder) : new ArrayList<>();
        for (UniverResourceData resourceData : data) {
            CellRangeAddressList addressList = new CellRangeAddressList();
            for (UniverSheetRange range : resourceData.getRanges()) {
                addressList.addCellRangeAddress(new CellRangeAddress(range.getStartRow(), range.getEndRow(), range.getStartColumn(), range.getEndColumn()));
            }
            String formula1 = resourceData.getFormula1() != null ? resourceData.getFormula1() : "";
            String formula2 = resourceData.getFormula2() != null ? resourceData.getFormula2() : "";
            ValidationType validationType = ValidationType.getValidationType(resourceData.getType());
            String value1 = Objects.equals(validationType, ValidationType.checkbox) ? formula1 + "," + formula2 : formula1;
            String value2 = formula2;
            operatorTypeEnum operator = operatorTypeEnum.getOperator(resourceData.getOperator());
            XSSFDataValidationConstraint constraint = new XSSFDataValidationConstraint(validationType.getValidationType(), operator.getOperator(), value1, value2);
            constraint.setExplicitListValues(value1.split(","));
            DataValidation validation = dataValidation.createValidation(constraint, addressList);
            validation.setErrorStyle(resourceData.getErrorStyle() != null ? 0 : 1);
            validation.setEmptyCellAllowed(resourceData.getAllowBlank() != null ? resourceData.getAllowBlank() : true);
            if (resourceData.getError() != null) {
                validation.createErrorBox(resourceData.getError(), "");
            }
            if (resourceData.getRenderMode() != null) {
                validation.setSuppressDropDownArrow(!Objects.equals(resourceData.getRenderMode(), 0));
            }
            validation.setShowErrorBox(resourceData.getShowErrorMessage() != null ? resourceData.getShowErrorMessage() : false);
            sheet.addValidationData(validation);
        }
    }

    /**
     * 筛选
     */
    private static void filter(String sheetOrder, List<UniverResource> resources, XSSFSheet sheet) {
        UniverResource univerResource = resources.stream().filter(t -> ResourceEnum.SHEET_FILTER_PLUGIN.name().equals(t.getName())).findFirst().orElse(null);
        Map<String, UniverResourceData> filterMap = new HashMap<>();
        if (ObjectUtil.isNotEmpty(univerResource) && ObjectUtil.isNotEmpty(univerResource.getData())) {
            Map<String, Object> data = JsonUtil.stringToMap(univerResource.getData());
            data.forEach((key, value) -> {
                filterMap.put(key, JsonUtil.getJsonToBean(value, UniverResourceData.class));
            });
        }
        UniverResourceData data = filterMap.get(sheetOrder) != null ? filterMap.get(sheetOrder) : new UniverResourceData();
        UniverSheetRange ref = data.getRef();
        if (ref != null) {
            sheet.setAutoFilter(new CellRangeAddress(ref.getStartRow(), ref.getEndRow(), ref.getStartColumn(), ref.getEndColumn()));
            List<CTFilterColumn> filterColumns = new ArrayList<>();
            CTAutoFilter autoFilter = sheet.getCTWorksheet().getAutoFilter();
            for (UniverFilters filters : data.getFilterColumns()) {
                Long colId = filters.getColId();
                UniverCustomFilters customFilters = filters.getFilters();
                //poi方法
                CTFilterColumn column = autoFilter.addNewFilterColumn();
                column.setColId(colId);
                CTCustomFilters ctCustomFilters = column.addNewCustomFilters();
                if (null != filters.getCustomFilters()) {
                    customFilters = filters.getCustomFilters();
                    for (UniverCustomFilters customFilter : customFilters.getCustomFilters()) {
                        CTCustomFilter ctCustomFilter = ctCustomFilters.addNewCustomFilter();
                        String val = customFilter.getVal();
                        if (val != null) {
                            ctCustomFilter.setVal(val);
                        }
                        String operator = customFilter.getOperator();
                        if (operator != null) {
                            ctCustomFilter.setOperator(STFilterOperator.Enum.forString(operator));
                        }
                        List<String> stringList = customFilter.getFilters();
                        if (CollectionUtil.isNotEmpty(stringList)) {
                            for (String string : stringList) {
                                CTCustomFilter addNewCustomFilter = ctCustomFilters.addNewCustomFilter();
                                addNewCustomFilter.setVal(string);
                                if (operator != null) {
                                    addNewCustomFilter.setOperator(STFilterOperator.Enum.forString(operator));
                                }
                            }
                        }
                        List<UniverCustomFilters> customFiltersList = customFilters.getCustomFilters();
                        if (CollectionUtil.isNotEmpty(customFiltersList)) {
                            for (UniverCustomFilters univerCustomFilters : customFiltersList) {
                                CTCustomFilter addNewCustomFilter = ctCustomFilters.addNewCustomFilter();
                                if (null != univerCustomFilters.getVal()) {
                                    addNewCustomFilter.setVal(univerCustomFilters.getVal());
                                }
                                if (operator != null) {
                                    addNewCustomFilter.setOperator(STFilterOperator.Enum.forString(operator));
                                }
                                if (null != univerCustomFilters.getOperator()) {
                                    addNewCustomFilter.setOperator(STFilterOperator.Enum.forString(univerCustomFilters.getOperator()));
                                }
                            }
                        }
                    }

                } else {
                    for (String customFilter : customFilters.getFilters()) {
                        String val = customFilters.getVal();
                        String operator = customFilters.getOperator();
                        CTCustomFilter addNewCustomFilter = ctCustomFilters.addNewCustomFilter();
                        addNewCustomFilter.setVal(customFilter);
                        if (operator != null) {
                            addNewCustomFilter.setOperator(STFilterOperator.Enum.forString(operator));
                        }
                    }
                }
                if (BeanUtil.isNotEmpty(customFilters) && null != customFilters.getAnd()) {
                    ctCustomFilters.setAnd(Objects.equals(customFilters.getAnd(), 1));
                }

                filterColumns.add(column);

            }
            //筛选隐藏
            List<Integer> cachedFilteredOut = data.getCachedFilteredOut();
            for (Integer rowIndex : cachedFilteredOut) {
                XSSFRow row = sheet.getRow(rowIndex);
                row.setZeroHeight(true);
            }
            autoFilter.setFilterColumnArray(filterColumns.toArray(new CTFilterColumn[filterColumns.size()]));
        }
    }

    /**
     * 名称管理器
     */
    private static void definedName(List<UniverResource> resources, Map<String, UniverSheet> sheetMap, Map<XSSFCell, String> linkMap, XSSFWorkbook workbook) {
        UniverResource univerResource = resources.stream().filter(t -> ResourceEnum.SHEET_DEFINED_NAME_PLUGIN.name().equals(t.getName())).findFirst().orElse(null);
        Map<String, UniverResourceData> definedNameMap = new HashMap<>();
        if (ObjectUtil.isNotEmpty(univerResource) && ObjectUtil.isNotEmpty(univerResource.getData())) {
            Map<String, Object> data = JsonUtil.stringToMap(univerResource.getData());
            data.forEach((key, value) -> {
                definedNameMap.put(key, JsonUtil.getJsonToBean(value, UniverResourceData.class));
            });
        }
        Map<String, Integer> sheetName = new HashMap<>();
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            XSSFSheet sheet = workbook.getSheetAt(i);
            sheetName.put(sheet.getSheetName(), i);
        }
        definedNameMap.forEach((key, value) -> {
            String localSheetId = value.getLocalSheetId();
            Integer sheetIndex = -1;
            if (!Objects.equals("AllDefaultWorkbook", localSheetId)) {
                UniverSheet univerSheet = sheetMap.get(localSheetId);
                sheetIndex = sheetName.get(univerSheet.getName());
            }
            if (sheetIndex != null) {
                XSSFName name = workbook.createName();
                name.setSheetIndex(sheetIndex);
                String formulaValue = value.getFormulaOrRefString();
                name.setNameName(value.getName());
                name.setRefersToFormula(formulaValue.startsWith("=") ? formulaValue.substring(1) : formulaValue);
                name.setComment(value.getComment());
            }
        });

        linkMap.forEach((key, value) -> {
            XSSFCreationHelper createHelper = workbook.getCreationHelper();
            String gid = "#gid=";
            String rangeId = "#rangeid=";
            boolean isGid = value.startsWith(gid);
            boolean isRangeId = value.startsWith(rangeId);
            XSSFHyperlink link = createHelper.createHyperlink(!isGid && !isRangeId ? HyperlinkType.URL : HyperlinkType.DOCUMENT);
            if (isGid) {
                value = value.replace(gid, "");
                String[] split = value.split("&range=");
                String sheetId = split[0];
                String cellName = split.length > 1 ? split[1] : "A1";
                UniverSheet univerSheet = sheetMap.get(sheetId);
                if (univerSheet != null) {
                    link.setAddress(univerSheet.getName() + "!" + cellName);
                }
            } else if (isRangeId) {
                value = value.replace(rangeId, "");
                UniverResourceData definedName = definedNameMap.get(value);
                if (definedName != null) {
                    String name = definedName.getName();
                    link.setAddress(name);
                }
            } else {
                link.setAddress(value);
            }
            Font hyperlinkFont = workbook.createFont();
//            XSSFCellStyle cellStyle = key.getCellStyle();
            XSSFCellStyle cellStyle = workbook.createCellStyle();
            hyperlinkFont.setUnderline(Font.U_SINGLE);
            hyperlinkFont.setColor(IndexedColors.BLUE.getIndex());
            cellStyle.setFont(hyperlinkFont);
            key.setCellStyle(cellStyle);
            key.setHyperlink(link);
        });
    }

}
