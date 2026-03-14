package jnpf.ureport.expression.assertor;


import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;

import java.util.Date;

/**
 * @author
 * @since 1月12日
 */
public class NotEqualsAssertor extends AbstractAssertor {

    @Override
    public boolean eval(Object left, Object right) {
        if (left == null && right == null) {
            return false;
        }
        if (left == null || right == null) {
            return true;
        }
        if (left instanceof java.sql.Time) {
            DateTime datetime = DateUtil.parse(right.toString());
            return ((Date) left).compareTo(new java.sql.Time(datetime.hour(true), datetime.minute(), datetime.second())) == 0;
        } else if (left instanceof Date) {
            return ((Date) left).compareTo(DateUtil.parse(right.toString())) == 0;
        }
        right = buildObject(right);
        return !left.equals(right);
    }
}
