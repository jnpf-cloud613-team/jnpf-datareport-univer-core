package jnpf.ureport.expression.function;

import jnpf.ureport.build.BindData;
import jnpf.ureport.build.Context;
import jnpf.ureport.expression.data.BindDataListExpressionData;
import jnpf.ureport.expression.data.ExpressionData;
import jnpf.ureport.expression.data.ObjectExpressionData;
import jnpf.ureport.expression.data.ObjectListExpressionData;
import jnpf.ureport.model.Cell;
import jnpf.ureport.utils.DataUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @since 1月20日
 */
public class AvgFunction implements Function {

    @Override
    public Object execute(List<ExpressionData> dataList, Context context, Cell currentCell) {
        List<BigDecimal> list = new ArrayList<>();
        for (ExpressionData exprData : dataList) {
            if (exprData instanceof ObjectListExpressionData) {
                ObjectListExpressionData listExpr = (ObjectListExpressionData) exprData;
                List<Object> objectList = listExpr.getData() != null ? listExpr.getData() : new ArrayList<>();
                for (Object obj : objectList) {
                    if (obj == null || StringUtils.isBlank(obj.toString())) {
                        continue;
                    }
                    BigDecimal bigData = DataUtils.toBigDecimal(obj);
                    if (bigData != null) {
                        list.add(bigData);
                    }
                }
            } else if (exprData instanceof ObjectExpressionData) {
                Object obj = exprData.getData();
                if (obj != null && StringUtils.isNotBlank(obj.toString())) {
                    BigDecimal bigData = DataUtils.toBigDecimal(obj);
                    if (bigData != null) {
                        list.add(bigData);
                    }
                }
            } else if (exprData instanceof BindDataListExpressionData) {
                BindDataListExpressionData bindDataList = (BindDataListExpressionData) exprData;
                List<BindData> objectList = bindDataList.getData();
                for (BindData bindData : objectList) {
                    Object obj = bindData.getValue();
                    if (obj == null || StringUtils.isBlank(obj.toString())) {
                        continue;
                    }
                    BigDecimal bigData = DataUtils.toBigDecimal(obj);
                    if (bigData != null) {
                        list.add(bigData);
                    }
                }
            }
        }
        BigDecimal result = list.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (!list.isEmpty()) {
            result = result.divide(new BigDecimal(list.size()), 8, BigDecimal.ROUND_HALF_UP);
        }
        return result;
    }

    @Override
    public String name() {
        return FunctionType.avg.name();
    }
}
