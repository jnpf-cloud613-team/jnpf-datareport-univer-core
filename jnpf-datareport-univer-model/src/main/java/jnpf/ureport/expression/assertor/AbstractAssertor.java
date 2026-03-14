package jnpf.ureport.expression.assertor;

import java.util.List;

/**
 * @author
 * @since 9月15日
 */
public abstract class AbstractAssertor implements Assertor {
    protected Object buildObject(Object obj) {
        if (obj == null) {
            return obj;
        }
        if (obj instanceof List) {
            List<Object> list = (List<Object>) obj;
            if (list.size() == 1) {
                return list.get(0);
            }
        }
        return obj;
    }
}
