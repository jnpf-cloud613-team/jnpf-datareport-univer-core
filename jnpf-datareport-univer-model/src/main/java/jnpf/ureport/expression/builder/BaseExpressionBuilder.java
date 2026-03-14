package jnpf.ureport.expression.builder;

import jnpf.ureport.expression.antlr.ReportParser.*;
import jnpf.ureport.expression.condition.*;
import jnpf.ureport.expression.expr.*;
import jnpf.ureport.expression.util.ExpressionUtils;
import jnpf.ureport.expression.util.Op;
import jnpf.ureport.utils.DataUtils;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author
 * @since 2016年12月26日
 */
public abstract class BaseExpressionBuilder implements ExpressionBuilder {
    protected BaseExpression parseSimpleValueContext(SimpleValueContext valueContext) {
        if (valueContext.BOOLEAN() != null) {
            return new BooleanExpression(Boolean.valueOf(valueContext.getText()));
        } else if (valueContext.INTEGER() != null) {
            return new IntegerExpression(Integer.valueOf(valueContext.INTEGER().getText()));
        } else if (valueContext.STRING() != null) {
            String text = valueContext.STRING().getText();
            text = text.substring(1, text.length() - 1);
            return new StringExpression(text);
        } else if (valueContext.NUMBER() != null) {
            BigDecimal number = DataUtils.toBigDecimal(valueContext.NUMBER().getText());
            return new NumberExpression(number);
        } else if (valueContext.NULL() != null) {
            return new NullExpression();
        }
        return new StringExpression("");
    }


    protected BaseCondition buildConditions(ConditionsContext conditionsContext) {
        List<ConditionContext> conditionContextList = conditionsContext.condition();
        List<JoinContext> joins = conditionsContext.join();
        BaseCondition condition = null;
        BaseCondition topCondition = null;
        int opIndex = 0;
        for (ConditionContext conditionCtx : conditionContextList) {
            if (condition == null) {
                condition = parseCondition(conditionCtx);
                topCondition = condition;
            } else {
                BaseCondition nextCondition = parseCondition(conditionCtx);
                condition.setNextCondition(nextCondition);
                condition.setJoin(Join.parse(joins.get(opIndex).getText()));
                opIndex++;
                condition = nextCondition;
            }
        }
        return topCondition;
    }

    private BaseCondition parseCondition(ConditionContext context) {
        if (context instanceof ExprConditionContext) {
            ExprConditionContext ctx = (ExprConditionContext) context;
            BothExpressionCondition condition = new BothExpressionCondition();
            List<ExprContext> exprContexts = ctx.expr();
            String left = exprContexts.get(0).getText();
            condition.setLeft(left);
            Expression leftExpr = ExpressionUtils.parseExpression(left);
            condition.setLeftExpression(leftExpr);
            String rightExpr = exprContexts.get(1).getText();
            condition.setRight(rightExpr);
            condition.setRightExpression(ExpressionUtils.parseExpression(rightExpr));
            condition.setOp(parseOp(ctx.OP()));
            condition.setOperation(ctx.OP().getText());
            return condition;
        } else if (context instanceof CurrentValueConditionContext) {
            CurrentValueConditionContext ctx = (CurrentValueConditionContext) context;
            CurrentValueExpressionCondition condition = new CurrentValueExpressionCondition();
            String rightExpr = ctx.expr().getText();
            condition.setRight(rightExpr);
            condition.setRightExpression(ExpressionUtils.parseExpression(rightExpr));
            condition.setOp(parseOp(ctx.OP()));
            return condition;
        } else if (context instanceof PropertyConditionContext) {
            PropertyConditionContext ctx = (PropertyConditionContext) context;
            PropertyExpressionCondition condition = new PropertyExpressionCondition();
            String left = ctx.property().getText();
            condition.setLeft(left);
            condition.setLeftProperty(left);
            String rightExpr = ctx.expr().getText();
            condition.setRight(rightExpr);
            condition.setRightExpression(ExpressionUtils.parseExpression(rightExpr));
            condition.setOp(parseOp(ctx.OP()));
            return condition;
        } else if (context instanceof CellNameExprConditionContext) {
            CellNameExprConditionContext ctx = (CellNameExprConditionContext) context;
            CellExpressionCondition condition = new CellExpressionCondition();
            String left = ctx.Cell().getText();
            condition.setLeft(left);
            condition.setCellName(left);
            String rightExpr = ctx.expr().getText();
            condition.setRight(rightExpr);
            condition.setRightExpression(ExpressionUtils.parseExpression(rightExpr));
            condition.setOp(parseOp(ctx.OP()));
            return condition;
        }
        return null;
    }

    private Op parseOp(TerminalNode opNode) {
        if (opNode.getText().equals(">")) {
            return Op.GreatThen;
        }
        if (opNode.getText().equals("<")) {
            return Op.LessThen;
        }
        if (opNode.getText().equals(">=")) {
            return Op.EqualsGreatThen;
        }
        if (opNode.getText().equals("<=")) {
            return Op.EqualsLessThen;
        }
        if (opNode.getText().equals("==")) {
            return Op.Equals;
        }
        if (opNode.getText().equals("!=")) {
            return Op.NotEquals;
        }
        return Op.Equals;
    }
}
