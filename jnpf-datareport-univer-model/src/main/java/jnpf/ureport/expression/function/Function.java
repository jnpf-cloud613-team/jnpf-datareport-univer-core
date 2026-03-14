package jnpf.ureport.expression.function;


import jnpf.ureport.build.Context;
import jnpf.ureport.expression.data.ExpressionData;
import jnpf.ureport.model.Cell;

import java.util.List;

/**
 * @author
 * @since 2016年12月27日
 */
public interface Function {
    Object execute(List<ExpressionData> dataList, Context context, Cell currentCell);

    String name();
}
