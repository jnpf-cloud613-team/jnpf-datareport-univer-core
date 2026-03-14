package jnpf.ureport.expression.expr;

import jnpf.ureport.build.Context;
import jnpf.ureport.expression.condition.Condition;
import jnpf.ureport.expression.data.ExpressionData;
import jnpf.ureport.expression.data.ObjectExpressionData;
import jnpf.ureport.expression.data.ObjectListExpressionData;
import jnpf.ureport.model.Cell;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @since 4月6日
 */
@Getter
@Setter
public class WholeCellExpression extends CellExpression {
    private Condition expressionCondition;

    public WholeCellExpression(String cellName) {
        super(cellName);
    }

    @Override
    public boolean supportPaging() {
        return false;
    }

    @Override
    protected ExpressionData compute(Cell cell, Cell currentCell, Context context) {
        while (!context.isCellPocessed(cellName)) {
            context.getReportBuilder().buildCell(context, null);
        }
        List<Cell> cells = context.getReport().getCellsMap().get(cellName);
        List<Object> list = new ArrayList<>();
        for (Cell c : cells) {
            Object obj = c.getData();
            list.add(obj);
        }
        if (list.size() == 1) {
            return new ObjectExpressionData(list.get(0));
        } else {
            return new ObjectListExpressionData(list);
        }
    }
}
