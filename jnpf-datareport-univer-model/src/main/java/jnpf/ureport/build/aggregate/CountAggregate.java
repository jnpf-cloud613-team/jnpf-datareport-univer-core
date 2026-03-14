package jnpf.ureport.build.aggregate;

import jnpf.ureport.build.BindData;
import jnpf.ureport.build.Context;
import jnpf.ureport.definition.value.DatasetValue;
import jnpf.ureport.model.Cell;
import jnpf.ureport.utils.DataUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class CountAggregate extends Aggregate {
    @Override
    public List<BindData> aggregate(DatasetValue expr, Cell cell, Context context) {
        List<Map<String, Object>> objList = DataUtils.fetchData(cell, context, expr.getDatasetName());
        return doAggregate(expr, cell, context, objList);
    }

    protected List<BindData> doAggregate(DatasetValue expr, Cell cell, Context context, List<Map<String, Object>> objList) {
        BindData bindData = new BindData();
        bindData.setValue(objList.size());
        bindData.setDataList(objList);
        List<BindData> list = new ArrayList<>();
        list.add(bindData);
        return list;
    }
}
