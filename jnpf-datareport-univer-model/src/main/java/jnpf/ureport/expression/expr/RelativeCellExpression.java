package jnpf.ureport.expression.expr;


import jnpf.ureport.build.Context;
import jnpf.ureport.expression.data.ExpressionData;
import jnpf.ureport.expression.data.ObjectExpressionData;
import jnpf.ureport.expression.data.ObjectListExpressionData;
import jnpf.ureport.model.Cell;
import jnpf.ureport.utils.DataUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @since 1月21日
 */
public class RelativeCellExpression extends CellExpression {
    public RelativeCellExpression(String cellName) {
        super(cellName);
    }

    @Override
    public boolean supportPaging() {
        return false;
    }

    @Override
    protected ExpressionData compute(Cell cell, Cell currentCell, Context context) {
        List<Cell> targetCells = DataUtils.fetchTargetCells(currentCell, context, cellName);
        int size = targetCells.size();
        if (size == 0) {
            return new ObjectListExpressionData(new ArrayList<>());
        } else if (size == 1) {
            return new ObjectExpressionData(targetCells.get(0).getData());
        } else {
            Cell targetCell = null;
            for (Cell c : targetCells) {
                if (c.getRow() == currentCell.getRow() || c.getColumn() == currentCell.getColumn()) {
                    targetCell = c;
                    break;
                }
            }
            if (targetCell != null) {
                return new ObjectExpressionData(targetCell.getData());
            } else {
                List<Object> list = new ArrayList<>();
                for (Cell c : targetCells) {
                    list.add(c.getData());
                }
                return new ObjectListExpressionData(list);
            }
        }
    }
}
