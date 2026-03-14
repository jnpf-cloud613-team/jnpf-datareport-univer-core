package jnpf.ureport.compute;

import jnpf.ureport.build.BindData;
import jnpf.ureport.build.Context;
import jnpf.ureport.definition.value.ExpressionValue;
import jnpf.ureport.expression.data.BindDataListExpressionData;
import jnpf.ureport.expression.data.ExpressionData;
import jnpf.ureport.expression.expr.Expression;
import jnpf.ureport.model.Cell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @since 2016年12月27日
 */
public class ExpressionValueCompute implements ValueCompute {
    @Override
    public List<BindData> compute(Cell cell, Context context) {
        ExpressionValue exprValue = (ExpressionValue) cell.getValue();
        Expression expr = exprValue.getExpression();
        List<BindData> list = new ArrayList<>();
        if (expr != null) {
            ExpressionData data = expr.execute(cell, cell, context);
            if (data instanceof BindDataListExpressionData) {
                BindDataListExpressionData exprData = (BindDataListExpressionData) data;
                list.addAll(exprData.getData());
            }
            Object obj = data.getData();
            if (obj instanceof List) {
                List<Object> listData = (List<Object>) obj;
                for (Object o : listData) {
                    BindData bindData = new BindData();
                    bindData.setValue(o);
                    list.add(bindData);
                }
            } else {
                if (obj != null) {
                    BindData bindData = new BindData();
                    bindData.setValue(obj);
                    list.add(bindData);
                }
            }
        }
        if (list.isEmpty()) {
            List<Map<String, Object>> rowList = new ArrayList<>();
            rowList.add(new HashMap<>());
            BindData bindData = new BindData();
            bindData.setValue("");
            bindData.setDataList(rowList);
            list.add(bindData);
        }
        return list;
    }

}
