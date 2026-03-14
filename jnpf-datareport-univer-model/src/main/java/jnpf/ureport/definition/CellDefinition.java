package jnpf.ureport.definition;

import jnpf.univer.sheet.UniverSheetCellData;
import jnpf.univer.zxing.UniverZxingModel;
import jnpf.ureport.Range;
import jnpf.ureport.definition.value.Value;
import jnpf.ureport.model.Cell;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jnpf.univer.sheet.UniverSheetColumnData;
import jnpf.univer.sheet.UniverSheetRowData;
import jnpf.ureport.model.StyleModel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Getter
@Setter
public class CellDefinition {
    private int rowNumber;
    private int columnNumber;
    private int rowSpan;
    private int colSpan;
    private String name;
    private Value value;
    private String type;
    private UniverZxingModel zxing;
    private StyleModel styleModel;
    private UniverSheetRowData sheetRowData;
    private UniverSheetColumnData sheetColumnData;

    private Expand expand = Expand.None;

    @JsonIgnore
    private Range duplicateRange;
    @JsonIgnore
    private List<String> increaseSpanCellNames = new ArrayList<>();
    @JsonIgnore
    private Map<String, BlankCellInfo> newBlankCellsMap = new HashMap<>();
    @JsonIgnore
    private List<String> newCellNames = new ArrayList<>();

    private UniverSheetCellData cellData;


    /**
     * 左父格类型
     */
    private String leftType;
    /**
     * 上父格类型
     */
    private String topType;
    /**
     * 当前单元格左父格名
     */
    private String leftParentCellName;
    /**
     * 当前单元格上父格名
     */
    private String topParentCellName;
    /**
     * 当前单元格左父格
     */
    @JsonIgnore
    private CellDefinition leftParentCell;
    /**
     * 当前单元格上父格
     */
    @JsonIgnore
    private CellDefinition topParentCell;
    /**
     * 当前单无格所在行的所有子格
     */
    @JsonIgnore
    private List<CellDefinition> rowChildrenCells = new ArrayList<>();
    /**
     * 当前单无格所在列的所有子格
     */
    @JsonIgnore
    private List<CellDefinition> columnChildrenCells = new ArrayList<>();


    protected Cell newCell() {
        Cell cell = new Cell();
        cell.setValue(value);
        cell.setName(name);
        cell.setRowSpan(rowSpan);
        cell.setColSpan(colSpan);
        cell.setExpand(expand);
        cell.setDuplicateRange(duplicateRange);
        cell.setRowNumber(rowNumber);
        cell.setColumnNumber(columnNumber);
        cell.setStyleModel(styleModel);
        cell.setSheetRowData(sheetRowData);
        cell.setSheetColumnData(sheetColumnData);
        cell.setCellData(cellData);
        cell.setNewBlankCellsMap(newBlankCellsMap);
        cell.setIncreaseSpanCellNames(increaseSpanCellNames);
        cell.setNewCellNames(newCellNames);
        cell.setLeftType(leftType);
        cell.setTopType(topType);
        cell.setType(type);
        cell.setZxing(zxing);
        return cell;
    }

}
