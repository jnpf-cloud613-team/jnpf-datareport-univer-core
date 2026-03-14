package jnpf.ureport.expression.expr;


import jnpf.ureport.build.BindData;
import jnpf.ureport.build.Context;
import jnpf.ureport.expression.data.BindDataListExpressionData;
import jnpf.ureport.expression.data.ExpressionData;
import jnpf.ureport.expression.data.ObjectExpressionData;
import jnpf.ureport.expression.data.ObjectListExpressionData;
import jnpf.ureport.expression.util.Operator;
import jnpf.ureport.model.Cell;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @since 1月15日
 */
@Getter
@Setter
public class JoinExpression extends BaseExpression {
    private List<Operator> operators;
    private List<BaseExpression> expressions;

    public JoinExpression(List<Operator> operators, List<BaseExpression> expressions) {
        this.operators = operators;
        this.expressions = expressions;
    }

    @Override
    protected ExpressionData compute(Cell cell, Cell currentCell, Context context) {
        if (expressions.size() == 1) {
            return expressions.get(0).compute(cell, currentCell, context);
        }
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < expressions.size(); i++) {
            BaseExpression expression = expressions.get(i);
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

            list.add(obj);
        }
        String str = null;
        for (int i = 0; i < list.size(); i++) {
            Object data = list.get(i);
            if (str == null) {
                str = "" + data;
            } else {
                Operator op = operators.get(i - 1);
                str += "" + op + data;
            }
        }
        //todo
        Object obj = context.evalExpr(str);
        return new ObjectExpressionData(obj);
    }
}
