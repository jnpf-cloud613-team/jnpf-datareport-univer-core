package jnpf.ureport.expression.expr;


import jnpf.ureport.build.Context;
import jnpf.ureport.expression.data.ExpressionData;
import jnpf.ureport.expression.data.ObjectExpressionData;
import jnpf.ureport.model.Cell;

/**
 * @author
 * @since 2018年7月15日
 */
public class VariableExpression extends BaseExpression {
    private String text;

    public VariableExpression(String text) {
        this.text = text;
    }

    @Override
    protected ExpressionData compute(Cell cell, Cell currentCell, Context context) {
        Object obj = context.getVariable(text);
        return new ObjectExpressionData(obj);
    }
}
