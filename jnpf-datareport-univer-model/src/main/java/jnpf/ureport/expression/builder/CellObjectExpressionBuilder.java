package jnpf.ureport.expression.builder;

import jnpf.ureport.expression.antlr.ReportParser.CellContext;
import jnpf.ureport.expression.antlr.ReportParser.PropertyContext;
import jnpf.ureport.expression.antlr.ReportParser.UnitContext;
import jnpf.ureport.expression.expr.BaseExpression;
import jnpf.ureport.expression.expr.CellObjectExpression;

/**
 * @author
 * @since 1月20日
 */
public class CellObjectExpressionBuilder implements ExpressionBuilder {

    @Override
    public BaseExpression build(UnitContext unitContext) {
        CellContext ctx = unitContext.cell();
        String property = null;
        PropertyContext propCtx = ctx.property();
        if (propCtx != null) {
            property = propCtx.getText();
        }
        CellObjectExpression expr = new CellObjectExpression(property);
        expr.setExpr(ctx.getText());
        return expr;
    }

    @Override
    public boolean support(UnitContext unitContext) {
        return unitContext.cell() != null;
    }
}
