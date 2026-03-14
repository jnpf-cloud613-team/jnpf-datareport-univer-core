package jnpf.ureport.expression.expr;


import jnpf.ureport.build.Context;
import jnpf.ureport.expression.data.ExpressionData;
import jnpf.ureport.expression.data.ObjectExpressionData;
import jnpf.ureport.model.Cell;

import java.math.BigDecimal;

/**
 * @author
 * @since 2016年12月23日
 */
public class NumberExpression extends BaseExpression {
    private BigDecimal value;

    public NumberExpression(BigDecimal value) {
        this.value = value;
    }

    @Override
    public ExpressionData compute(Cell cell, Cell currentCell, Context context) {
        return new ObjectExpressionData(value.floatValue());
    }
}
