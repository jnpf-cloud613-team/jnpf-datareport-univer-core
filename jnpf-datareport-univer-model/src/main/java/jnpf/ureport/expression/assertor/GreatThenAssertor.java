package jnpf.ureport.expression.assertor;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

/**
 * @author
 * @since 1月12日
 */
public class GreatThenAssertor extends AbstractAssertor {

    @Override
    public boolean eval(Object left, Object right) {
        if (left == null || right == null) {
            return false;
        }
        if (StringUtils.isBlank(left.toString()) || StringUtils.isBlank(right.toString())) {
            return false;
        }
        if (left instanceof java.sql.Time) {
            DateTime datetime = DateUtil.parse(right.toString());
            return ((java.util.Date) left).compareTo(new java.sql.Time(datetime.hour(true), datetime.minute(), datetime.second())) == 1;
        } else if (left instanceof java.util.Date) {
            return ((java.util.Date) left).compareTo(DateUtil.parse(right.toString())) == 1;
        } else if (left instanceof String) {
            return ((String) left).compareTo(right.toString()) == 1;
        } else {
            try {
                BigDecimal leftObj = new BigDecimal(String.valueOf(left));
                right = buildObject(right);
                BigDecimal rightObj = new BigDecimal(String.valueOf(right));
                return leftObj.compareTo(rightObj) == 1;
            } catch (Exception e) {
                return false;
            }
        }
    }
}
