package jnpf.ureport.compute;

import jnpf.ureport.build.BindData;
import jnpf.ureport.build.Context;
import jnpf.ureport.model.Cell;

import java.util.ArrayList;
import java.util.List;


public class SimpleValueCompute implements ValueCompute {

    @Override
    public List<BindData> compute(Cell cell, Context context) {
        List<BindData> list = new ArrayList<>();
        BindData bindData = new BindData();
        bindData.setValue(cell.getValue().getValue());
        list.add(bindData);
        return list;
    }

}
