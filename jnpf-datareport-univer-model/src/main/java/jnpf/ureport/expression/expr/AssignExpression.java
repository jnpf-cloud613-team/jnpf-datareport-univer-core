package jnpf.ureport.expression.expr;


import jnpf.ureport.build.BindData;
import jnpf.ureport.build.Context;
import jnpf.ureport.expression.data.BindDataListExpressionData;
import jnpf.ureport.expression.data.ExpressionData;
import jnpf.ureport.expression.data.ObjectExpressionData;
import jnpf.ureport.expression.data.ObjectListExpressionData;
import jnpf.ureport.model.Cell;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author
 * @since 2018年7月13日
 */
@Getter
@Setter
public class AssignExpression extends BaseExpression {
    private String variable;
    private Expression expression;

    @Override
    protected ExpressionData compute(Cell cell, Cell currentCell, Context context) {
        ExpressionData data = expression.execute(cell, currentCell, context);
        Object obj = null;
        if (data instanceof ObjectExpressionData) {
            ObjectExpressionData d = (ObjectExpressionData) data;
            obj = d.getData();
        } else if (data instanceof ObjectListExpressionData) {
            ObjectListExpressionData d = (ObjectListExpressionData) data;
            obj = d.getData();
        } else if (data instanceof BindDataListExpressionData) {
            BindDataListExpressionData dataList = (BindDataListExpressionData) data;
            List<BindData> bindList = dataList.getData();
            if (bindList.size() == 1) {
                BindData bindData = bindList.get(0);
                obj = bindData.getValue();
            } else {
                StringBuilder sb = new StringBuilder();
                for (BindData bd : bindList) {
                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append(bd.getValue());
                }
                obj = sb.toString();
            }
        }
        if (obj != null) {
            context.putVariable(variable, obj);
        }
        return null;
    }
}
