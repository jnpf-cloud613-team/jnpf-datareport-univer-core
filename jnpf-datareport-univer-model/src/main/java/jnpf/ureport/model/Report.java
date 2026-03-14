package jnpf.ureport.model;

import cn.hutool.core.util.ObjectUtil;
import jnpf.ureport.build.Context;
import jnpf.util.StringUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class Report {
    private Cell rootCell;
    private Context context;
    private List<Row> rows;
    private List<Column> columns;
    private String reportFullName;
    private List<Cell> lazyComputeCells = new ArrayList<>();
    private Map<Row, Map<Column, Cell>> rowColCellMap = new HashMap<>();
    private Map<String, List<Cell>> cellsMap = new HashMap<>();

    public void insertRow(Row row, int rowNumber) {
        int pos = rowNumber - 1;
        rows.add(pos, row);
    }

    public void insertRows(int firstRowIndex, List<Row> insertRows) {
        //判断是否连续
        boolean isAllTemplateNum = insertRows.stream()
                .allMatch(t -> ObjectUtil.isNotEmpty(t.getTempRowNumber())&&t.getTempRowNumber()!=0);
        if (isAllTemplateNum) {
            insertRows=insertRows.stream().sorted(Comparator.comparing(Row::getTempRowNumber))
                    .collect(Collectors.toList());
        }
        int pos = firstRowIndex - 1;
        rows.addAll(pos, insertRows);
    }

    public void insertColumn(Column column, int columnNumber) {
        int pos = columnNumber - 1;
        if (pos > 0) {
            Column columnSet = new Column(new ArrayList<>());
            columns.add(columnSet);
        }
        columns.add(pos, column);
    }

    public void insertColumns(int firstColumnIndex, List<Column> insertColumns) {
        int pos = firstColumnIndex - 1;
        columns.addAll(pos, insertColumns);
    }

    public Row getRow(int rowNumber) {
        if (rowNumber > rows.size()) {
            return null;
        }
        return rows.get(rowNumber - 1);
    }

    public Column getColumn(int columnNumber) {
        if (columnNumber > columns.size()) {
            return null;
        }
        return columns.get(columnNumber - 1);
    }

    public boolean addCell(Cell cell) {
        String cellName = cell.getName();
        List<Cell> cells = null;
        if (cellsMap.containsKey(cellName)) {
            cells = cellsMap.get(cellName);
        } else {
            cells = new ArrayList<>();
            cellsMap.put(cellName, cells);
        }
        int lastIdx = findSameLeftCell(cells, cell, 0);
        if (lastIdx > -1 && lastIdx < cells.size() - 1) {
            cells.add(lastIdx + 1, cell);
        } else {
            cells.add(cell);
        }
        Row row = cell.getRow();
        Column col = cell.getColumn();
        Map<Column, Cell> colMap = null;
        if (rowColCellMap.containsKey(row)) {
            colMap = rowColCellMap.get(row);
        } else {
            colMap = new HashMap<>();
            rowColCellMap.put(row, colMap);
        }
        colMap.put(col, cell);
        return addLazyCell(cell);
    }

    public int findSameLeftCell(List<Cell> cells, Cell nowCell, int deep) {
        int lastIdx = -1;
        //todo
//        nowCell = nowCell.getLeftParentCell();
//        if (nowCell == null) {
//            return lastIdx;
//        }
//        for (int i = cells.size() - 1; i >= 0; i--) {
//            Cell left = cells.get(i).getLeftParentCell();
//            for (int j = 0; j < deep; j++) {
//                if (left == null) {
//                    return lastIdx;
//                }
//                left = left.getLeftParentCell();
//            }
//            if (left != null && left.equals(nowCell)) {
//                return i;
//            }
//        }
//        if (lastIdx == -1 && nowCell.getLeftParentCell() != null) {
//            return findSameLeftCell(cells, nowCell, deep + 1);
//        }
        return lastIdx;
    }

    public boolean addLazyCell(Cell cell) {
        return false;
    }


}
