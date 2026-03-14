package jnpf.ureport.utils;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/12/16 下午3:26
 */
@Setter
@Getter
public class DataComparator implements Comparator<Map<String, Object>> {

    private String order;

    @Override
    public int compare(Map<String, Object> data1, Map<String, Object> data2) {
        if (order == null) {
            return 0;
        }
        Object object1 = data1.get(order);
        Object object2 = data2.get(order);
        if (object1 == null && object2 == null) {
            return 0;
        }
        int comparison = comparison(object1, object2);
        return comparison;
    }

    private int comparison(Object object1, Object object2) {
        Date date1 = null;
        Date date2 = null;
        BigDecimal bigDecimal1 = null;
        BigDecimal bigDecimal2 = null;
        String string1 = null;
        String string2 = null;
        if (object1 instanceof Date && object2 instanceof Date) {
            date1 = (Date) object1;
            date2 = (Date) object2;
        } else if (object1 instanceof Number && object2 instanceof Number) {
            bigDecimal1 = DataUtils.toBigDecimal(object1);
            bigDecimal2 = DataUtils.toBigDecimal(object2);
        } else {
            string1 = String.valueOf(object1);
            string2 = String.valueOf(object2);
        }
        if (date1 != null) {
            return date1.compareTo(date2);
        } else if (bigDecimal1 != null && bigDecimal2 != null) {
            return bigDecimal1.compareTo(bigDecimal2);
        } else {
            if (string1 == null) {
                if (string2 == null) {
                    return 0;
                }
                return 1;
            }
            if (string2 == null) {
                return -1;
            }
            return string1.compareTo(string2);
        }
    }


}
