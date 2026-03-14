package jnpf.util.excel;

import jnpf.univer.resources.UniverResourceData;
import jnpf.univer.style.UniverStyle;
import org.apache.poi.ss.usermodel.*;

import java.util.*;

public abstract class ImportExcelParser {
    /**样式**/
    public abstract UniverStyle getUniverStyle(CellStyle cellStyle, Workbook workbook);
    /**筛选**/
    public abstract Map<String, UniverResourceData> filter(Sheet sheet, String sheetId, String unitId);
    /**数据管理**/
    public abstract Map<String, List<UniverResourceData>> dataValidation(Sheet sheet, String sheetId, String unitId);
    /**条件**/
    public abstract Map<String, List<UniverResourceData>> format(Sheet sheet, String sheetId, String unitId);
    /**图片**/
    public abstract Map<String, UniverResourceData> drawing(Sheet sheet, String sheetId, String unitId);

}