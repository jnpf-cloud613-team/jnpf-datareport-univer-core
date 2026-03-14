package jnpf.ureport.expression.condition;

import jnpf.ureport.build.BindData;
import jnpf.ureport.build.Context;
import jnpf.ureport.expression.data.*;
import jnpf.ureport.expression.expr.Join;
import jnpf.ureport.expression.util.ExpressionUtils;
import jnpf.ureport.expression.util.Op;
import jnpf.ureport.model.Cell;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @since 2016年11月22日
 */
@Getter
@Setter
public abstract class BaseCondition implements Condition {
    private Op op;
    private String operation;
    private Join join;
    private Condition nextCondition;
    private String left;
    private String right;

    @Override
    public final boolean filter(Cell cell, Cell currentCell, Object obj, Context context) {
        Object left = computeLeft(cell, currentCell, obj, context);
        Object right = computeRight(cell, currentCell, obj, context);
        boolean result = ExpressionUtils.conditionEval(op, left, right);
        if (join != null && nextCondition != null) {
            if (result) {
                if (join.equals(Join.and)) {
                    return nextCondition.filter(cell, currentCell, obj, context);
                } else {
                    return result;
                }
            } else {
                if (join.equals(Join.and)) {
                    return result;
                } else {
                    return nextCondition.filter(cell, currentCell, obj, context);
                }
            }
        }
        return result;
    }

    abstract Object computeLeft(Cell cell, Cell currentCell, Object obj, Context context);

    abstract Object computeRight(Cell cell, Cell currentCell, Object obj, Context context);

    public abstract ConditionType getType();

    protected Object extractExpressionData(ExpressionData data) {
        if (data instanceof ObjectExpressionData) {
            return data.getData();
        } else if (data instanceof ObjectListExpressionData) {
            ObjectListExpressionData listData = (ObjectListExpressionData) data;
            List<Object> list = listData.getData();
            return list;
        } else if (data instanceof BindDataListExpressionData) {
            BindDataListExpressionData bindData = (BindDataListExpressionData) data;
            List<BindData> bindDataList = bindData.getData();
            List<Object> list = new ArrayList<>();
            for (BindData bd : bindDataList) {
                Object v = bd.getValue();
                list.add(v);
            }
            if (list.size() == 1) {
                return list.get(0);
            } else if (list.size() == 0) {
                return null;
            }
            return list;
        }
        return new NoneExpressionData().getData();
    }

}
