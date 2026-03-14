package jnpf.ureport.expression.expr;

import jnpf.ureport.build.Context;
import jnpf.ureport.expression.data.ExpressionData;
import jnpf.ureport.expression.data.ObjectExpressionData;
import jnpf.ureport.expression.data.ObjectListExpressionData;
import jnpf.ureport.model.Cell;
import jnpf.ureport.utils.DataUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @since 1月1日
 */
public class FromToExpression extends BaseExpression {
    private BaseExpression fromExpression;
    private BaseExpression toExpression;

    public FromToExpression(BaseExpression fromExpression, BaseExpression toExpression) {
        this.fromExpression = fromExpression;
        this.toExpression = toExpression;
    }

    @Override
    protected ExpressionData compute(Cell cell, Cell currentCell, Context context) {
        Object fromData = fromExpression.execute(cell, currentCell, context);
        Object toData = toExpression.execute(cell, currentCell, context);
        int from = convertFloatData(fromData), to = convertFloatData(toData);
        List<Object> list = new ArrayList<>();
        for (int i = from; i <= to; i++) {
            list.add(i);
        }
        return new ObjectListExpressionData(list);
    }

    private int convertFloatData(Object data) {
        int value = 0;
        if (data instanceof ObjectExpressionData) {
            Object obj = ((ObjectExpressionData) data).getData();
            try {
                value = DataUtils.toBigDecimal(String.valueOf(obj)).intValue();
            } catch (Exception e) {
            }
        }
        return value;
    }


}
