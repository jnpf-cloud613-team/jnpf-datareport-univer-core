package jnpf.ureport.expression.expr;

import jnpf.ureport.build.Context;
import jnpf.ureport.expression.condition.Condition;
import jnpf.ureport.expression.data.ExpressionData;
import jnpf.ureport.expression.data.NoneExpressionData;
import jnpf.ureport.expression.data.ObjectExpressionData;
import jnpf.ureport.expression.data.ObjectListExpressionData;
import jnpf.ureport.model.Cell;
import jnpf.ureport.utils.DataUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @since 1月1日
 */
public class CellConditionExpression extends CellExpression {
    protected Condition condition;

    public CellConditionExpression(String cellName, Condition condition) {
        super(cellName);
        this.condition = condition;
    }

    @Override
    protected ExpressionData compute(Cell cell, Cell currentCell, Context context) {
        List<Cell> targetCells = filterCells(cell, context, condition, DataUtils.fetchTargetCells(cell, context, cellName));
        if (targetCells.size() > 1) {
            List<Object> list = new ArrayList<>();
            for (Cell targetCell : targetCells) {
                list.add(targetCell.getData());
            }
            return new ObjectListExpressionData(list);
        } else if (targetCells.size() == 1) {
            return new ObjectExpressionData(targetCells.get(0).getData());
        } else {
            return new NoneExpressionData();
        }
    }

}
