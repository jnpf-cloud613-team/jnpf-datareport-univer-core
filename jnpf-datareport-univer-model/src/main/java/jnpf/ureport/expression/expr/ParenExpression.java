package jnpf.ureport.expression.expr;


import jnpf.ureport.expression.util.Operator;

import java.util.List;


/**
 * @author
 * @since 2016年11月18日
 */
public class ParenExpression extends JoinExpression {

    public ParenExpression(List<Operator> operators, List<BaseExpression> expressions) {
        super(operators, expressions);
    }
}
