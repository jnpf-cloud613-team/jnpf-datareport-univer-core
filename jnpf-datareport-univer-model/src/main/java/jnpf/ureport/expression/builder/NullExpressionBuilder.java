package jnpf.ureport.expression.builder;

import jnpf.ureport.expression.antlr.ReportParser.UnitContext;
import jnpf.ureport.expression.expr.BaseExpression;
import jnpf.ureport.expression.expr.NullExpression;


/**
 * @author
 * @since 2016年12月25日
 */
public class NullExpressionBuilder implements ExpressionBuilder {
    @Override
    public BaseExpression build(UnitContext unitContext) {
        return new NullExpression();
    }

    @Override
    public boolean support(UnitContext unitContext) {
        return unitContext.NULL() != null;
    }
}
