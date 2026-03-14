package jnpf.ureport.expression.assertor;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author
 * @since 1月12日
 */
public class EqualsAssertor extends AbstractAssertor {
    @Override
    public boolean eval(Object left, Object right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        if (left instanceof java.sql.Time) {
            DateTime datetime = DateUtil.parse(right.toString());
            return ((Date) left).compareTo(new java.sql.Time(datetime.hour(true), datetime.minute(), datetime.second())) == 0;
        } else if (left instanceof Date) {
            return ((Date) left).compareTo(DateUtil.parse(right.toString())) == 0;
        } else if (left instanceof Number && right instanceof Number) {
            BigDecimal b1 = new BigDecimal(String.valueOf(left));
            BigDecimal b2 = new BigDecimal(String.valueOf(right));
            return b1.compareTo(b2) == 0;
        } else if (left instanceof Number) {
            BigDecimal b1 = new BigDecimal(String.valueOf(left));
            BigDecimal b2 = null;
            try {
                b2 = new BigDecimal(String.valueOf(right));
            } catch (Exception ex) {
            }
            if (b2 != null) {
                return b1.compareTo(b2) == 0;
            }
        } else if (right instanceof Number) {
            BigDecimal b1 = new BigDecimal(String.valueOf(right));
            BigDecimal b2 = null;
            try {
                b2 = new BigDecimal(String.valueOf(left));
            } catch (Exception ex) {
            }
            if (b2 != null) {
                return b1.compareTo(b2) == 0;
            }
        }
        right = buildObject(right);
        return left.equals(right);
    }
}
