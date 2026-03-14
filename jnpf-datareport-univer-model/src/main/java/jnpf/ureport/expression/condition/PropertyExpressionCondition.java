package jnpf.ureport.expression.condition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jnpf.ureport.build.Context;
import jnpf.ureport.expression.data.ExpressionData;
import jnpf.ureport.expression.expr.Expression;
import jnpf.ureport.model.Cell;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @author
 * @since 2016年11月22日
 */
@Setter
@Getter
public class PropertyExpressionCondition extends BaseCondition {
    private ConditionType type = ConditionType.property;
    @JsonIgnore
    private String leftProperty;
    @JsonIgnore
    private Expression rightExpression;

    @Override
    Object computeLeft(Cell cell, Cell currentCell, Object obj, Context context) {
        Object data = cell.getData();
        try {
            if (StringUtils.isNotBlank(leftProperty) && obj instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) obj;
                data = map.get(leftProperty);
            }
        } catch (Exception e) {

        }
        return data;
    }

    @Override
    Object computeRight(Cell cell, Cell currentCell, Object obj, Context context) {
        ExpressionData exprData = rightExpression.execute(cell, currentCell, context);
        return extractExpressionData(exprData);
    }

    @Override
    public ConditionType getType() {
        return type;
    }

}
