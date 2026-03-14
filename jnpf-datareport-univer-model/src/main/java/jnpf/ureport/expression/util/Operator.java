package jnpf.ureport.expression.util;

/**
 * @author
 * @since 2016年11月18日
 */
public enum Operator {
    Add, Subtract, Multiply, Divide, Complementation;

    public static Operator parse(String op) {
        if (op.equals("+")) {
            return Add;
        } else if (op.equals("-")) {
            return Subtract;
        } else if (op.equals("*")) {
            return Multiply;
        } else if (op.equals("/")) {
            return Divide;
        } else if (op.equals("%")) {
            return Complementation;
        }
        return Add;
    }

    @Override
    public String toString() {
        switch (this) {
            case Add:
                return "+";
            case Divide:
                return "/";
            case Multiply:
                return "*";
            case Subtract:
                return "-";
            case Complementation:
                return "%";
            default:
                return "+";
        }
    }

}
