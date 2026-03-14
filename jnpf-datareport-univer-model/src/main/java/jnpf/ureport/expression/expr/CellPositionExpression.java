package jnpf.ureport.expression.expr;


import jnpf.ureport.build.Context;
import jnpf.ureport.expression.data.ExpressionData;
import jnpf.ureport.expression.data.ObjectExpressionData;
import jnpf.ureport.model.Cell;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @since 4月5日
 */
public class CellPositionExpression extends CellExpression {

    public CellPositionExpression(String cellName) {
        super(cellName);
    }

    @Override
    public boolean supportPaging() {
        return false;
    }

    @Override
    protected ExpressionData compute(Cell cell, Cell currentCell, Context context) {
        //todo
        List<Cell> cellList = new ArrayList<>();
        Cell targetCell = leftCell(cell, cellName);
        if (targetCell == null) {
            targetCell = topCell(cell, cellName);
        }
        if (targetCell != null && targetCell.getRowChildrenCellsMap().get(cellName) != null) {
            cellList.addAll(targetCell.getRowChildrenCellsMap().get(cellName));
        } else {
            if (context.getReport().getCellsMap().get(cellName) != null) {
                cellList.addAll(context.getReport().getCellsMap().get(cellName));
            }
        }
        int index = -1;
        if (cellList.isEmpty()) {
            return new ObjectExpressionData(index);
        }
        int rowNumber = cell.getRow().getRowNumber();
        for (int i = 0; i < cellList.size(); i++) {
            Cell target = cellList.get(i);
            if (target.getRow() == cell.getRow()) {
                index = i;
                break;
            }
            int rowSpan = target.getRowSpan();
            if (rowSpan > 0) {
                int targetRowStart = target.getRow().getRowNumber();
                int targetRowEnd = targetRowStart + rowSpan - 1;
                if (targetRowStart <= rowNumber && targetRowEnd >= rowNumber) {
                    index = i;
                    break;
                }
            }
        }
        if (index > -1) {
            index++;
            return new ObjectExpressionData(index);
        }
        int colNumber = cell.getColumn().getColumnNumber();
        for (int i = 0; i < cellList.size(); i++) {
            Cell target = cellList.get(i);
            if (target.getColumn() == cell.getColumn()) {
                index = i;
                break;
            }
            int colSpan = target.getColSpan();
            if (colSpan > 0) {
                int targetColStart = target.getColumn().getColumnNumber();
                int targetColEnd = targetColStart + colSpan - 1;
                if (targetColStart <= colNumber && targetColEnd >= colNumber) {
                    index = i;
                    break;
                }
            }
        }
        index++;
        return new ObjectExpressionData(index);
    }

    private Cell leftCell(Cell cell, String cellName) {
        Cell leftParentCell = cell.getLeftParentCell();
        if (leftParentCell == null) {
            return null;
        }
        List<String> cellNameList = leftParentCell.getNewCellNames() != null ? leftParentCell.getNewCellNames() : new ArrayList<>();
        if (cellNameList.contains(cellName)) {
            return leftParentCell;
        }
        return leftCell(leftParentCell, cellName);
    }

    private Cell topCell(Cell cell, String cellName) {
        Cell topParentCell = cell.getTopParentCell();
        if (topParentCell == null) {
            return null;
        }
        List<String> cellNameList = topParentCell.getNewCellNames() != null ? topParentCell.getNewCellNames() : new ArrayList<>();
        if (cellNameList.contains(cellName)) {
            return topParentCell;
        }
        return topCell(topParentCell, cellName);
    }

}
