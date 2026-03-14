package jnpf.ureport.expression.antlr;

import jnpf.ureport.expression.antlr.ReportParser.*;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/11/1 上午9:46
 */
public class ReportVisitor<T> extends AbstractParseTreeVisitor<T> {

    public T visitEntry(EntryContext ctx) {
        return visitChildren(ctx);
    }

    public T visitExpression(ExpressionContext ctx) {
        return visitChildren(ctx);
    }

    public T visitComplexExprComposite(ComplexExprCompositeContext ctx) {
        return visitChildren(ctx);
    }

    public T visitSingleExprComposite(SingleExprCompositeContext ctx) {
        return visitChildren(ctx);
    }

    public T visitParenExprComposite(ParenExprCompositeContext ctx) {
        return visitChildren(ctx);
    }

    public T visitTernaryExprComposite(TernaryExprCompositeContext ctx) {
        return visitChildren(ctx);
    }

    public T visitTernaryExpr(TernaryExprContext ctx) {
        return visitChildren(ctx);
    }

    public T visitCaseExpr(CaseExprContext ctx) {
        return visitChildren(ctx);
    }

    public T visitCasePart(CasePartContext ctx) {
        return visitChildren(ctx);
    }

    public T visitIfExpr(IfExprContext ctx) {
        return visitChildren(ctx);
    }

    public T visitIfPart(IfPartContext ctx) {
        return visitChildren(ctx);
    }

    public T visitElseIfPart(ElseIfPartContext ctx) {
        return visitChildren(ctx);
    }

    public T visitElsePart(ElsePartContext ctx) {
        return visitChildren(ctx);
    }

    public T visitBlock(BlockContext ctx) {
        return visitChildren(ctx);
    }

    public T visitExprBlock(ExprBlockContext ctx) {
        return visitChildren(ctx);
    }

    public T visitReturnExpr(ReturnExprContext ctx) {
        return visitChildren(ctx);
    }

    public T visitExpr(ExprContext ctx) {
        return visitChildren(ctx);
    }

    public T visitIfCondition(IfConditionContext ctx) {
        return visitChildren(ctx);
    }

    public T visitVariableAssign(VariableAssignContext ctx) {
        return visitChildren(ctx);
    }

    public T visitSimpleJoin(SimpleJoinContext ctx) {
        return visitChildren(ctx);
    }

    public T visitSingleParenJoin(SingleParenJoinContext ctx) {
        return visitChildren(ctx);
    }

    public T visitParenJoin(ParenJoinContext ctx) {
        return visitChildren(ctx);
    }

    public T visitUnit(UnitContext ctx) {
        return visitChildren(ctx);
    }

    public T visitVariable(VariableContext ctx) {
        return visitChildren(ctx);
    }

    public T visitCellPosition(CellPositionContext ctx) {
        return visitChildren(ctx);
    }

    public T visitRelativeCell(RelativeCellContext ctx) {
        return visitChildren(ctx);
    }

    public T visitCurrentCellValue(CurrentCellValueContext ctx) {
        return visitChildren(ctx);
    }

    public T visitCurrentCellData(CurrentCellDataContext ctx) {
        return visitChildren(ctx);
    }

    public T visitCell(CellContext ctx) {
        return visitChildren(ctx);
    }

    public T visitDataset(DatasetContext ctx) {
        return visitChildren(ctx);
    }

    public T visitFunction(FunctionContext ctx) {
        return visitChildren(ctx);
    }

    public T visitFunctionParameter(FunctionParameterContext ctx) {
        return visitChildren(ctx);
    }

    public T visitCellPair(CellPairContext ctx) {
        return visitChildren(ctx);
    }

    public T visitWholeCell(WholeCellContext ctx) {
        return visitChildren(ctx);
    }

    public T visitCellCoordinateCondition(CellCoordinateConditionContext ctx) {
        return visitChildren(ctx);
    }

    public T visitSingleCellCondition(SingleCellConditionContext ctx) {
        return visitChildren(ctx);
    }

    public T visitSingleCell(SingleCellContext ctx) {
        return visitChildren(ctx);
    }

    public T visitSimpleData(SimpleDataContext ctx) {
        return visitChildren(ctx);
    }

    public T visitRange(RangeContext ctx) {
        return visitChildren(ctx);
    }

    public T visitSingleCellCoordinate(SingleCellCoordinateContext ctx) {
        return visitChildren(ctx);
    }

    public T visitCellCoordinate(CellCoordinateContext ctx) {
        return visitChildren(ctx);
    }

    public T visitCoordinate(CoordinateContext ctx) {
        return visitChildren(ctx);
    }

    public T visitRelative(RelativeContext ctx) {
        return visitChildren(ctx);
    }

    public T visitAbsolute(AbsoluteContext ctx) {
        return visitChildren(ctx);
    }

    public T visitConditions(ConditionsContext ctx) {
        return visitChildren(ctx);
    }

    public T visitCellNameExprCondition(CellNameExprConditionContext ctx) {
        return visitChildren(ctx);
    }

    public T visitPropertyCondition(PropertyConditionContext ctx) {
        return visitChildren(ctx);
    }

    public T visitCurrentValueCondition(CurrentValueConditionContext ctx) {
        return visitChildren(ctx);
    }

    public T visitExprCondition(ExprConditionContext ctx) {
        return visitChildren(ctx);
    }

    public T visitProperty(PropertyContext ctx) {
        return visitChildren(ctx);
    }

    public T visitCurrentValue(CurrentValueContext ctx) {
        return visitChildren(ctx);
    }

    public T visitSimpleValue(SimpleValueContext ctx) {
        return visitChildren(ctx);
    }

    public T visitJoin(JoinContext ctx) {
        return visitChildren(ctx);
    }

    public T visitAggregate(AggregateContext ctx) {
        return visitChildren(ctx);
    }

}
