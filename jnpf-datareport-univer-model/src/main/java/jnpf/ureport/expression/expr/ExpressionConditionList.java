package jnpf.ureport.expression.expr;


import jnpf.ureport.build.Context;
import jnpf.ureport.model.Cell;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author
 * @since 1月16日
 */
@Getter
@Setter
public class ExpressionConditionList {
    private List<ExpressionCondition> conditions;
    private List<Join> joins;

    public ExpressionConditionList(List<ExpressionCondition> expressionConditions, List<Join> joins) {
        this.conditions = expressionConditions;
        this.joins = joins;
    }

    public boolean eval(Context context, Cell cell, Cell currentCell) {
        if (conditions.size() == 1) {
            return conditions.get(0).eval(context, cell, currentCell);
        }
        for (int i = 0; i < conditions.size(); i++) {
            ExpressionCondition expressionCondition = conditions.get(i);
            boolean result = expressionCondition.eval(context, cell, currentCell);
            Join join = null;
            if (i < joins.size()) {
                join = joins.get(i);
            }
            if (join == null) {
                return result;
            } else {
                if (join.equals(Join.and) && !result) {
                    return false;
                }
                if (join.equals(Join.or) && result) {
                    return true;
                }
            }
        }
        return true;
    }
}
