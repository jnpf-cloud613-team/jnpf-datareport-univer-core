package jnpf.ureport.expression.data;


import jnpf.ureport.build.BindData;

import java.util.List;

/**
 * @author
 * @since 4月28日
 */
public class BindDataListExpressionData implements ExpressionData {
    private List<BindData> list;

    public BindDataListExpressionData(List<BindData> list) {
        this.list = list;
    }

    @Override
    public List<BindData> getData() {
        return list;
    }
}
