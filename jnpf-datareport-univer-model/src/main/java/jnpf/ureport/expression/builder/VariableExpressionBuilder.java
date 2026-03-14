package jnpf.ureport.expression.builder;


import jnpf.ureport.expression.antlr.ReportParser.UnitContext;
import jnpf.ureport.expression.expr.BaseExpression;
import jnpf.ureport.expression.expr.VariableExpression;

/**
 * @author
 * @since 2018年7月15日
 */
public class VariableExpressionBuilder implements ExpressionBuilder {
    @Override
    public BaseExpression build(UnitContext unitContext) {
        String text = unitContext.variable().Identifier().getText();
        VariableExpression varExpr = new VariableExpression(text);
        return varExpr;
    }

    @Override
    public boolean support(UnitContext unitContext) {
        return unitContext.variable() != null;
    }
}
