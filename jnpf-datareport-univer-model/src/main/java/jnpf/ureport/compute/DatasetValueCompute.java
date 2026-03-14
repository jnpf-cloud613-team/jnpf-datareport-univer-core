package jnpf.ureport.compute;

import jnpf.ureport.build.BindData;
import jnpf.ureport.build.Context;
import jnpf.ureport.build.DatasetUtils;
import jnpf.ureport.definition.value.DatasetValue;
import jnpf.ureport.model.Cell;

import java.util.List;


public class DatasetValueCompute implements ValueCompute {
    @Override
    public List<BindData> compute(Cell cell, Context context) {
        DatasetValue expr = (DatasetValue) cell.getValue();
        return DatasetUtils.computeDatasetExpression(expr, cell, context);
    }

}
