package jnpf.ureport.expression.expr;

/**
 * @author
 * @since 2016年12月1日
 */
public enum Join {
    and, or;

    public static Join parse(String join) {
        if (join.equals("and") || join.equals("&&")) {
            return and;
        }
        return or;
    }
}
