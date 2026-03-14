package jnpf.ureport.cell;

import jnpf.ureport.build.BindData;
import jnpf.ureport.build.Context;
import jnpf.ureport.model.Cell;

import java.util.List;

public interface CellBuilder {
    Cell buildCell(List<BindData> dataList, Cell cell, Context context);
}
