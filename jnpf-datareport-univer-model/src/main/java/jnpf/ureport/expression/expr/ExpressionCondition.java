package jnpf.ureport.expression.expr;

import jnpf.ureport.build.BindData;
import jnpf.ureport.build.Context;
import jnpf.ureport.expression.data.*;
import jnpf.ureport.expression.util.ExpressionUtils;
import jnpf.ureport.expression.util.Op;
import jnpf.ureport.model.Cell;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author
 * @since 1月16日
 */
@Getter
@Setter
public class ExpressionCondition {
    private Expression left;
    private Op op;
    private Expression right;

    public ExpressionCondition(Expression left, Op op, Expression right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }

    public boolean eval(Context context, Cell cell, Cell currentCell) {
        ExpressionData leftData = left.execute(cell, currentCell, context);
        ExpressionData rightData = right.execute(cell, currentCell, context);
        Object leftObj = getData(leftData);
        Object rightObj = getData(rightData);
        return ExpressionUtils.conditionEval(op, leftObj, rightObj);
    }

    private Object getData(ExpressionData data) {
        if (data instanceof ObjectExpressionData) {
            ObjectExpressionData objData = (ObjectExpressionData) data;
            return objData.getData();
        } else if (data instanceof ObjectListExpressionData) {
            ObjectListExpressionData exprData = (ObjectListExpressionData) data;
            List<Object> list = exprData.getData();
            StringBuffer sb = new StringBuffer();
            for (Object obj : list) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(obj);
            }
            return sb.toString();
        } else if (data instanceof NoneExpressionData) {
            NoneExpressionData noneData = (NoneExpressionData) data;
            return noneData.getData();
        } else if (data instanceof BindDataListExpressionData) {
            BindDataListExpressionData bindDataList = (BindDataListExpressionData) data;
            List<BindData> list = bindDataList.getData();
            if (list.size() == 1) {
                return list.get(0).getValue();
            } else {
                StringBuffer sb = new StringBuffer();
                for (BindData bindData : list) {
                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append(bindData.getValue());
                }
                return sb.toString();
            }
        }
        return "";
    }

}
