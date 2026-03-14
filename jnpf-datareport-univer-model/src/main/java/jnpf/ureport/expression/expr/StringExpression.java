package jnpf.ureport.expression.expr;


import jnpf.ureport.build.Context;
import jnpf.ureport.expression.data.ExpressionData;
import jnpf.ureport.expression.data.ObjectExpressionData;
import jnpf.ureport.model.Cell;

/**
 * @author
 * @since 2016年12月23日
 */
public class StringExpression extends BaseExpression {
    private String text;

    public StringExpression(String text) {
        this.text = text;
    }

    @Override
    protected ExpressionData compute(Cell cell, Cell currentCell, Context context) {
        return new ObjectExpressionData(text);
    }
}
