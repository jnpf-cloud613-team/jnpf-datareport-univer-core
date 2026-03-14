package jnpf.ureport.expression.builder;


import jnpf.ureport.expression.antlr.ReportParser.CellPositionContext;
import jnpf.ureport.expression.antlr.ReportParser.UnitContext;
import jnpf.ureport.expression.expr.BaseExpression;
import jnpf.ureport.expression.expr.CellPositionExpression;

/**
 * @author
 * @since 1月21日
 */
public class CellPositionExpressionBuilder implements ExpressionBuilder {

    @Override
    public BaseExpression build(UnitContext unitContext) {
        CellPositionContext ctx = unitContext.cellPosition();
        CellPositionExpression expr = new CellPositionExpression(ctx.Cell().getText());
        return expr;
    }

    @Override
    public boolean support(UnitContext unitContext) {
        return unitContext.cellPosition() != null;
    }
}
