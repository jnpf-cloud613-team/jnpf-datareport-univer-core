package jnpf.ureport.compute;

import jnpf.ureport.build.BindData;
import jnpf.ureport.build.Context;
import jnpf.ureport.model.Cell;

import java.util.List;

public interface ValueCompute {
    List<BindData> compute(Cell cell, Context context);
}
