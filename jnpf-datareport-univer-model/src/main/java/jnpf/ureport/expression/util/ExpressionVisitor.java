package jnpf.ureport.expression.util;

import jnpf.ureport.expression.antlr.ReportParser.*;
import jnpf.ureport.expression.antlr.ReportVisitor;
import jnpf.ureport.expression.builder.ExpressionBuilder;
import jnpf.ureport.expression.expr.*;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @since 2016年11月18日
 */
public class ExpressionVisitor extends ReportVisitor {
    private List<ExpressionBuilder> expressionBuilders;

    public ExpressionVisitor(List<ExpressionBuilder> expressionBuilders) {
        this.expressionBuilders = expressionBuilders;
    }

    @Override
    public Expression visitEntry(EntryContext ctx) {
        StringBuilder sb = new StringBuilder();
        List<ExpressionContext> exprs = ctx.expression();
        List<Expression> list = new ArrayList<>();
        for (ExpressionContext exprContext : exprs) {
            sb.append(exprContext.getText());
            Expression expr = visitExpression(exprContext);
            list.add(expr);
        }
        BlockExpression block = new BlockExpression();
        block.setExpressionList(list);
        block.setExpr(sb.toString());
        return block;
    }

    @Override
    public Expression visitExpression(ExpressionContext ctx) {
        ExprCompositeContext exprCompositeContext = ctx.exprComposite();
        IfExprContext ifExprContext = ctx.ifExpr();
        CaseExprContext caseExprContext = ctx.caseExpr();
        VariableAssignContext assignCtx = ctx.variableAssign();
        ReturnExprContext returnCtx = ctx.returnExpr();
        if (exprCompositeContext != null) {
            return parseExprComposite(exprCompositeContext);
        } else if (ifExprContext != null) {
            IfExpression expr = parseIfExprContext(ifExprContext);
            return expr;
        } else if (caseExprContext != null) {
            IfExpression expr = parseCaseExprContext(caseExprContext);
            return expr;
        } else if (assignCtx != null) {
            AssignExpression expr = new AssignExpression();
            expr.setExpr(assignCtx.getText());
            expr.setVariable(assignCtx.variable().Identifier().getText());
            expr.setExpression(parseItemContext(assignCtx.item()));
            return expr;
        } else if (returnCtx != null) {
            return parseExpr(returnCtx.expr());
        }
        return null;
    }

    private Expression parseExprComposite(ExprCompositeContext exprCompositeContext) {
        if (exprCompositeContext instanceof SingleExprCompositeContext) {
            SingleExprCompositeContext singleExprCompositeContext = (SingleExprCompositeContext) exprCompositeContext;
            ExprContext exprContext = singleExprCompositeContext.expr();
            return parseExpr(exprContext);
        } else if (exprCompositeContext instanceof ParenExprCompositeContext) {
            ParenExprCompositeContext parenExprCompositeContext = (ParenExprCompositeContext) exprCompositeContext;
            ExprCompositeContext childExprCompositeContext = parenExprCompositeContext.exprComposite();
            return parseExprComposite(childExprCompositeContext);
        } else if (exprCompositeContext instanceof TernaryExprCompositeContext) {
            TernaryExprCompositeContext ternaryExprCompositeContext = (TernaryExprCompositeContext) exprCompositeContext;
            TernaryExprContext ternaryExprContext = ternaryExprCompositeContext.ternaryExpr();
            List<IfConditionContext> ifConditionContexts = ternaryExprContext.ifCondition();
            IfExpression expr = new IfExpression();
            expr.setExpressionConditionList(parseCondtionList(ifConditionContexts, ternaryExprContext.join()));
            BlockContext firstBlockContext = ternaryExprContext.block(0);
            expr.setExpression(parseBlock(firstBlockContext));
            BlockContext secondBlockContext = ternaryExprContext.block(1);
            ElseExpression elseExpr = new ElseExpression();
            elseExpr.setExpression(parseBlock(secondBlockContext));
            expr.setElseExpression(elseExpr);
            return expr;
        } else if (exprCompositeContext instanceof ComplexExprCompositeContext) {
            ComplexExprCompositeContext complexExprCompositeContext = (ComplexExprCompositeContext) exprCompositeContext;
            ExprCompositeContext leftExprCompositeContext = complexExprCompositeContext.exprComposite(0);
            Expression leftExpression = parseExprComposite(leftExprCompositeContext);
            ExprCompositeContext rightExprCompositeContext = complexExprCompositeContext.exprComposite(1);
            Expression rightExpression = parseExprComposite(rightExprCompositeContext);
            String op = complexExprCompositeContext.Operator().getText();
            Operator operator = Operator.parse(op);
            List<BaseExpression> expressions = new ArrayList<>();
            expressions.add((BaseExpression) leftExpression);
            expressions.add((BaseExpression) rightExpression);
            List<Operator> operators = new ArrayList<>();
            operators.add(operator);
            ParenExpression expression = new ParenExpression(operators, expressions);
            expression.setExpr(complexExprCompositeContext.getText());
            return expression;
        }
        return null;
    }

    private BlockExpression parseExpressionBlock(List<ExprBlockContext> contexts) {
        StringBuilder sb = new StringBuilder();
        List<Expression> expressionList = new ArrayList<>();
        for (ExprBlockContext ctx : contexts) {
            sb.append(ctx.getText());
            VariableAssignContext assignContext = ctx.variableAssign();
            if (assignContext != null) {
                VariableContext varCtx = assignContext.variable();
                String variableName = varCtx.Identifier().getText();
                AssignExpression assignExpr = new AssignExpression();
                assignExpr.setExpr(assignContext.getText());
                assignExpr.setVariable(variableName);
                ItemContext itemCtx = assignContext.item();
                BaseExpression itemExpr = parseItemContext(itemCtx);
                assignExpr.setExpression(itemExpr);
                expressionList.add(assignExpr);
            }
            IfExprContext ifCtx = ctx.ifExpr();
            if (ifCtx != null) {
                IfExpression ifExpr = parseIfExprContext(ifCtx);
                expressionList.add(ifExpr);
            }
            CaseExprContext caseCtx = ctx.caseExpr();
            if (caseCtx != null) {
                IfExpression caseExpr = parseCaseExprContext(caseCtx);
                expressionList.add(caseExpr);
            }
        }
        BlockExpression blockExpr = new BlockExpression();
        blockExpr.setExpressionList(expressionList);
        blockExpr.setExpr(sb.toString());
        return blockExpr;
    }

    private IfExpression parseIfExprContext(IfExprContext ifExprContext) {
        IfExpression expr = new IfExpression();
        expr.setExpr(ifExprContext.getText());
        IfPartContext ifPartContext = ifExprContext.ifPart();
        List<IfConditionContext> ifConditionContexts = ifPartContext.ifCondition();
        List<JoinContext> joinContexts = ifPartContext.join();
        expr.setExpressionConditionList(parseCondtionList(ifConditionContexts, joinContexts));
        BlockExpression blockExpr = parseBlock(ifPartContext.block());
        expr.setExpression(blockExpr);
        List<ElseIfPartContext> elseIfPartContexts = ifExprContext.elseIfPart();
        if (elseIfPartContexts != null && elseIfPartContexts.size() > 0) {
            List<ElseIfExpression> elseIfExpressionList = new ArrayList<>();
            for (ElseIfPartContext elseIfContext : elseIfPartContexts) {
                ifConditionContexts = elseIfContext.ifCondition();
                joinContexts = elseIfContext.join();
                ElseIfExpression elseIfExpr = new ElseIfExpression();
                elseIfExpr.setExpressionConditionList(parseCondtionList(ifConditionContexts, joinContexts));
                elseIfExpr.setExpression(parseBlock(elseIfContext.block()));
                elseIfExpressionList.add(elseIfExpr);
            }
            expr.setElseIfExpressions(elseIfExpressionList);
        }
        ElsePartContext elsePartContext = ifExprContext.elsePart();
        if (elsePartContext != null) {
            ElseExpression elseExpression = new ElseExpression();
            elseExpression.setExpression(parseBlock(elsePartContext.block()));
            expr.setElseExpression(elseExpression);
        }
        return expr;
    }

    private BlockExpression parseBlock(BlockContext blockCtx) {
        List<ExprBlockContext> exprBlockCtxs = blockCtx.exprBlock();
        ReturnExprContext returnCtx = blockCtx.returnExpr();
        BlockExpression block = null;
        if (exprBlockCtxs != null) {
            block = parseExpressionBlock(exprBlockCtxs);
        }
        if (returnCtx != null) {
            if (block == null) block = new BlockExpression();
            block.setReturnExpression(parseExpr(returnCtx.expr()));
        }
        return block;
    }

    private IfExpression parseCaseExprContext(CaseExprContext caseExprContext) {
        IfExpression expr = new IfExpression();
        List<ElseIfExpression> elseIfExpressionList = new ArrayList<>();
        expr.setElseIfExpressions(elseIfExpressionList);
        List<CasePartContext> casePartContexts = caseExprContext.casePart();
        for (CasePartContext casePartContext : casePartContexts) {
            List<IfConditionContext> ifConditionContexts = casePartContext.ifCondition();
            List<JoinContext> joinContexts = casePartContext.join();
            ElseIfExpression elseIfExpr = new ElseIfExpression();
            elseIfExpr.setExpressionConditionList(parseCondtionList(ifConditionContexts, joinContexts));
            elseIfExpr.setExpr(casePartContext.getText());
            BlockExpression blockExpr = parseBlock(casePartContext.block());
            elseIfExpr.setExpression(blockExpr);
            elseIfExpressionList.add(elseIfExpr);
        }
        return expr;
    }

    private Expression parseExpr(ExprContext exprContext) {
        List<BaseExpression> expressions = new ArrayList<>();
        List<Operator> operators = new ArrayList<>();
        List<ItemContext> itemContexts = exprContext.item();
        List<TerminalNode> operatorNodes = exprContext.Operator();
        for (int i = 0; i < itemContexts.size(); i++) {
            ItemContext itemContext = itemContexts.get(i);
            BaseExpression expr = parseItemContext(itemContext);
            expressions.add(expr);
            if (i > 0) {
                TerminalNode operatorNode = operatorNodes.get(i - 1);
                String op = operatorNode.getText();
                operators.add(Operator.parse(op));
            }
        }
        ParenExpression expression = new ParenExpression(operators, expressions);
        expression.setExpr(exprContext.getText());
        return expression;
    }

    private ExpressionConditionList parseCondtionList(List<IfConditionContext> ifConditionContexts, List<JoinContext> joinContexts) {
        List<ExpressionCondition> list = new ArrayList<>();
        List<Join> joins = new ArrayList<>();
        for (int i = 0; i < ifConditionContexts.size(); i++) {
            IfConditionContext context = ifConditionContexts.get(i);
            ExprContext left = context.expr(0);
            ExprContext right = context.expr(1);
            Expression leftExpr = parseExpr(left);
            Expression rightExpr = parseExpr(right);
            Op op = Op.parse(context.OP().getText());
            ExpressionCondition expressionCondition = new ExpressionCondition(leftExpr, op, rightExpr);
            list.add(expressionCondition);
            if (i > 0) {
                JoinContext joinContext = joinContexts.get(i - 1);
                String text = joinContext.getText();
                Join join = Join.and;
                if (text.equals("or") || text.equals("||")) {
                    join = Join.or;
                }
                joins.add(join);
            }
        }
        return new ExpressionConditionList(list, joins);
    }

    public BaseExpression parseItemContext(ItemContext itemContext) {
        BaseExpression expression = null;
        if (itemContext instanceof SimpleJoinContext) {
            SimpleJoinContext simpleJoinContext = (SimpleJoinContext) itemContext;
            expression = visitSimpleJoin(simpleJoinContext);
        } else if (itemContext instanceof ParenJoinContext) {
            ParenJoinContext parenJoinContext = (ParenJoinContext) itemContext;
            expression = visitParenJoin(parenJoinContext);
        } else if (itemContext instanceof SingleParenJoinContext) {
            SingleParenJoinContext singleContext = (SingleParenJoinContext) itemContext;
            ItemContext childItemContext = singleContext.item();
            expression = parseItemContext(childItemContext);
        }
        return expression;
    }

    @Override
    public BaseExpression visitSimpleJoin(SimpleJoinContext ctx) {
        List<BaseExpression> expressions = new ArrayList<>();
        List<Operator> operators = new ArrayList<>();
        List<UnitContext> unitContexts = ctx.unit();
        List<TerminalNode> operatorNodes = ctx.Operator();
        for (int i = 0; i < unitContexts.size(); i++) {
            UnitContext unitContext = unitContexts.get(i);
            BaseExpression expr = buildExpression(unitContext);
            if (expr != null) {
                expressions.add(expr);
                if (i > 0) {
                    TerminalNode operatorNode = operatorNodes.get(i - 1);
                    String op = operatorNode.getText();
                    operators.add(Operator.parse(op));
                }
            }
        }
        if (operators.size() == 0 && expressions.size() == 1) {
            return expressions.get(0);
        }
        JoinExpression expression = new JoinExpression(operators, expressions);
        expression.setExpr(ctx.getText());
        return expression;
    }

    @Override
    public BaseExpression visitParenJoin(ParenJoinContext ctx) {
        List<BaseExpression> expressions = new ArrayList<>();
        List<Operator> operators = new ArrayList<>();
        List<ItemContext> itemContexts = ctx.item();
        List<TerminalNode> operatorNodes = ctx.Operator();
        for (int i = 0; i < itemContexts.size(); i++) {
            ItemContext itemContext = itemContexts.get(i);
            BaseExpression expr = parseItemContext(itemContext);
            expressions.add(expr);
            if (i > 0) {
                TerminalNode operatorNode = operatorNodes.get(i - 1);
                String op = operatorNode.getText();
                operators.add(Operator.parse(op));
            }
        }
        ParenExpression expression = new ParenExpression(operators, expressions);
        expression.setExpr(ctx.getText());
        return expression;
    }

    private BaseExpression buildExpression(UnitContext unitContext) {
        for (ExpressionBuilder builder : expressionBuilders) {
            if (builder.support(unitContext)) {
                return builder.build(unitContext);
            }
        }
        return null;
    }
}
