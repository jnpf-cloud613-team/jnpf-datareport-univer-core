package jnpf.ureport.expression.builder;

import jnpf.ureport.expression.antlr.ReportParser.CurrentCellDataContext;
import jnpf.ureport.expression.antlr.ReportParser.UnitContext;
import jnpf.ureport.expression.expr.BaseExpression;
import jnpf.ureport.expression.expr.CellDataExpression;

/**
 * @author
 * @since 7月11日
 */
public class CurrentCellDataExpressionBuilder implements ExpressionBuilder {

    @Override
    public BaseExpression build(UnitContext unitContext) {
        CurrentCellDataContext context = unitContext.currentCellData();
        CellDataExpression expr = new CellDataExpression();
        expr.setProperty(context.property().getText());
        return expr;
    }

    @Override
    public boolean support(UnitContext unitContext) {
        return unitContext.currentCellData() != null;
    }

}
