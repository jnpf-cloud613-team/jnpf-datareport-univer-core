package jnpf.ureport.expression.data;

import java.util.List;

/**
 * @author
 * @since 1月3日
 */
public class ObjectListExpressionData implements ExpressionData {
    private List<Object> list;

    public ObjectListExpressionData(List<Object> list) {
        this.list = list;
    }

    @Override
    public List<Object> getData() {
        return list;
    }
}
