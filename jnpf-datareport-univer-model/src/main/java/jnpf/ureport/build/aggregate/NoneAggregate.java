package jnpf.ureport.build.aggregate;

import jnpf.ureport.build.BindData;
import jnpf.ureport.build.Context;
import jnpf.ureport.definition.value.DatasetValue;
import jnpf.ureport.model.Cell;
import jnpf.ureport.utils.DataUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/9/13 下午5:17
 */
public class NoneAggregate extends Aggregate {

    @Override
    public List<BindData> aggregate(DatasetValue expr, Cell cell, Context context) {
        List<Map<String, Object>> objList = DataUtils.fetchData(cell, context, expr.getDatasetName());
        return doAggregate(expr, cell, context, objList);
    }

    protected List<BindData> doAggregate(DatasetValue expr, Cell cell, Context context, List<Map<String, Object>> objList) {
        List<String> dataList = new ArrayList<>();
        String property = expr.getProperty();
        List<Map<String, Object>> newObjList = DataUtils.orderDataList(objList, property);
        for (Map<String, Object> o : objList) {
            Object data = o.get(property);
            dataList.add(data != null ? data.toString() : "");
        }
        BindData bindData = new BindData();
        bindData.setDataList(newObjList);
        bindData.setValue(String.join(",", dataList));
        List<BindData> list = new ArrayList<>();
        list.add(bindData);
        return list;
    }


}
