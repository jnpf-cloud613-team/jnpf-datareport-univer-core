package jnpf.ureport.expression.expr;


import jnpf.ureport.build.Context;
import jnpf.ureport.expression.data.ExpressionData;
import jnpf.ureport.expression.data.ObjectExpressionData;
import jnpf.ureport.model.Cell;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author
 * @since 1月16日
 */
@Getter
@Setter
public class IfExpression extends BaseExpression {
    private ExpressionConditionList expressionConditionList;
    private BlockExpression expression;
    private List<ElseIfExpression> elseIfExpressions;
    private ElseExpression elseExpression;

    @Override
    protected ExpressionData compute(Cell cell, Cell currentCell, Context context) {
        if (expressionConditionList != null) {
            boolean result = expressionConditionList.eval(context, cell, currentCell);
            if (result) {
                return expression.execute(cell, currentCell, context);
            }
        }
        if (elseIfExpressions != null) {
            for (ElseIfExpression elseIfExpr : elseIfExpressions) {
                if (elseIfExpr.conditionsEval(cell, currentCell, context)) {
                    return elseIfExpr.execute(cell, currentCell, context);
                }
            }
        }
        if (elseExpression != null) {
            return elseExpression.execute(cell, currentCell, context);
        }
        return new ObjectExpressionData(null);
    }

}
