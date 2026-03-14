package jnpf.ureport.model;

import jnpf.univer.sheet.UniverSheetCellData;
import jnpf.univer.zxing.UniverZxingModel;
import jnpf.ureport.Range;
import jnpf.ureport.build.BindData;
import jnpf.ureport.build.Context;
import jnpf.ureport.definition.BlankCellInfo;
import jnpf.ureport.definition.Expand;
import jnpf.ureport.definition.value.SimpleValue;
import jnpf.ureport.definition.value.Value;
import jnpf.univer.sheet.UniverSheetColumnData;
import jnpf.univer.sheet.UniverSheetRowData;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.util.CellReference;

import java.util.*;

@Getter
@Setter
public class Cell {
    private String name;

    private int rowNumber;
    private int columnNumber;

    private int rowSpan;
    private int colSpan;

    private Range duplicateRange;

    /**
     * 当前单元格计算后的实际值
     */
    private String type;
    private Object data;
    private String leftGroupId;
    private String topGroupId;

    private UniverZxingModel zxing;
    private CellReference reference;

    private StyleModel styleModel;

    private UniverSheetRowData sheetRowData;
    private UniverSheetColumnData sheetColumnData;

    private UniverSheetCellData cellData;

    private Value value;
    private Row row;
    private Column column;
    private Expand expand;
    private boolean processed;
    private boolean blankCell;
    private List<Map<String, Object>> bindData;
    private List<BindData> dataList;

    /**
     * 当前单元格左父格
     */
    private String leftType;
    private Cell leftParentCell;
    private List<Cell> leftParentList = new ArrayList<>();
    /**
     * 当前单元格上父格
     */
    private String topType;
    private Cell topParentCell;
    private List<Cell> topParentList = new ArrayList<>();

    /**
     * 当前单元格所在行所有子格
     */
    private Map<String, List<Cell>> rowChildrenCellsMap = new HashMap<>();
    /**
     * 当前单元格所在列所有子格
     */
    private Map<String, List<Cell>> columnChildrenCellsMap = new HashMap<>();


    private List<String> increaseSpanCellNames;
    private Map<String, BlankCellInfo> newBlankCellsMap;
    private List<String> newCellNames;


    public Cell newCell() {
        Cell cell = new Cell();
        cell.setColumn(column);
        cell.setRowNumber(rowNumber);
        cell.setColumnNumber(columnNumber);
        cell.setRow(row);
        cell.setLeftParentCell(leftParentCell);
        cell.setTopParentCell(topParentCell);
        cell.setValue(value);
        cell.setLeftGroupId(leftGroupId);
        cell.setTopGroupId(topGroupId);
        cell.setDataList(dataList);
        cell.setRowSpan(rowSpan);
        cell.setColSpan(colSpan);
        cell.setExpand(expand);
        cell.setName(name);
        cell.setDuplicateRange(duplicateRange);
        cell.setCellData(cellData);
        cell.setStyleModel(styleModel);
        cell.setNewBlankCellsMap(newBlankCellsMap);
        cell.setNewCellNames(newCellNames);
        cell.setIncreaseSpanCellNames(increaseSpanCellNames);
        cell.setLeftType(leftType);
        cell.setTopType(topType);
        cell.setType(type);
        cell.setZxing(zxing);
        return cell;
    }

    public void addRowChild(Cell child) {
        String name = child.getName();
        List<Cell> cells = rowChildrenCellsMap.get(name);
        if (cells == null) {
            cells = new ArrayList<>();
            rowChildrenCellsMap.put(name, cells);
        }
        if (!cells.contains(child)) {
            cells.add(child);
        }
        if (leftParentCell != null) {
            leftParentCell.addRowChild(child);
        }
    }

    public void addColumnChild(Cell child) {
        String name = child.getName();
        List<Cell> cells = columnChildrenCellsMap.get(name);
        if (cells == null) {
            cells = new ArrayList<>();
            columnChildrenCellsMap.put(name, cells);
        }
        if (!cells.contains(child)) {
            cells.add(child);
        }
        if (topParentCell != null) {
            topParentCell.addColumnChild(child);
        }
    }

    public Cell newColumnBlankCell(Context context, BlankCellInfo blankCellInfo, Cell mainCell) {
        Cell blankCell = newCell();
        blankCell.setBlankCell(true);
        blankCell.setValue(new SimpleValue(null));
        blankCell.setExpand(Expand.None);
        blankCell.setBindData(null);

        int offset = blankCellInfo.getOffset();
        int mainColNumber = mainCell.getColumn().getColumnNumber();
        if (offset == 0) {
            blankCell.setColumn(mainCell.getColumn());
        } else {
            int colNumber = mainColNumber + offset;
            Column col = context.getColumn(colNumber);
            blankCell.setColumn(col);
        }
        blankCell.setColSpan(blankCellInfo.getSpan());
        return blankCell;
    }

    public Cell newRowBlankCell(Context context, BlankCellInfo blankCellInfo, Cell mainCell) {
        Cell blankCell = newCell();
        blankCell.setBlankCell(true);
        blankCell.setValue(new SimpleValue(null));
        blankCell.setExpand(Expand.None);
        blankCell.setBindData(null);
        if (blankCellInfo != null) {
            int offset = blankCellInfo.getOffset();
            int mainRowNumber = mainCell.getRow().getRowNumber();
            if (offset == 0) {
                blankCell.setRow(mainCell.getRow());
            } else {
                int rowNumber = mainRowNumber + offset;
                Row row = context.getRow(rowNumber);
                blankCell.setRow(row);
            }
            blankCell.setRowSpan(blankCellInfo.getSpan());
        }
        return blankCell;
    }

}
