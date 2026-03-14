package jnpf.ureport.expression.function;

import jnpf.ureport.build.BindData;
import jnpf.ureport.build.Context;
import jnpf.ureport.expression.data.BindDataListExpressionData;
import jnpf.ureport.expression.data.ExpressionData;
import jnpf.ureport.expression.data.ObjectExpressionData;
import jnpf.ureport.expression.data.ObjectListExpressionData;
import jnpf.ureport.model.Cell;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @since 2016年12月27日
 */
public class CountFunction implements Function {

    @Override
    public Object execute(List<ExpressionData> dataList, Context context, Cell currentCell) {
        List<Object> list = new ArrayList<>();
        for (ExpressionData exprData : dataList) {
            if (exprData instanceof ObjectListExpressionData) {
                ObjectListExpressionData listExpr = (ObjectListExpressionData) exprData;
                List<Object> objectList = listExpr.getData() != null ? listExpr.getData() : new ArrayList<>();
                list.addAll(objectList);
            } else if (exprData instanceof ObjectExpressionData) {
                Object obj = exprData.getData();
                list.add(obj);
            } else if (exprData instanceof BindDataListExpressionData) {
                BindDataListExpressionData bindDataList = (BindDataListExpressionData) exprData;
                List<BindData> objectList = bindDataList.getData();
                list.addAll(objectList);
            }
        }
        return list.size();
    }

    @Override
    public String name() {
        return FunctionType.count.name();
    }
}
