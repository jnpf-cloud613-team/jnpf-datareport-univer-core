package jnpf.ureport.build.aggregate;

import jnpf.ureport.build.BindData;
import jnpf.ureport.build.Context;
import jnpf.ureport.definition.value.DatasetValue;
import jnpf.ureport.model.Cell;
import jnpf.ureport.utils.DataUtils;

import java.util.*;


public class SelectAggregate extends Aggregate {
    @Override
    public List<BindData> aggregate(DatasetValue expr, Cell cell, Context context) {
        List<Map<String, Object>> objList = DataUtils.fetchData(cell, context, expr.getDatasetName());
        return doAggregate(expr, cell, context, objList);
    }

    protected List<BindData> doAggregate(DatasetValue expr, Cell cell, Context context, List<Map<String, Object>> dataList) {
        List<BindData> list = new ArrayList<>();
        String property = expr.getProperty();
//        List<Map<String, Object>> dataList = expr.getSort()==null?DataUtils.orderDataList(objList, property):DataUtils.orderDataList(objList, expr.getSort());
        for (Map<String, Object> o : dataList) {
            List<Map<String, Object>> bindList = new ArrayList<>();
            bindList.add(o);
            Object data = o.get(property);
            BindData bindData = new BindData();
            bindData.setDataList(bindList);
            bindData.setValue(data);
            list.add(bindData);
        }
        if (list.isEmpty()) {
            List<Map<String, Object>> rowList = new ArrayList<>();
            rowList.add(new HashMap<>());
            BindData bindData = new BindData();
            bindData.setValue("");
            bindData.setDataList(rowList);
            list.add(bindData);
        }
        return list;
    }
}
