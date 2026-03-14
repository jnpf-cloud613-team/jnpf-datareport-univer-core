package jnpf.ureport.expression.builder;


import jnpf.ureport.expression.antlr.ReportParser.UnitContext;
import jnpf.ureport.expression.expr.BaseExpression;
import jnpf.ureport.expression.expr.BooleanExpression;

/**
 * @author
 * @since 2016年12月25日
 */
public class BooleanExpressionBuilder implements ExpressionBuilder {
    @Override
    public BaseExpression build(UnitContext unitContext) {
        String text = unitContext.BOOLEAN().getText();
        return new BooleanExpression(Boolean.valueOf(text));
    }

    @Override
    public boolean support(UnitContext unitContext) {
        return unitContext.BOOLEAN() != null;
    }
}
