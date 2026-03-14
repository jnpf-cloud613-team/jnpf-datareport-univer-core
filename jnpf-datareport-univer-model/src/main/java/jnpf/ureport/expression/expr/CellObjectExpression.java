package jnpf.ureport.expression.expr;


import jnpf.ureport.build.Context;
import jnpf.ureport.expression.data.ExpressionData;
import jnpf.ureport.expression.data.ObjectExpressionData;
import jnpf.ureport.model.Cell;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * @author
 * @since 1月20日
 */
public class CellObjectExpression extends BaseExpression {
    private String property;

    public CellObjectExpression(String property) {
        this.property = property;
    }

    @Override
    protected ExpressionData compute(Cell cell, Cell currentCell, Context context) {
        while (!context.isCellPocessed(cell.getName())) {
            context.getReportBuilder().buildCell(context, null);
        }
        if (StringUtils.isNotBlank(property)) {
            List<Map<String, Object>> bindData = cell.getBindData();
            Object obj = null;
            if (bindData != null && bindData.size() > 0) {
                obj = bindData.get(0).get(property);
            }
            return new ObjectExpressionData(obj);
        }
        return new ObjectExpressionData(cell);
    }


}
