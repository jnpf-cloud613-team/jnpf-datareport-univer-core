package jnpf.ureport.expression.condition;


import jnpf.ureport.build.Context;
import jnpf.ureport.model.Cell;

/**
 * @author
 * @since 2016年11月18日
 */
public interface Condition {

    boolean filter(Cell cell, Cell currentCell, Object obj, Context context);
}
