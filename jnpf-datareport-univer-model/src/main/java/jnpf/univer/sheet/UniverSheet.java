package jnpf.univer.sheet;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/5/11 下午4:35
 */
@Data
public class UniverSheet implements Serializable {
    private String id;
    private String name;
    private String tabColor;
    private Integer hidden;
    private UniverSheetFreeze freeze;
    private Integer rowCount;
    private Integer columnCount;
    private Integer zoomRatio;
    private Integer scrollTop;
    private Integer scrollLeft;
    private Integer defaultColumnWidth = 88;
    private Integer defaultRowHeight = 24;
    private List<UniverSheetRange> mergeData;
    private Map<Integer, Map<Integer, UniverSheetCellData>> cellData;
    private Map<Integer, UniverSheetRowData> rowData;
    private Map<Integer, UniverSheetColumnData> columnData;
    private UniverSheetRowHeader rowHeader;
    private UniverSheetColumnHeader columnHeader;
    private Integer showGridlines;
    private List<String> selections;
    private Integer rightToLeft;
}
