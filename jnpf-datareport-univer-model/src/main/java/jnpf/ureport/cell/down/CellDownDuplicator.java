package jnpf.ureport.cell.down;

import jnpf.enums.UniverDataEnum;
import jnpf.ureport.build.Context;
import jnpf.ureport.cell.DuplicateType;
import jnpf.ureport.definition.BlankCellInfo;
import jnpf.ureport.definition.value.SimpleValue;
import jnpf.ureport.definition.value.Value;
import jnpf.ureport.model.Cell;
import jnpf.ureport.model.Row;
import jnpf.ureport.utils.DataUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;


@Getter
@Setter
public class CellDownDuplicator {
    private Cell cell;
    private int cellRowNumber;
    private DuplicateType duplicateType;
    private BlankCellInfo blankCellInfo;
    private boolean nonChild = false;

    public CellDownDuplicator(Cell cell, DuplicateType duplicateType, int cellRowNumber) {
        this.cell = cell;
        this.cellRowNumber = cellRowNumber;
        this.duplicateType = duplicateType;
    }

    public CellDownDuplicator(Cell cell, DuplicateType duplicateType, BlankCellInfo blankCellInfo, int cellRowNumber) {
        this.cell = cell;
        if (cellRowNumber == 0) {
            this.cellRowNumber = cell.getRow().getRowNumber();
        } else {
            this.cellRowNumber = cellRowNumber;
        }
        this.duplicateType = duplicateType;
        this.blankCellInfo = blankCellInfo;
    }

    public Cell duplicateChildrenCell(DownDuplicate downDuplicate, Cell leftParent, Cell originalCell, boolean parentNonChild) {
        Cell newCell = cell.newCell();
        Row newRow = downDuplicate.newRow(newCell.getRow(), cellRowNumber);
        newRow.getCells().add(newCell);
        newCell.getColumn().getCells().add(newCell);
        newCell.setRow(newRow);
        if (newCell.getLeftParentCell() == originalCell) {
            newCell.setLeftParentCell(leftParent);
            if (parentNonChild) {
                nonChild = true;
            }
        } else {
            nonChild = true;
        }
        Cell leftParentCell = newCell.getLeftParentCell();
        if (leftParentCell != null) {
            leftParentCell.addRowChild(newCell);
        }
        Cell topParentCell = newCell.getTopParentCell();
        if (topParentCell != null) {
            topParentCell.addColumnChild(newCell);
        }
        Value value = newCell.getValue();
        Context context = downDuplicate.getContext();
        String leftType = newCell.getLeftType();
        String topType = newCell.getTopType();
        DataUtils.cellList(newCell, true, true);
        if (value instanceof SimpleValue) {
            newCell.setData(value.getValue());
            newCell.setProcessed(true);
            context.addReportCell(newCell);
            if (isSimpleValue(value, leftType, topType)) {
                newCell.setData(null);
//                newCell.setCellData(new UniverSheetCellData());
            }
        } else {
            if (nonChild) {
                newCell.setValue(new SimpleValue(null));
            } else {
                context.addCell(newCell);
            }
        }
        return newCell;
    }

    public Cell duplicate(DownDuplicate downDuplicate, Cell newMainCell) {
        switch (duplicateType) {
            case Blank:
                processBlankCell(downDuplicate, newMainCell);
                break;
            case Self:
                processSelfBlankCell(downDuplicate);
                break;
            case IncreaseSpan:
                processIncreaseSpanCell(downDuplicate);
                break;
        }
        return null;
    }

    private void processBlankCell(DownDuplicate downDuplicate, Cell newMainCell) {
        Context context = downDuplicate.getContext();
        Cell newBlankCell = cell.newRowBlankCell(context, blankCellInfo, downDuplicate.getMainCell());
        if (blankCellInfo.isParent() && newMainCell.getLeftParentCell() == cell) {
            newMainCell.setLeftParentCell(newBlankCell);
        }
        Row newRow = downDuplicate.newRow(newBlankCell.getRow(), cellRowNumber);
        newRow.getCells().add(newBlankCell);
        newBlankCell.getColumn().getCells().add(newBlankCell);
        newBlankCell.setRow(newRow);
        context.addReportCell(newBlankCell);
    }

    private void processSelfBlankCell(DownDuplicate downDuplicate) {
        Cell newBlankCell = cell.newCell();
        newBlankCell.setValue(new SimpleValue(null));
        Row newRow = downDuplicate.newRow(newBlankCell.getRow(), cellRowNumber);
        newRow.getCells().add(newBlankCell);
        newBlankCell.getColumn().getCells().add(newBlankCell);
        newBlankCell.setRow(newRow);
        Cell leftParentCell = newBlankCell.getLeftParentCell();
        if (leftParentCell != null) {
            leftParentCell.addRowChild(newBlankCell);
        }
        Cell topParentCell = newBlankCell.getTopParentCell();
        if (topParentCell != null) {
            topParentCell.addColumnChild(newBlankCell);
        }
        Context context = downDuplicate.getContext();
        context.addBlankCell(newBlankCell);
    }

    private void processIncreaseSpanCell(DownDuplicate downDuplicate) {
        int rowSpan = cell.getRowSpan();
        Value value = cell.getValue();
        String leftType = cell.getLeftType();
        String topType = cell.getTopType();
        if (isSimpleValue(value, leftType, topType)) {
            return;
        }
        rowSpan += downDuplicate.getRowSize();
        if (rowSpan == 1) {
            rowSpan++;
        }
        cell.setRowSpan(rowSpan);
    }

    private boolean isSimpleValue(Value value, String leftType, String topType) {
        //判断文本类型是否要合并格子
        boolean isSimpleValue = value instanceof SimpleValue;
        boolean isLeftNone = Objects.equals(leftType, UniverDataEnum.cellNone.getName());
        boolean isTopNone = Objects.equals(topType, UniverDataEnum.cellNone.getName());
        return isSimpleValue && isLeftNone && isTopNone;
    }

}
