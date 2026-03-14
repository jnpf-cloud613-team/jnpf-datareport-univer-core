package jnpf.ureport.expression.expr;


import jnpf.ureport.build.Context;
import jnpf.ureport.expression.data.ExpressionData;
import jnpf.ureport.expression.data.NoneExpressionData;
import jnpf.ureport.model.Cell;

/**
 * @author
 * @since 2016年12月23日
 */
public class NullExpression extends BaseExpression {

    @Override
    public ExpressionData compute(Cell cell, Cell currentCell, Context context) {
        return new NoneExpressionData();
    }
}
