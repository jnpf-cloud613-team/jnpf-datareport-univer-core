package jnpf.ureport.expression.assertor;

/**
 * @author
 * @since 1月12日
 */
public class LikeAssertor implements Assertor {

    @Override
    public boolean eval(Object left, Object right) {
        if (left == null || right == null) {
            return false;
        }
        if (left.equals(right)) {
            return true;
        }
        return left.toString().indexOf(right.toString()) > -1;
    }

}
