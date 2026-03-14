package jnpf.ureport.expression.expr;


import jnpf.ureport.build.Context;
import jnpf.ureport.expression.data.ExpressionData;
import jnpf.ureport.expression.data.NoneExpressionData;
import jnpf.ureport.expression.data.ObjectExpressionData;
import jnpf.ureport.model.Cell;

import java.util.List;
import java.util.Map;

/**
 * @author
 * @since 7月11日
 */
public class CellDataExpression extends BaseExpression {
    private String property;

    @Override
    protected ExpressionData compute(Cell cell, Cell currentCell, Context context) {
        List<Map<String, Object>> bindDataList = cell.getBindData();
        if (bindDataList == null || bindDataList.size() == 0) {
            return new NoneExpressionData();
        }
        Map<String, Object> obj = bindDataList.get(0);
        Object data = obj.get(property);
        return new ObjectExpressionData(data);
    }

    public void setProperty(String property) {
        this.property = property;
    }
}
