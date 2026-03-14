package jnpf.ureport.build;

import jnpf.ureport.definition.value.SimpleValue;
import jnpf.ureport.definition.value.Value;
import jnpf.ureport.expression.util.ElCompute;
import jnpf.ureport.model.Cell;
import jnpf.ureport.model.Column;
import jnpf.ureport.model.Report;
import jnpf.ureport.model.Row;
import lombok.Getter;
import lombok.Setter;

import java.util.*;


@Getter
@Setter
public class Context {
    private Report report;
    private Cell rootCell;
    private ReportBuilder reportBuilder;
    private Map<String, Dataset> datasetMap;
    private Map<String, Object> variableMap = new HashMap<>();
    private Map<Integer, List<Row>> currentPageRowsMap = new HashMap<>();
    private Map<Row, Map<Column, Cell>> blankCellsMap = new HashMap<>();
    private Map<String, List<Cell>> unprocessedCellsMap = new HashMap<>();
    private Map<String, Map<Row, Integer>> fillBlankRowsMap = new HashMap<>();

    public Context(ReportBuilder reportBuilder, Report report, Map<String, Dataset> datasetMap) {
        this.reportBuilder = reportBuilder;
        this.report = report;
        report.setContext(this);
        this.datasetMap = datasetMap;
        Map<String, List<Cell>> cellsMap = report.getCellsMap();
        for (String key : cellsMap.keySet()) {
            if (key.equals(report.getRootCell().getName())) {
                continue;
            }
            List<Cell> list = new ArrayList<>();
            list.addAll(cellsMap.get(key));
            unprocessedCellsMap.put(key, list);
        }
        this.rootCell = new Cell();
        this.rootCell.setName("ROOT");
    }

    public List<Cell> nextUnprocessedCells() {
        if (unprocessedCellsMap.size() == 0) {
            return null;
        }
        List<Cell> targetCellsList = null;
        String targetCellName = null;
        Set<String> keySet = unprocessedCellsMap.keySet();
        for (String cellName : keySet) {
            List<Cell> cells = unprocessedCellsMap.get(cellName);
            Cell cell = cells.get(0);
            Value value = cell.getValue();
            Cell leftParent = cell.getLeftParentCell();
            Cell topParent = cell.getTopParentCell();
            if ((leftParent == null || leftParent.isProcessed()) && (topParent == null || topParent.isProcessed())) {
                targetCellsList = cells;
                targetCellName = cellName;
                break;
            }
            if (value instanceof SimpleValue) {
                targetCellsList = cells;
                targetCellName = cellName;
                break;
            }
        }
        unprocessedCellsMap.remove(targetCellName);
        return targetCellsList;
    }

    public List<BindData> buildCellData(Cell cell) {
        return DataCompute.buildCellData(cell, this);
    }

    public void addReportCell(Cell newCell) {
        boolean lazyAdd = report.addCell(newCell);
    }

    public void addCell(Cell newCell) {
        addReportCell(newCell);
        addUnprocessedCell(newCell);
    }

    public void addUnprocessedCell(Cell cell) {
        String cellName = cell.getName();
        List<Cell> cells = null;
        if (unprocessedCellsMap.containsKey(cellName)) {
            cells = unprocessedCellsMap.get(cellName);
        } else {
            cells = new ArrayList<>();
            unprocessedCellsMap.put(cellName, cells);
        }

        int lastIdx = report.findSameLeftCell(cells, cell, 0);
        if (lastIdx > -1 && lastIdx < cells.size() - 1) {
            cells.add(lastIdx + 1, cell);
        } else {
            cells.add(cell);
        }
    }

    public List<Map<String, Object>> getDatasetData(String name) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (datasetMap.containsKey(name)) {
            list.addAll(datasetMap.get(name).getData());
        }
        return list;
    }

    public Row getRow(int rowNumber) {
        return report.getRow(rowNumber);
    }

    public Column getColumn(int columnNumber) {
        return report.getColumn(columnNumber);
    }

    public void addBlankCell(Cell cell) {
        cell.setBlankCell(true);
        Row row = cell.getRow();
        Column column = cell.getColumn();
        Map<Column, Cell> cellMap = blankCellsMap.get(row);
        if (cellMap == null) {
            cellMap = new HashMap<>();
            blankCellsMap.put(row, cellMap);
        }
        cellMap.put(column, cell);
        addReportCell(cell);
    }

    public Cell getBlankCell(Row row, Column column) {
        Map<Column, Cell> colCellMap = blankCellsMap.get(row);
        if (colCellMap == null) {
            return null;
        }
        Cell targetCell = colCellMap.get(column);
        return targetCell;
    }

    public void removeBlankCell(Cell blankCell) {
        Row row = blankCell.getRow();
        Column col = blankCell.getColumn();
        Map<Column, Cell> colCellMap = blankCellsMap.get(row);
        colCellMap.remove(col);
    }

    public void putVariable(String key, Object value) {
        variableMap.put(key, value);
    }

    public Object evalExpr(String expression) {
        return new ElCompute().doCompute(expression);
    }

    public boolean isCellPocessed(String cellName) {
        return !unprocessedCellsMap.containsKey(cellName);
    }

    public void addFillBlankRow(String cellName, Row row, int value) {
        Map<Row, Integer> rowMap = new HashMap<>();
        rowMap.put(row, value);
        fillBlankRowsMap.put(cellName, rowMap);
    }

    public Object getVariable(String key) {
        return variableMap.get(key);
    }

}
