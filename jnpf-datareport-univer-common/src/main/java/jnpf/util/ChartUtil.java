package jnpf.util;

import jnpf.univer.chart.UniverChartField;
import jnpf.univer.chart.UniverChartModel;
import jnpf.univer.data.cell.UniverDataConfig;
import jnpf.ureport.definition.value.AggregateType;
import jnpf.ureport.definition.value.DatasetValue;
import jnpf.ureport.utils.DataUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/9/10 下午3:18
 */
public class ChartUtil {

    public static void chart(List<UniverChartModel> chartList, Map<String, List<Map<String, Object>>> dataListAll, List<UniverDataConfig> echartsList) {
        for (UniverDataConfig model : echartsList) {
            UniverChartModel chartModel = new UniverChartModel();
            chartModel.setDrawingId(model.getDrawingId());
            chartModel.setSubUnitId(model.getSubUnitId());
            chartModel.setUnitId(model.getUnitId());
            //数据
            Set<String> datasetName = new HashSet<>();
            String classifyNameField = model.getClassifyNameField();
            model.setClassifyNameField(fieldName(classifyNameField, datasetName));

            String seriesNameField = model.getSeriesNameField();
            model.setSeriesNameField(fieldName(seriesNameField, datasetName));

            String seriesDataField = model.getSeriesDataField();
            model.setSeriesDataField(fieldName(seriesDataField, datasetName));

            String maxField = model.getMaxField();
            model.setMaxField(fieldName(maxField, datasetName));

            List<Map<String, Object>> dataList = new ArrayList<>();
            for (String dataName : datasetName) {
                List<Map<String, Object>> data = dataListAll.get(dataName) != null ? dataListAll.get(dataName) : new ArrayList<>();
                dataList.addAll(data);
            }
            UniverChartField chartField = chart(dataList, model);
            chartModel.setField(chartField);
            chartList.add(chartModel);
        }
    }

    private static UniverChartField chart(List<Map<String, Object>> dataList, UniverDataConfig dataConfig) {
        String classifyName = dataConfig.getClassifyNameField();
        String seriesName = dataConfig.getSeriesNameField();
        String seriesData = dataConfig.getSeriesDataField();
        String maxField = dataConfig.getMaxField();
        Map<Object, Map<Object, List<Object>>> chartDataMap = new HashMap<>();
        Map<Object, List<Object>> maxDatMap = new HashMap<>();
        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> data = dataList.get(i);
            Object classify = data.get(classifyName);
            if (classify == null) {
                continue;
            }
            Object value = data.get(seriesData);
            if (value == null) {
                continue;
            }
            Object series = data.get(seriesName) != null ? data.get(seriesName) : "";
            Map<Object, List<Object>> categoryMap = chartDataMap.get(series) != null ? chartDataMap.get(series) : new HashMap<>();
            List<Object> valueList = categoryMap.get(classify) != null ? categoryMap.get(classify) : new ArrayList<>();
            valueList.add(value);
            categoryMap.put(classify, valueList);
            chartDataMap.put(series, categoryMap);

            Object max = data.get(maxField);
            if (max != null) {
                List<Object> maxData = maxDatMap.get(classify) != null ? maxDatMap.get(classify) : new ArrayList<>();
                maxData.add(max);
                maxDatMap.put(classify, maxData);
            }
        }
        return chartData(chartDataMap, maxDatMap, dataConfig);
    }

    private static UniverChartField chartData(Map<Object, Map<Object, List<Object>>> seriesDataMap, Map<Object, List<Object>> maxDataMap, UniverDataConfig dataConfig) {
        UniverChartField chartField = new UniverChartField();
        List<String> seriesNameList = new ArrayList<>();
        Map<Object, List<List<String>>> classifyMap = new HashMap<>();
        List<Integer> maxCount = new ArrayList<>();
        maxCount.add(0);
        for (Object series : seriesDataMap.keySet()) {
            seriesNameList.add(String.valueOf(series));
            Map<Object, List<Object>> classifyNameMap = seriesDataMap.get(series);
            for (Object classify : classifyNameMap.keySet()) {
                List<List<String>> categroyList = new ArrayList<>();
                if (classifyMap.get(classify) != null) {
                    categroyList.addAll(classifyMap.get(classify));
                }
                List<Object> valueList = classifyNameMap.get(classify);
                List<String> data = data(valueList, dataConfig);
                for (int i = 0; i < data.size(); i++) {
                    List<String> classifyData = new ArrayList<>();
                    classifyData.add(data.get(i));
                    categroyList.add(classifyData);
                }
                classifyMap.put(classify, categroyList);
                maxCount.add(categroyList.size());
            }
        }
        if (StringUtil.isNotEmpty(dataConfig.getSeriesNameField())) {
            chartField.setSeriesNameField(seriesNameList);
        }
        List<String> maxFieldList = new ArrayList<>();
        List<String> classifyNameList = new ArrayList<>();
        for (Object classify : classifyMap.keySet()) {
            classifyNameList.add(String.valueOf(classify));
            List<Object> objects = maxDataMap.get(classify) != null ? maxDataMap.get(classify) : new ArrayList<>();
            if (objects.isEmpty()) {
                objects.add(new BigDecimal(0));
            }
            dataConfig.setSummaryType(AggregateType.max.name());
            List<String> data = data(objects, dataConfig);
            maxFieldList.add(data.get(0));
        }
        List<List<String>> seriesDataList = new ArrayList<>();
        for (int i = 0; i < Collections.max(maxCount); i++) {
            List<String> seriesData = new ArrayList<>();
            for (int k = 0; k < classifyNameList.size(); k++) {
                String category = classifyNameList.get(k);
                List<List<String>> categoryList = classifyMap.get(category) != null ? classifyMap.get(category) : new ArrayList<>();
                List<String> categoryData = categoryList.size() - 1 >= i ? categoryList.get(i) : new ArrayList<>();
                String data = categoryData.size() > 0 ? categoryData.get(0) : "";
                seriesData.add(data);
            }
            seriesDataList.add(seriesData);
        }
        chartField.setClassifyNameField(classifyNameList);
        chartField.setSeriesDataField(seriesDataList);
        if (StringUtil.isNotEmpty(dataConfig.getMaxField())) {
            chartField.setMaxField(maxFieldList);
        }
        return chartField;
    }

    private static List<String> data(List<Object> list, UniverDataConfig dataConfig) {
        List<String> result = new ArrayList<>();
        if (list.isEmpty()) {
            return result;
        }
        String chartType = AggregateType.value(dataConfig.getSummaryType()) != null ? dataConfig.getSummaryType() : AggregateType.select.name();
        DatasetValue expr = new DatasetValue();
        expr.setProperty(chartType);
        AggregateType value = AggregateType.value(chartType);
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (Object data : list) {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put(chartType, data);
            dataList.add(dataMap);
        }
        List<BigDecimal> bindDataList = DataUtils.dataList(expr, dataList);
        switch (value) {
            case sum:
                result.add(bindDataList.stream().reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue() + "");
                break;
            case avg:
                BigDecimal sumDecimal = bindDataList.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                if (bindDataList.isEmpty()) {
                    bindDataList.add(new BigDecimal(0));
                }
                result.add(sumDecimal.divide(new BigDecimal(bindDataList.size()), 8, BigDecimal.ROUND_HALF_UP).doubleValue() + "");
                break;
            case max:
                if (bindDataList.isEmpty()) {
                    bindDataList.add(new BigDecimal(0));
                }
                result.add(bindDataList.stream().reduce(bindDataList.get(0), BigDecimal::max).doubleValue() + "");
                break;
            case min:
                if (bindDataList.isEmpty()) {
                    bindDataList.add(new BigDecimal(0));
                }
                result.add(bindDataList.stream().reduce(bindDataList.get(0), BigDecimal::min).doubleValue() + "");
                break;
            case count:
                result.add(list.size() + "");
                break;
            default:
                for (Object data : list) {
                    result.add(String.valueOf(data));
                }
                break;
        }
        return result;
    }

    private static String fieldName(String field, Set<String> datasetName) {
        String fieldName = field;
        if (StringUtil.isNotEmpty(fieldName)) {
            String[] params = fieldName.split("\\.");
            datasetName.add(params[0]);
            fieldName = params.length == 2 ? params[1] : fieldName;
        }
        return fieldName;
    }
}
