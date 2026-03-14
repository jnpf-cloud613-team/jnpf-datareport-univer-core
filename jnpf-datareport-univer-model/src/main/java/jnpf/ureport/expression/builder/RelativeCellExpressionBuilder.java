package jnpf.ureport.expression.builder;

import jnpf.ureport.expression.antlr.ReportParser.RelativeCellContext;
import jnpf.ureport.expression.antlr.ReportParser.UnitContext;
import jnpf.ureport.expression.expr.BaseExpression;
import jnpf.ureport.expression.expr.RelativeCellExpression;

/**
 * @author
 * @since 1月21日
 */
public class RelativeCellExpressionBuilder implements ExpressionBuilder {

    @Override
    public BaseExpression build(UnitContext unitContext) {
        RelativeCellContext ctx = unitContext.relativeCell();
        RelativeCellExpression expr = new RelativeCellExpression(ctx.Cell().getText());
        return expr;
    }

    @Override
    public boolean support(UnitContext unitContext) {
        return unitContext.relativeCell() != null;
    }
}
