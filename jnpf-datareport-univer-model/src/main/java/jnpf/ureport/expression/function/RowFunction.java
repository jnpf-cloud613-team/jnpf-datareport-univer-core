package jnpf.ureport.expression.function;

import jnpf.ureport.build.Context;
import jnpf.ureport.expression.data.ExpressionData;
import jnpf.ureport.model.Cell;
import jnpf.ureport.model.Row;

import java.util.List;

/**
 * @author
 * @since 4月25日
 */
public class RowFunction implements Function {
    @Override
    public Object execute(List<ExpressionData> dataList, Context context, Cell currentCell) {
        Row row = currentCell.getRow();
        return row.getRowNumber();
    }

    @Override
    public String name() {
        return FunctionType.row.name();
    }
}
