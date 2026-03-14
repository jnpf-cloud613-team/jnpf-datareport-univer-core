package jnpf.ureport.expression.builder;


import jnpf.ureport.expression.antlr.ReportParser.UnitContext;
import jnpf.ureport.expression.expr.BaseExpression;
import jnpf.ureport.expression.expr.StringExpression;

/**
 * @author
 * @since 2016年12月23日
 */
public class StringExpressionBuilder implements ExpressionBuilder {
    @Override
    public BaseExpression build(UnitContext unitContext) {
        String text = unitContext.STRING().getText();
        text = text.substring(1, text.length() - 1);
        StringExpression stringExpr = new StringExpression(text);
        return stringExpr;
    }

    @Override
    public boolean support(UnitContext unitContext) {
        return unitContext.STRING() != null;
    }
}
