package jnpf.ureport.expression.data;

/**
 * @author
 * @since 1月1日
 */
public class ObjectExpressionData implements ExpressionData {
    private Object data;

    public ObjectExpressionData(Object data) {
        this.data = data;
    }

    @Override
    public Object getData() {
        return data;
    }
}
