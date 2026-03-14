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
 * @since 12月8日
 */
@Setter
@Getter
public class CurrentValueExpressionCondition extends BaseCondition {
    private ConditionType type = ConditionType.current;
    @JsonIgnore
    private Expression rightExpression;

    @Override
    Object computeLeft(Cell cell, Cell currentCell, Object obj, Context context) {
        return obj;
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
