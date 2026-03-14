package jnpf.ureport.definition.value;

import jnpf.ureport.expression.expr.Expression;
import jnpf.ureport.expression.util.ExpressionUtils;
import lombok.Getter;
import lombok.Setter;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/10/31 下午5:48
 */
@Setter
@Getter
public class ExpressionValue {

    private String text;
    private Expression expression;

    public ExpressionValue(String text) {
        this.text = text;
        expression = ExpressionUtils.parseExpression(text);
    }

}
