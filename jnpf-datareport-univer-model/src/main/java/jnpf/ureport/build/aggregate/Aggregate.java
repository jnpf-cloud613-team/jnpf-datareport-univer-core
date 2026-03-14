package jnpf.ureport.build.aggregate;

import jnpf.ureport.build.BindData;
import jnpf.ureport.build.Context;
import jnpf.ureport.definition.value.DatasetValue;
import jnpf.ureport.model.Cell;

import java.util.*;

/**
 * @author
 * @since 2016年12月21日
 */
public abstract class Aggregate {

    public abstract List<BindData> aggregate(DatasetValue expr, Cell cell, Context context);

}
