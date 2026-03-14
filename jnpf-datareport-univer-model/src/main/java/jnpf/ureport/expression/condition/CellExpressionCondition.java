package jnpf.ureport.expression.condition;


import jnpf.ureport.build.Context;
import jnpf.ureport.expression.data.ExpressionData;
import jnpf.ureport.expression.expr.Expression;
import jnpf.ureport.model.Cell;
import jnpf.ureport.utils.DataUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.StringJoiner;

/**
 * @author
 * @since 4月7日
 */
@Setter
@Getter
public class CellExpressionCondition extends BaseCondition {
    private ConditionType type = ConditionType.cell;
    private String cellName;
    private Expression rightExpression;

    @Override
    Object computeLeft(Cell cell, Cell currentCell, Object obj, Context context) {
        if (cellName.equals(currentCell.getName())) {
            return currentCell.getData();
        } else {
            List<Cell> cells = DataUtils.fetchTargetCells(cell, context, cellName);
            for (Cell c : cells) {
                if (c.getRow() == cell.getRow() || c.getColumn() == cell.getColumn()) {
                    return c.getData();
                }
            }
            StringJoiner joiner = new StringJoiner(",");
            for (Cell c : cells) {
                if (c.getData() != null) {
                    joiner.add(c.getData().toString());
                }
            }
            return joiner;
        }
    }

    @Override
    Object computeRight(Cell cell, Cell currentCell, Object obj, Context context) {
        ExpressionData exprData = rightExpression.execute(cell, currentCell, context);
        return extractExpressionData(exprData);
    }

    @Override
    public ConditionType getType() {
        return type;
    }

}
