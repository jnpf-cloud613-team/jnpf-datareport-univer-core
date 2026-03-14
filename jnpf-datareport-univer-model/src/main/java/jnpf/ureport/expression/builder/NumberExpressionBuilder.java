package jnpf.ureport.expression.builder;

import jnpf.ureport.expression.antlr.ReportParser.UnitContext;
import jnpf.ureport.expression.expr.BaseExpression;
import jnpf.ureport.expression.expr.NumberExpression;
import jnpf.ureport.utils.DataUtils;

import java.math.BigDecimal;

/**
 * @author
 * @since 2016年12月25日
 */
public class NumberExpressionBuilder implements ExpressionBuilder {
    @Override
    public BaseExpression build(UnitContext unitContext) {
        BigDecimal number = DataUtils.toBigDecimal(unitContext.NUMBER().getText());
        return new NumberExpression(number);
    }

    @Override
    public boolean support(UnitContext unitContext) {
        return unitContext.NUMBER() != null;
    }
}
