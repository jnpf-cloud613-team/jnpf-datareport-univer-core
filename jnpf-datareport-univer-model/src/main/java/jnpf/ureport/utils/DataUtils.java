package jnpf.ureport.utils;

import jnpf.ureport.build.Context;
import jnpf.ureport.definition.value.DatasetValue;
import jnpf.ureport.definition.value.Value;
import jnpf.ureport.model.Cell;
import jnpf.ureport.model.Report;
import jnpf.util.StringUtil;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


public class DataUtils {

    public static List<Map<String, Object>> fetchData(Cell cell, Context context, String datasetName) {
        Cell leftCell = fetchLeftCell(cell, context, datasetName);
        Cell topCell = fetchTopCell(cell, context, datasetName);
        List<Map<String, Object>> leftList = null;
        List<Map<String, Object>> topList = null;
        if (leftCell != null) {
            leftList = leftCell.getBindData();
        }
        if (topCell != null) {
            topList = topCell.getBindData();
        }
        if (leftList == null && topList == null) {
            List<Map<String, Object>> data = context.getDatasetData(datasetName);
            return data;
        } else if (leftList == null) {
            return topList;
        } else if (topList == null) {
            return leftList;
        } else {
            String leftProp = ((DatasetValue) leftCell.getValue()).getProperty();
            Object leftValue = leftCell.getData();
            String topProp = ((DatasetValue) topCell.getValue()).getProperty();
            Object topValue = topCell.getData();
            List<Map<String, Object>> topDataList = new ArrayList<>();
            for (Map<String, Object> data : topList) {
                if (Objects.equals(data.get(leftProp), leftValue)) {
                    topDataList.add(data);
                }
            }
            List<Map<String, Object>> leftDataList = new ArrayList<>();
            for (Map<String, Object> data : leftList) {
                if (Objects.equals(data.get(topProp), topValue)) {
                    leftDataList.add(data);
                }
            }
            List<Map<String, Object>> result = leftDataList.size() > topDataList.size() ? topDataList : leftDataList;
            return result;
        }
    }

    public static Cell fetchLeftCell(Cell cell, Context context, String datasetName) {
        Cell targetCell = null;
        Cell parentCell = cell.getLeftParentCell();
        if (parentCell != null) {
            Value cellValue = parentCell.getValue();
            if (cellValue instanceof DatasetValue) {
                DatasetValue datasetValue = (DatasetValue) cellValue;
                if (datasetValue != null) {
                    String dataName = datasetValue.getDatasetName();
                    if (Objects.equals(dataName, datasetName)) {
                        if (parentCell.getBindData() != null) {
                            targetCell = parentCell;
                        }
                    }
                }
            }
        }
        return targetCell;
    }

    public static Cell fetchTopCell(Cell cell, Context context, String datasetName) {
        Cell targetCell = null;
        Cell parentCell = cell.getTopParentCell();
        if (parentCell != null) {
            Value cellValue = parentCell.getValue();
            if (cellValue instanceof DatasetValue) {
                DatasetValue datasetValue = (DatasetValue) cellValue;
                if (datasetValue != null) {
                    String dataName = datasetValue.getDatasetName();
                    if (Objects.equals(dataName, datasetName)) {
                        if (parentCell.getBindData() != null) {
                            targetCell = parentCell;
                        }
                    }
                }
            }
        }
        return targetCell;
    }

    private static void leftList(Cell cell, List<Cell> list) {
        if (cell != null) {
            list.add(cell);
            leftList(cell.getLeftParentCell(), list);
        }
    }

    private static void topList(Cell cell, List<Cell> list) {
        if (cell != null) {
            list.add(cell);
            topList(cell.getTopParentCell(), list);
        }
    }

    public static void cellList(Cell cell, boolean left, boolean top) {
        if (cell != null) {
            if (left && cell.getLeftParentCell() != null) {
                List<Cell> leftList = new ArrayList<>();
//                leftList(cell.getLeftParentCell(), leftList);
                cell.setLeftParentList(leftList);
            }
            if (top && cell.getTopParentCell() != null) {
                List<Cell> topList = new ArrayList<>();
//                topList(cell.getTopParentCell(), topList);
                cell.setTopParentList(topList);
            }
        }
    }

    public static List<BigDecimal> dataList(DatasetValue expr, List<Map<String, Object>> objList) {
        List<BigDecimal> result = new ArrayList<>();
        String property = expr.getProperty();
        for (Map<String, Object> o : objList) {
            Object data = o.get(property);
            try {
                BigDecimal bigDecimal = new BigDecimal(String.valueOf(data));
                result.add(bigDecimal);
            } catch (Exception e) {
            }
        }
        return result;
    }

    public static List<Cell> fetchTargetCells(Cell cell, Context context, String cellName) {
        while (!context.isCellPocessed(cellName)) {
            context.getReportBuilder().buildCell(context, null);
        }
        List<Cell> leftCells = fetchCellsByLeftParent(context, cell, cellName);
        List<Cell> topCells = fetchCellsByTopParent(context, cell, cellName);
        if (leftCells != null && topCells != null) {
            int leftSize = leftCells.size();
            int topSize = topCells.size();
            if (leftSize == 1 || topSize == 0) {
                return leftCells;
            }
            if (topSize == 1 || leftSize == 0) {
                return topCells;
            }
            List<Cell> list = new ArrayList<>(CollectionUtils.intersection(leftCells, topCells));
            return list;
        } else if (leftCells != null && topCells == null) {
            return leftCells;
        } else if (leftCells == null && topCells != null) {
            return topCells;
        } else {
            Report report = context.getReport();
            return report.getCellsMap().get(cellName);
        }
    }

    private static List<Cell> fetchCellsByLeftParent(Context context, Cell cell, String cellName) {
        Cell leftParentCell = cell.getLeftParentCell();
        if (leftParentCell == null) {
            return null;
        }
        if (leftParentCell.getName().equals(cellName)) {
            List<Cell> list = new ArrayList<>();
            list.add(leftParentCell);
            return list;
        }
        //todo
//        Map<String, List<Cell>> childrenCellsMap = leftParentCell.getRowChildrenCellsMap();
//        List<Cell> targetCells = childrenCellsMap.get(cellName);
//        if (targetCells != null) {
//            return targetCells;
//        }
        return fetchCellsByLeftParent(context, leftParentCell, cellName);
    }

    private static List<Cell> fetchCellsByTopParent(Context context, Cell cell, String cellName) {
        Cell topParentCell = cell.getTopParentCell();
        if (topParentCell == null) {
            return null;
        }
        if (topParentCell.getName().equals(cellName)) {
            List<Cell> list = new ArrayList<>();
            list.add(topParentCell);
            return list;
        }
        //todo
//        Map<String, List<Cell>> childrenCellsMap = topParentCell.getColumnChildrenCellsMap();
//        List<Cell> targetCells = childrenCellsMap.get(cellName);
//        if (targetCells != null) {
//            return targetCells;
//        }
        return fetchCellsByTopParent(context, topParentCell, cellName);
    }

    public static BigDecimal toBigDecimal(Object obj) {
        BigDecimal bigDecimal = null;
        if (obj == null) {
            return bigDecimal;
        }
        if (obj instanceof BigDecimal) {
            bigDecimal = (BigDecimal) obj;
        } else if (obj instanceof String) {
            if (obj.toString().trim().equals("")) {
                bigDecimal = new BigDecimal(0);
            }
            try {
                String str = obj.toString().trim();
                bigDecimal = new BigDecimal(str);
            } catch (Exception ex) {

            }
        } else if (obj instanceof Number) {
            Number n = (Number) obj;
            bigDecimal = new BigDecimal(n.doubleValue());
        }
        return bigDecimal;
    }

    public static List<Map<String, Object>> orderDataList(List<Map<String, Object>> list, String order) {
        List<Map<String, Object>> dataList = new ArrayList<>(list);
        if (StringUtil.isEmpty(order)) {
            return dataList;
        }
        Map<Integer, Comparator<Map<String, Object>>> comparatorMap = getComparatorMap(order);
        Comparator<Map<String, Object>> comparator = null;
        List<Integer> comparatorList = new ArrayList<>(comparatorMap.keySet()).stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
        for (int i = 0; i < comparatorList.size(); i++) {
            comparator = i == 0 ? comparatorMap.get(i) : comparator.thenComparing(comparatorMap.get(i));
        }
        if (comparator != null) {
            dataList.sort(comparator);
        }
        return dataList;
    }

    private static Map<Integer, Comparator<Map<String, Object>>> getComparatorMap(String order) {
        List<String> orderList = Arrays.asList(order.split(","));
        Map<Integer, Comparator<Map<String, Object>>> comparatorMap = new HashMap<>();
        for (int i = 0; i < orderList.size(); i++) {
            String vModel = orderList.get(i);
            boolean isDesc = vModel.startsWith("-");
            if (isDesc) {
                vModel = vModel.substring(1);
            }
            DataComparator dataComparator = new DataComparator();
            dataComparator.setOrder(vModel);
            Comparator<Map<String, Object>> comparator = isDesc ? dataComparator.reversed() : dataComparator;
            comparatorMap.put(i, comparator);
        }
        return comparatorMap;
    }

}
