package jnpf.ureport.cell.right;

import jnpf.enums.UniverDataEnum;
import jnpf.ureport.build.Context;
import jnpf.ureport.cell.DuplicateType;
import jnpf.ureport.definition.BlankCellInfo;
import jnpf.ureport.definition.value.SimpleValue;
import jnpf.ureport.definition.value.Value;
import jnpf.ureport.model.Cell;
import jnpf.ureport.model.Column;
import jnpf.ureport.utils.DataUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * @author
 * @since 2016年11月7日
 */
@Getter
@Setter
public class CellRightDuplicator {
    private Cell cell;
    private int cellColNumber;
    private DuplicateType duplicateType;
    private BlankCellInfo blankCellInfo;
    private boolean nonChild = false;

    public CellRightDuplicator(Cell cell, DuplicateType duplicateType, int cellColNumber) {
        this.cell = cell;
        this.duplicateType = duplicateType;
        this.cellColNumber = cellColNumber;
    }

    public CellRightDuplicator(Cell cell, DuplicateType duplicateType, BlankCellInfo blankCellInfo, int cellColNumber) {
        this.cell = cell;
        this.duplicateType = duplicateType;
        this.blankCellInfo = blankCellInfo;
        if (cellColNumber > 0) {
            this.cellColNumber = cellColNumber;
        } else {
            this.cellColNumber = cell.getColumn().getColumnNumber();
        }
    }


    public Cell duplicateChildrenCell(RightDuplicate rightDuplicate, Cell topParent, Cell originalCell, boolean parentNonChild) {
        Cell newCell = cell.newCell();
        Column newCol = rightDuplicate.newColumn(newCell.getColumn(), cellColNumber);
        newCol.getCells().add(newCell);
        newCell.getRow().getCells().add(newCell);
        newCell.setColumn(newCol);
        if (newCell.getTopParentCell() == originalCell) {
            newCell.setTopParentCell(topParent);
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
        Context context = rightDuplicate.getContext();
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

    public Cell duplicate(RightDuplicate rightDuplicate, Cell newMainCell) {
        switch (duplicateType) {
            case Blank:
                processBlankCell(rightDuplicate, newMainCell);
                break;
            case Self:
                processSelfBlankCell(rightDuplicate);
                break;
            case IncreaseSpan:
                processIncreaseSpanCell(rightDuplicate);
                break;
        }
        return null;
    }

    private void processBlankCell(RightDuplicate rightDuplicate, Cell newMainCell) {
        Context context = rightDuplicate.getContext();
        Cell newBlankCell = cell.newColumnBlankCell(context, blankCellInfo, rightDuplicate.getMainCell());
        if (blankCellInfo.isParent() && newMainCell.getTopParentCell() == cell) {
            newMainCell.setTopParentCell(newBlankCell);
        }
        Column col = rightDuplicate.newColumn(newBlankCell.getColumn(), cellColNumber);
        col.getCells().add(newBlankCell);
        newBlankCell.getRow().getCells().add(newBlankCell);
        newBlankCell.setColumn(col);
        context.addReportCell(newBlankCell);
    }

    private void processSelfBlankCell(RightDuplicate rightDuplicate) {
        Cell newBlankCell = cell.newCell();
        newBlankCell.setValue(new SimpleValue(null));
        Column newCol = rightDuplicate.newColumn(newBlankCell.getColumn(), cellColNumber);
        newCol.getCells().add(newBlankCell);
        newBlankCell.getRow().getCells().add(newBlankCell);
        newBlankCell.setColumn(newCol);
        Cell leftParentCell = newBlankCell.getLeftParentCell();
        if (leftParentCell != null) {
            leftParentCell.addRowChild(newBlankCell);
        }
        Cell topParentCell = newBlankCell.getTopParentCell();
        if (topParentCell != null) {
            topParentCell.addColumnChild(newBlankCell);
        }
        Context context = rightDuplicate.getContext();
        context.addBlankCell(newBlankCell);
    }

    private void processIncreaseSpanCell(RightDuplicate rightDuplicate) {
        int colSpan = cell.getColSpan();
        Value value = cell.getValue();
        String leftType = cell.getLeftType();
        String topType = cell.getTopType();
        if (isSimpleValue(value, leftType, topType)) {
            return;
        }
        colSpan += rightDuplicate.getColumnSize();
        if (colSpan == 1) {
            colSpan++;
        }
        cell.setColSpan(colSpan);
    }

    private boolean isSimpleValue(Value value, String leftType, String topType) {
        //判断文本类型是否要合并格子
        boolean isSimpleValue = value instanceof SimpleValue;
        boolean isLeftNone = Objects.equals(leftType, UniverDataEnum.cellNone.getName());
        boolean isTopNone = Objects.equals(topType, UniverDataEnum.cellNone.getName());
        return isSimpleValue && isLeftNone && isTopNone;
    }


}
