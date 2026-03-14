package jnpf.ureport.expression.builder;

import jnpf.ureport.expression.antlr.ReportParser.*;
import jnpf.ureport.expression.condition.BaseCondition;
import jnpf.ureport.expression.condition.CellCoordinate;
import jnpf.ureport.expression.condition.CellCoordinateSet;
import jnpf.ureport.expression.condition.CoordinateType;
import jnpf.ureport.expression.expr.*;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;


/**
 * @author
 * @since 2016年12月25日
 */
public class SetExpressionBuilder extends BaseExpressionBuilder {

    @Override
    public BaseExpression build(UnitContext unitContext) {
        SetContext context = unitContext.set();
        BaseExpression setExpr = buildSetExpression(context);
        if (setExpr != null) {
            setExpr.setExpr(context.getText());
        }
        return setExpr;
    }

    public BaseExpression buildSetExpression(SetContext context) {
        if (context instanceof SingleCellContext) {
            TerminalNode cellNode = ((SingleCellContext) context).Cell();
            return new CellExpression(cellNode.getText());
        } else if (context instanceof WholeCellContext) {
            WholeCellContext ctx = (WholeCellContext) context;
            WholeCellExpression wholeCellExpression = new WholeCellExpression(ctx.Cell().getText());
            ConditionsContext conditionsContext = ctx.conditions();
            if (conditionsContext != null) {
                BaseCondition condition = buildConditions(conditionsContext);
                wholeCellExpression.setExpressionCondition(condition);
            }
            return wholeCellExpression;
        } else if (context instanceof SingleCellConditionContext) {
            SingleCellConditionContext ctx = (SingleCellConditionContext) context;
            BaseCondition condition = buildConditions(ctx.conditions());
            return new CellConditionExpression(ctx.Cell().getText(), condition);
        } else if (context instanceof SingleCellCoordinateContext) {
            SingleCellCoordinateContext ctx = (SingleCellCoordinateContext) context;
            String cellName = null;
            if (ctx.Cell() != null) {
                cellName = ctx.Cell().getText();
            }
            CellCoordinateContext cellCoordinateContext = ctx.cellCoordinate();
            List<CoordinateContext> coordinateContexts = cellCoordinateContext.coordinate();
            CellCoordinateSet leftCoordinate = parseCellCoordinateSet(coordinateContexts.get(0));
            CellCoordinateSet topCoordinate = null;
            if (coordinateContexts.size() > 1) {
                topCoordinate = parseCellCoordinateSet(coordinateContexts.get(1));
            }
            CellCoordinateExpression cellCoordinate = new CellCoordinateExpression(cellName);
            cellCoordinate.setTopCoordinate(topCoordinate);
            cellCoordinate.setLeftCoordinate(leftCoordinate);
            return cellCoordinate;
        } else if (context instanceof CellCoordinateConditionContext) {
            CellCoordinateConditionContext ctx = (CellCoordinateConditionContext) context;
            String cellName = null;
            if (ctx.Cell() != null) {
                cellName = ctx.Cell().getText();
            }
            CellCoordinateContext cellCoordinateContext = ctx.cellCoordinate();
            List<CoordinateContext> coordinateContexts = cellCoordinateContext.coordinate();
            CellCoordinateSet leftCoordinate = parseCellCoordinateSet(coordinateContexts.get(0));
            CellCoordinateSet topCoordinate = null;
            if (coordinateContexts.size() > 1) {
                topCoordinate = parseCellCoordinateSet(coordinateContexts.get(1));
            }
            BaseCondition condition = buildConditions(ctx.conditions());
            CellCoordinateExpression cellCoordinate = new CellCoordinateExpression(cellName);
            cellCoordinate.setTopCoordinate(topCoordinate);
            cellCoordinate.setLeftCoordinate(leftCoordinate);
            cellCoordinate.setCondition(condition);
            return cellCoordinate;
        } else if (context instanceof RangeContext) {
            RangeContext ctx = (RangeContext) context;
            List<SetContext> sets = ctx.set();
            if (sets.size() != 2) {
                return null;
            }
            BaseExpression fromExpr = buildSetExpression(sets.get(0));
            BaseExpression toExpr = buildSetExpression(sets.get(1));
            FromToExpression expr = new FromToExpression(fromExpr, toExpr);
            return expr;
        } else if (context instanceof SimpleDataContext) {
            SimpleDataContext ctx = (SimpleDataContext) context;
            SimpleValueContext valueContext = ctx.simpleValue();
            return parseSimpleValueContext(valueContext);
        }
        return null;
    }

    private CellCoordinateSet parseCellCoordinateSet(CoordinateContext ctx) {
        List<CellCoordinate> coordinates = new ArrayList<>();
        for (CellIndicatorContext indicatorContext : ctx.cellIndicator()) {
            if (indicatorContext instanceof RelativeContext) {
                RelativeContext context = (RelativeContext) indicatorContext;
                String cellName = context.Cell().getText();
                CellCoordinate coordinate = new CellCoordinate(cellName, CoordinateType.relative);
                coordinates.add(coordinate);
            } else {
                AbsoluteContext context = (AbsoluteContext) indicatorContext;
                String cellName = context.Cell().getText();
                String pos = context.INTEGER().getText();
                int position = Integer.valueOf(pos);
                CellCoordinate coordinate = new CellCoordinate(cellName, CoordinateType.absolute);
                coordinate.setPosition(position);
                if (context.EXCLAMATION() != null) {
                    coordinate.setReverse(true);
                }
                coordinates.add(coordinate);
            }
        }
        CellCoordinateSet coordinateSet = new CellCoordinateSet();
        coordinateSet.setCellCoordinates(coordinates);
        return coordinateSet;
    }

    @Override
    public boolean support(UnitContext unitContext) {
        return unitContext.set() != null;
    }
}
