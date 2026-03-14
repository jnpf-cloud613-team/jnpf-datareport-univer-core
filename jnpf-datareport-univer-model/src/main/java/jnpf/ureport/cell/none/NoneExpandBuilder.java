package jnpf.ureport.cell.none;

import jnpf.ureport.build.BindData;
import jnpf.ureport.build.Context;
import jnpf.ureport.cell.CellBuilder;
import jnpf.ureport.model.Cell;

import java.util.List;
import java.util.Map;


public class NoneExpandBuilder implements CellBuilder {

    @Override
    public Cell buildCell(List<BindData> dataList, Cell cell, Context context) {
        if (dataList.size() == 1) {
            BindData bindData = dataList.get(0);
            cell.setData(bindData.getValue());
            cell.setBindData(bindData.getDataList());
        } else {
            Object obj = null;
            List<Map<String, Object>> bindData = null;
            for (BindData data : dataList) {
                if (obj == null) {
                    obj = data.getValue();
                } else {
                    obj = obj + "," + data.getValue();
                }
                bindData = data.getDataList();
            }
            cell.setData(obj);
            cell.setBindData(bindData);
        }
        return cell;
    }
}
