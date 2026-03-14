package jnpf.ureport.expression.expr;


import jnpf.ureport.build.Context;
import jnpf.ureport.expression.data.ExpressionData;
import jnpf.ureport.expression.data.ObjectExpressionData;
import jnpf.ureport.model.Cell;

/**
 * @author
 * @since 7月11日
 */
public class CellValueExpression extends BaseExpression {

    @Override
    protected ExpressionData compute(Cell cell, Cell currentCell, Context context) {
        return new ObjectExpressionData(cell.getData());
    }
}
