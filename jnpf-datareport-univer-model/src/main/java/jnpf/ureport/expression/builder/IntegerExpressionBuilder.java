package jnpf.ureport.expression.builder;


import jnpf.ureport.expression.antlr.ReportParser.UnitContext;
import jnpf.ureport.expression.expr.BaseExpression;
import jnpf.ureport.expression.expr.IntegerExpression;

/**
 * @author
 * @since 2016年12月24日
 */
public class IntegerExpressionBuilder implements ExpressionBuilder {
    @Override
    public BaseExpression build(UnitContext unitContext) {
        Integer value = null;
        if (unitContext.INTEGER() != null) {
            value = Integer.valueOf(unitContext.INTEGER().getText());
        }
        return new IntegerExpression(value);
    }

    @Override
    public boolean support(UnitContext unitContext) {
        return unitContext.INTEGER() != null;
    }

}
