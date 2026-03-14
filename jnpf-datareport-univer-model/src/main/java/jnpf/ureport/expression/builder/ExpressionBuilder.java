package jnpf.ureport.expression.builder;


import jnpf.ureport.expression.antlr.ReportParser.UnitContext;
import jnpf.ureport.expression.expr.BaseExpression;

/**
 * @author
 * @since 2016年12月23日
 */
public interface ExpressionBuilder {
    BaseExpression build(UnitContext unitContext);

    boolean support(UnitContext unitContext);
}
