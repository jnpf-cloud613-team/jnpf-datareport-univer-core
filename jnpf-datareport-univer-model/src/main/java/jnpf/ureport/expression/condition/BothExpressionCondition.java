package jnpf.ureport.expression.condition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jnpf.ureport.build.Context;
import jnpf.ureport.expression.data.ExpressionData;
import jnpf.ureport.expression.expr.Expression;
import jnpf.ureport.model.Cell;
import lombok.Getter;
import lombok.Setter;

/**
 * @author
 * @since 2016年11月22日
 */
@Setter
@Getter
public class BothExpressionCondition extends BaseCondition {
    private ConditionType type = ConditionType.expression;
    @JsonIgnore
    private Expression leftExpression;
    @JsonIgnore
    private Expression rightExpression;

    @Override
    Object computeLeft(Cell cell, Cell currentCell, Object obj, Context context) {
        ExpressionData exprData = leftExpression.execute(cell, currentCell, context);
        return extractExpressionData(exprData);
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
