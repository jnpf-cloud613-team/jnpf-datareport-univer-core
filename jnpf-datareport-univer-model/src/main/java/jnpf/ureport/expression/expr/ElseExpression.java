package jnpf.ureport.expression.expr;


import jnpf.ureport.build.Context;
import jnpf.ureport.expression.data.ExpressionData;
import jnpf.ureport.model.Cell;
import lombok.Getter;
import lombok.Setter;

/**
 * @author
 * @since 1月16日
 */
@Getter
@Setter
public class ElseExpression extends BaseExpression {
    private BlockExpression expression;

    @Override
    protected ExpressionData compute(Cell cell, Cell currentCell, Context context) {
        return expression.execute(cell, currentCell, context);
    }
}
