package jnpf.ureport.expression.expr;

import jnpf.ureport.build.Context;
import jnpf.ureport.expression.condition.Condition;
import jnpf.ureport.expression.data.ExpressionData;
import jnpf.ureport.model.Cell;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @since 2016年11月18日
 */
@Getter
@Setter
public abstract class BaseExpression implements Expression {
    protected String expr;

    @Override
    public ExpressionData execute(Cell cell, Cell currentCell, Context context) {
        ExpressionData data = compute(cell, currentCell, context);
        return data;
    }

    protected abstract ExpressionData compute(Cell cell, Cell currentCell, Context context);

    protected List<Cell> filterCells(Cell cell, Context context, Condition condition, List<Cell> targetCells) {
        if (condition == null) {
            return targetCells;
        }
        List<Cell> list = new ArrayList<>();
        for (Cell targetCell : targetCells) {
            boolean conditionResult = true;
            List<Map<String, Object>> dataList = targetCell.getBindData();
            if (dataList == null) {
                conditionResult = false;
            } else {
                for (Map<String, Object> obj : dataList) {
                    boolean result = condition.filter(cell, targetCell, obj, context);
                    if (!result) {
                        conditionResult = false;
                        break;
                    }
                }
            }
            if (!conditionResult) {
                continue;
            }
            list.add(targetCell);
        }
        return list;
    }

}
