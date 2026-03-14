package jnpf.ureport.expression.expr;


import com.google.common.collect.ImmutableMap;
import jnpf.ureport.build.Context;
import jnpf.ureport.expression.data.ExpressionData;
import jnpf.ureport.expression.data.ObjectExpressionData;
import jnpf.ureport.expression.data.ObjectListExpressionData;
import jnpf.ureport.expression.function.*;
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
@Setter
@Getter
public class FunctionExpression extends BaseExpression {
    private String name;
    private List<BaseExpression> expressions;

    @Override
    public ExpressionData compute(Cell cell, Cell currentCell, Context context) {
        Map<String, Function> functions = ImmutableMap.of(
                FunctionType.avg.name(), new AvgFunction(),
                FunctionType.column.name(), new ColumnFunction(),
                FunctionType.count.name(), new CountFunction(),
                FunctionType.max.name(), new MaxFunction(),
                FunctionType.min.name(), new MinFunction(),
                FunctionType.row.name(), new RowFunction(),
                FunctionType.sum.name(), new SumFunction()
        );
        Function targetFunction = functions.get(name);
        if (targetFunction == null) {
            return new ObjectExpressionData("");
        }
        List<ExpressionData> dataList = new ArrayList<>();
        if (expressions != null) {
            for (BaseExpression expr : expressions) {
                ExpressionData data = expr.execute(cell, currentCell, context);
                dataList.add(data);
            }
        }
        Object obj = targetFunction.execute(dataList, context, currentCell);
        if (obj instanceof List) {
            return new ObjectListExpressionData((List) obj);
        }
        return new ObjectExpressionData(obj);
    }

}
