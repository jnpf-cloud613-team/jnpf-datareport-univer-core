package jnpf.ureport.expression.builder;


import jnpf.ureport.expression.antlr.ReportParser.FunctionContext;
import jnpf.ureport.expression.antlr.ReportParser.FunctionParameterContext;
import jnpf.ureport.expression.antlr.ReportParser.ItemContext;
import jnpf.ureport.expression.antlr.ReportParser.UnitContext;
import jnpf.ureport.expression.expr.BaseExpression;
import jnpf.ureport.expression.expr.FunctionExpression;
import jnpf.ureport.expression.util.ExpressionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @since 2016年12月26日
 */
public class FunctionExpressionBuilder extends BaseExpressionBuilder {
    @Override
    public BaseExpression build(UnitContext unitContext) {
        FunctionContext ctx = unitContext.function();
        FunctionExpression expr = new FunctionExpression();
        expr.setExpr(ctx.getText());
        expr.setName(ctx.Identifier().getText());
        FunctionParameterContext functionParameterContext = ctx.functionParameter();
        if (functionParameterContext != null) {
            List<BaseExpression> exprList = new ArrayList<>();
            List<ItemContext> itemContexts = functionParameterContext.item();
            if (itemContexts != null) {
                for (int i = 0; i < itemContexts.size(); i++) {
                    ItemContext itemContext = itemContexts.get(i);
                    BaseExpression baseExpr = ExpressionUtils.getExprVisitor().parseItemContext(itemContext);
                    exprList.add(baseExpr);
                }
            }
            expr.setExpressions(exprList);
        }
        return expr;
    }

    @Override
    public boolean support(UnitContext unitContext) {
        return unitContext.function() != null;
    }
}
