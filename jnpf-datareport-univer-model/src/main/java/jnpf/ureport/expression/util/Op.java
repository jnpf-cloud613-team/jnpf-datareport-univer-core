package jnpf.ureport.expression.util;

import java.util.Objects;

/**
 * @author
 * @since 2016年11月22日
 */
public enum Op {
    GreatThen, EqualsGreatThen, LessThen, EqualsLessThen, Equals, NotEquals, In, NotIn, Like;

    public static Op parse(String op) {
        return getOp(op);
    }

    public static Op getOp(String op) {
        for (Op status : Op.values()) {
            if (Objects.equals(status.name(), op.trim())) {
                return status;
            }
        }
        return Op.Equals;
    }

    @Override
    public String toString() {
        switch(this){
            case GreatThen:
                return ">";
            case EqualsGreatThen:
                return ">=";
            case LessThen:
                return "<";
            case EqualsLessThen:
                return "<=";
            case Equals:
                return "==";
            case NotEquals:
                return "!=";
            case In:
                return " in ";
            case NotIn:
                return " not in ";
            case Like:
                return " like ";
        }
        return super.toString();
    }

}
