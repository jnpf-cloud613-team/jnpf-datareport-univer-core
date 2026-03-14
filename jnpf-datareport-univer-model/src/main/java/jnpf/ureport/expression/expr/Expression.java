package jnpf.ureport.expression.expr;


import jnpf.ureport.build.Context;
import jnpf.ureport.expression.data.ExpressionData;
import jnpf.ureport.model.Cell;

import java.io.Serializable;

/**
 * @author
 * @since 2016年11月18日
 */
public interface Expression extends Serializable {
    ExpressionData execute(Cell cell, Cell currentCell, Context context);
}
