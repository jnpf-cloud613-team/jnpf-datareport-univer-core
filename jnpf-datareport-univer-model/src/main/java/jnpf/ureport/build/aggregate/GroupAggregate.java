package jnpf.ureport.build.aggregate;

import jnpf.enums.UniverDataEnum;
import jnpf.ureport.build.BindData;
import jnpf.ureport.build.Context;
import jnpf.ureport.definition.value.DatasetValue;
import jnpf.ureport.model.Cell;
import jnpf.ureport.utils.DataUtils;

import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;


public class GroupAggregate extends Aggregate {
    @Override
    public List<BindData> aggregate(DatasetValue expr, Cell cell, Context context) {
        List<Map<String, Object>> objList = DataUtils.fetchData(cell, context, expr.getDatasetName());
        return aggregate(expr, objList);
    }

    protected List<BindData> aggregate(DatasetValue expr, List<Map<String, Object>> dataList) {
        String groupType = expr.getGroupType();
        String property = expr.getProperty();
//        List<Map<String, Object>> dataList = expr.getSort()==null?objList:DataUtils.orderDataList(objList, expr.getSort());
        List<BindData> list = Objects.equals(UniverDataEnum.adjacent.getName(), groupType) ? doAdjacentAggregate(expr, dataList) : doAggregate(expr, dataList);
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

    protected List<BindData> doAggregate(DatasetValue expr, List<Map<String, Object>> dataList) {
        List<BindData> list = new ArrayList<>();
        String property = expr.getProperty();
        Map<Object, List<Map<String, Object>>> map = new LinkedHashMap<>();
        for (Map<String, Object> obj : dataList) {
            List<Map<String, Object>> rowList = new ArrayList<>();
            Object value = obj.get(property) != null ? obj.get(property) : "";
            if (map.containsKey(value)) {
                rowList = map.get(value);
            }
            rowList.add(obj);
            map.put(value, rowList);
        }
        for (Object key : map.keySet()) {
            BindData bindData = new BindData();
            bindData.setValue(key);
            bindData.setDataList(map.get(key));
            list.add(bindData);
        }
        return list;
    }

    protected List<BindData> doAdjacentAggregate(DatasetValue expr, List<Map<String, Object>> objList) {
        List<BindData> list = new ArrayList<>();
        String property = expr.getProperty();
        Map<Integer, List<Map<String, Object>>> map = new HashMap<>();
        Map<Integer, Object> mapValue = new HashMap<>();
        int num = 0;
        Object data = "";
        for (int i = 0; i < objList.size(); i++) {
            Map<String, Object> obj = objList.get(i);
            Object value = obj.get(property) != null ? obj.get(property) : "";
            if (!Objects.equals(value, data)) {
                data = value;
                num = i;
            }
            List<Map<String, Object>> rowList = map.get(num) != null ? map.get(num) : new ArrayList<>();
            rowList.add(obj);
            map.put(num, rowList);
            mapValue.put(num, value);
        }
        List<Integer> numList = new ArrayList<>(mapValue.keySet()).stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
        for (Integer key : numList) {
            BindData bindData = new BindData();
            bindData.setValue(mapValue.get(key));
            bindData.setDataList(map.get(key));
            list.add(bindData);
        }
        return list;
    }

}
