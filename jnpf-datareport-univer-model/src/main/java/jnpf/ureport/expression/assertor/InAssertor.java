package jnpf.ureport.expression.assertor;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;

import java.sql.Time;
import java.util.Date;
import java.util.List;

/**
 * @author
 * @since 1月12日
 */
public class InAssertor implements Assertor {

    @Override
    public boolean eval(Object left, Object right) {
        if (left == null || right == null) {
            return false;
        }
        if (right instanceof List) {
            List<Object> list = (List<Object>) right;
            for (Object obj : list) {
                if (left.equals(obj)) {
                    return true;
                }
            }
            return false;
        } else if (right instanceof Object[]) {
            Object[] objs = (Object[]) right;
            for (Object obj : objs) {
                if (left.equals(obj)) {
                    return true;
                }
            }
            return false;
        } else if (right instanceof String) {
            String[] array = right.toString().split(",");
            for (String str : array) {
                if (left instanceof Time) {
                    DateTime datetime = DateUtil.parse(str);
                    if (((Time) left).compareTo(new Time(datetime.hour(true), datetime.minute(), datetime.second())) == 0) {
                        return true;
                    }
                } else if (left instanceof Date) {
                    if (((Date) left).compareTo(DateUtil.parse(str)) == 0) {
                        return true;
                    }
                } else if (left.equals(str)) {
                    return true;
                }
            }
            return false;
        }
        return left.equals(right);
    }
}
