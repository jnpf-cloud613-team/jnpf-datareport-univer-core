package jnpf.univer.resources;

import jnpf.univer.data.resource.*;
import jnpf.univer.sheet.UniverSheetRange;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/5/22 下午2:10
 */
@Data
public class UniverResourceData {
    //SHEET_DATA_VALIDATION
    private String uid;
    private String operator;
    private String formula1;
    private String formula2;
    private String error;
    private Integer renderMode;
    private Boolean allowBlank;
    private Integer errorStyle;
    private Boolean showErrorMessage;

    //SHEET_NUMFMT_PLUGIN
    private Map<String, Map<Integer, Map<Integer, UniverNum>>> model;
    private List<UniverRef> refModel;
    private List<UniverNum> numModel;

    //SHEET_CONDITIONAL_FORMATTING_PLUGIN
    private String cfId;
    private UniverRule rule;
    private Boolean stopIfTrue;

    //SHEET_CONDITIONAL_FORMATTING_PLUGIN、SHEET_DATA_VALIDATION、SHEET_RANGE_PROTECTION
    private List<UniverSheetRange> ranges;

    //SHEET_NUMFMT_PLUGIN、SHEET_DATA_VALIDATION
    private String type;

    //SHEET_DEFINED_NAME_PLUGIN
    private String id;
    private String name;
    private String formulaOrRefString;
    private String comment;
    private String localSheetId;

    //SHEET_RANGE_PROTECTION
    private String permissionId;
    private String unitType;
    private String unitId;
    private String subUnitId;

    //SHEET_DRAWING_PLUGIN
    private List<String> order;
    private Map<String, UniverDrawing> data;

    //SHEET_FILTER_PLUGIN
    private UniverSheetRange ref;
    private List<UniverFilters> filterColumns;
    private List<Integer> cachedFilteredOut;

}
