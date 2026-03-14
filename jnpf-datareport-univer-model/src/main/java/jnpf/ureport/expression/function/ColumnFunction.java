package jnpf.ureport.expression.function;


import jnpf.ureport.build.Context;
import jnpf.ureport.expression.data.ExpressionData;
import jnpf.ureport.model.Cell;
import jnpf.ureport.model.Column;

import java.util.List;

/**
 * @author
 * @since 4月25日
 */
public class ColumnFunction implements Function {

    @Override
    public Object execute(List<ExpressionData> dataList, Context context, Cell currentCell) {
        Column col = currentCell.getColumn();
        return col.getColumnNumber();
    }

    @Override
    public String name() {
        return FunctionType.column.name();
    }
}
