package jnpf.ureport.expression.expr;


import jnpf.ureport.build.Context;
import jnpf.ureport.expression.data.ExpressionData;
import jnpf.ureport.model.Cell;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author
 * @since 2018年7月13日
 */
@Getter
@Setter
public class BlockExpression extends BaseExpression {
    private List<Expression> expressionList;
    private Expression returnExpression;


    @Override
    protected ExpressionData compute(Cell cell, Cell currentCell, Context context) {
        ExpressionData data = null;
        if (expressionList != null) {
            for (Expression expr : expressionList) {
                data = expr.execute(cell, currentCell, context);
            }
        }
        if (returnExpression != null) {
            data = returnExpression.execute(cell, currentCell, context);
        }
        return data;
    }
}
