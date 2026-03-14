package jnpf.ureport.expression.builder;


import jnpf.ureport.expression.antlr.ReportParser.UnitContext;
import jnpf.ureport.expression.expr.BaseExpression;
import jnpf.ureport.expression.expr.CellValueExpression;

/**
 * @author
 * @since 7月11日
 */
public class CurrentCellValueExpressionBuilder implements ExpressionBuilder {

    @Override
    public BaseExpression build(UnitContext unitContext) {
        return new CellValueExpression();
    }

    @Override
    public boolean support(UnitContext unitContext) {
        return unitContext.currentCellValue() != null;
    }

}
