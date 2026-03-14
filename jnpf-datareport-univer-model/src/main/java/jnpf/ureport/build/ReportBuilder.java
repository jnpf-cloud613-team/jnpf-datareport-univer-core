package jnpf.ureport.build;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.ImmutableList;
import jnpf.univer.sheet.UniverSheetCellData;
import jnpf.ureport.cell.CellBuilder;
import jnpf.ureport.cell.down.DownExpandBuilder;
import jnpf.ureport.cell.none.NoneExpandBuilder;
import jnpf.ureport.cell.right.RightExpandBuilder;
import jnpf.ureport.definition.Expand;
import jnpf.ureport.definition.ReportDefinition;
import jnpf.ureport.definition.value.*;
import jnpf.ureport.model.Cell;
import jnpf.ureport.model.Column;
import jnpf.ureport.model.Report;
import jnpf.ureport.model.Row;

import java.util.*;
import java.util.stream.Collectors;


public class ReportBuilder {
    public static final String DATASET = "dataset";
    private final Map<Expand, CellBuilder> cellBuildersMap = new HashMap<>();

    public ReportBuilder() {
        cellBuildersMap.put(Expand.Down, new DownExpandBuilder());
        cellBuildersMap.put(Expand.None, new NoneExpandBuilder());
        cellBuildersMap.put(Expand.Right, new RightExpandBuilder());
    }

    public Report buildReport(ReportDefinition reportDefinition,Map<String, String> sortInMap) {
        Report report = reportDefinition.newReport();
        Map<String, Integer> fillBlankRowsMap = reportDefinition.getFillBlankRowsMap();
        Map<String, Dataset> datasetMap = reportDefinition.getDatasetMap();
        Context context = new Context(this, report, datasetMap);
        List<Cell> cells = new ArrayList<>();
        cells.add(report.getRootCell());
        do {
            buildCell(context, cells, fillBlankRowsMap,sortInMap);
            cells = context.nextUnprocessedCells();
        } while (cells != null);
        doFillBlankRows(report, context);
        return report;
    }

    public void buildCell(Context context, List<Cell> cells) {
        buildCell(context, cells, new HashMap<>(), null);
    }

    public void buildCell(Context context, List<Cell> cells, Map<String, Integer> fillRowMap,Map<String, String> sortInMap) {
        if (cells == null) {
            cells = context.nextUnprocessedCells();
        }
        if (cells == null) {
            return;
        }
        CellBuilder noneExpandBuilder = new NoneExpandBuilder();
        for (int i = 0; i < cells.size(); i++) {
            Cell cell = cells.get(i);
            String sort="";
            if (cell.getValue().getType().equals(DATASET)){
                DatasetValue value = (DatasetValue) cell.getValue();
                if (CollectionUtil.isNotEmpty(sortInMap)) {
                        Set<String> strings = sortInMap.keySet();
                        List<String> collect = strings.stream()
                                .filter(value.getDatasetName()::equals).collect(Collectors.toList());
                        if (CollectionUtil.isNotEmpty(collect)) {
                            sort=collect.get(0);
                            value.setSort(sort);
                    }
                }
            }


            List<BindData> dataList = context.buildCellData(cell);
            cell.setProcessed(true);
            int size = dataList.size();
            Cell lastCell = cell;
            if (size == 1) {
                lastCell = noneExpandBuilder.buildCell(dataList, cell, context);
            } else if (size > 1) {
                CellBuilder cellBuilder = cellBuildersMap.get(cell.getExpand());
                lastCell = cellBuilder.buildCell(dataList, cell, context);
            }
            boolean isLast = i == cells.size() - 1;
            if (isLast && fillRowMap.get(cell.getName()) != null) {
                int multiple = fillRowMap.get(cell.getName());
                context.addFillBlankRow(lastCell.getName(), lastCell.getRow(), multiple);
            }
        }
    }

    private int resultCount(int total, int multiple, int num) {
        int multipleNum = multiple * num;
        if (multipleNum >= total) {
            return multipleNum - total;
        }
        return resultCount(total, multiple, num + 1);
    }

    private void doFillBlankRows(Report report, Context context) {
        Map<String, List<Cell>> cellsMap = report.getCellsMap();
        Map<String, Map<Row, Integer>> map = context.getFillBlankRowsMap();
        for (String key : map.keySet()) {
            List<Cell> cells = cellsMap.get(key) != null ? cellsMap.get(key) : new ArrayList<>();
            List<Row> newRowList = new ArrayList<>();
            Map<Row, Integer> rowMap = map.get(key);
            int total = cells.size();
            for (Row row : rowMap.keySet()) {
                int multiple = rowMap.get(row);
                int size = resultCount(total, multiple, 1);
                Row lastRow = findLastRow(row, report);
                for (int i = 0; i < size; i++) {
                    Row newRow = buildNewRow(lastRow, report);
                    newRowList.add(newRow);
                }
                int rowNumber = lastRow.getRowNumber();
                if (!newRowList.isEmpty()) {
                    report.insertRows(rowNumber + 1, newRowList);
                    newRowList.clear();
                }
            }
        }
    }

    private Row findLastRow(Row row, Report report) {
        List<Row> rows = report.getRows();
        List<Cell> cells = row.getCells();
        Row lastRow = row;
        int span = 0;
        for (Cell cell : cells) {
            int rowSpan = cell.getRowSpan();
            if (rowSpan < 2) {
                continue;
            }
            if (span == 0) {
                span = rowSpan;
            } else if (rowSpan > span) {
                span = rowSpan;
            }
        }
        if (span > 1) {
            int rowIndex = row.getRowNumber() - 1 + span - 1;
            lastRow = rows.get(rowIndex);
        }
        return lastRow;
    }

    private Row buildNewRow(Row row, Report report) {
        Row newRow = row.newRow();
        List<Row> rows = report.getRows();
        List<Column> columns = report.getColumns();
        int start = -1;
        int colSize = columns.size();
        Map<Row, Map<Column, Cell>> rowMap = report.getRowColCellMap();
        Map<Column, Cell> newCellMap = new HashMap<>();
        rowMap.put(newRow, newCellMap);

        Map<Column, Cell> colMap = rowMap.get(row);
        for (int index = 0; index < colSize; index++) {
            Column column = columns.get(index);
            Cell currentCell = colMap.get(column);
            if (currentCell == null) {
                if (start == -1) {
                    start = row.getRowNumber() - 2;
                }
                for (int i = start; i > -1; i--) {
                    Row currentRow = rows.get(i);
                    Map<Column, Cell> prevColMap = rowMap.get(currentRow);
                    if (prevColMap == null) {
                        continue;
                    }
                    if (prevColMap.containsKey(column)) {
                        currentCell = prevColMap.get(column);
                        break;
                    }
                }
            }
            if (currentCell == null) {
                continue;
            }
//            int colSpan = currentCell.getColSpan();
//            if (colSpan > 0) {
//                colSpan--;
//                index += colSpan;
//            }
//            int rowSpan = currentCell.getRowSpan();
//            if (rowSpan > 1) {
//                currentCell.setRowSpan(rowSpan + 1);
//            } else {
            Cell newCell = newBlankCell(currentCell, column, report);
            newCell.setRow(newRow);
            newRow.getCells().add(newCell);
            newCellMap.put(newCell.getColumn(), newCell);
//            }
        }
        return newRow;
    }

    private Cell newBlankCell(Cell cell, Column column, Report report) {
        Cell newCell = new Cell();
        Value value = cell.getValue();
        newCell.setData(null);
        if (value instanceof DatasetValue) {
            DatasetValue datasetValue = (DatasetValue) value;
            List<AggregateType> typeList = ImmutableList.of(AggregateType.sum, AggregateType.avg, AggregateType.min, AggregateType.max, AggregateType.count);
            AggregateType aggregate = datasetValue.getAggregate();
            if (typeList.contains(aggregate)) {
                newCell.setData(0);
            }
        }
        newCell.setColSpan(cell.getColSpan());
        report.addLazyCell(newCell);
        newCell.setName(cell.getName());
        newCell.setColumn(column);
        newCell.setStyleModel(cell.getStyleModel());
        newCell.setZxing(cell.getZxing());
        newCell.setCellData(cell.getCellData());
        newCell.setSheetColumnData(cell.getSheetColumnData());
        newCell.setSheetRowData(cell.getSheetRowData());
        column.getCells().add(newCell);
        Cell leftParent = cell.getLeftParentCell();
        if (leftParent != null) {
            newCell.setLeftParentCell(leftParent);
            leftParent.addRowChild(newCell);
        }
        Cell topParent = cell.getTopParentCell();
        if (topParent != null) {
            newCell.setTopParentCell(topParent);
            topParent.addColumnChild(newCell);
        }
        return newCell;
    }

}
