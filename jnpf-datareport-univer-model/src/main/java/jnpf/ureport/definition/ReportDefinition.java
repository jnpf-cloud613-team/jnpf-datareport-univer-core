package jnpf.ureport.definition;

import jnpf.ureport.build.Dataset;
import jnpf.ureport.model.Cell;
import jnpf.ureport.model.Column;
import jnpf.ureport.model.Report;
import jnpf.ureport.model.Row;
import jnpf.ureport.utils.DataUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Getter
@Setter
public class ReportDefinition {
    private String reportFullName;
    private CellDefinition rootCell;
    private List<CellDefinition> cells;
    private List<RowDefinition> rows;
    private List<ColumnDefinition> columns;
    private Map<String, Dataset> datasetMap;
    private Map<String,Integer> fillBlankRowsMap;

    public Report newReport() {
        Report report = new Report();
        report.setReportFullName(reportFullName);
        List<Row> reportRows = new ArrayList<>();
        List<Column> reportColumns = new ArrayList<>();
        report.setRows(reportRows);
        report.setColumns(reportColumns);
        Map<Integer, Row> rowMap = new HashMap<>();
        for (RowDefinition rowDef : rows) {
            Row newRow = rowDef.newRow(reportRows);
            report.insertRow(newRow, rowDef.getRowNumber());
            rowMap.put(rowDef.getRowNumber(), newRow);
        }
        Map<Integer, Column> columnMap = new HashMap<>();
        for (ColumnDefinition columnDef : columns) {
            Column newColumn = columnDef.newColumn(reportColumns);
            newColumn.setColumnKey(columnDef.getColumnNumber());
            report.insertColumn(newColumn, columnDef.getColumnNumber());
            columnMap.put(columnDef.getColumnNumber(), newColumn);
        }
        Map<CellDefinition, Cell> cellMap = new HashMap<>();
        for (CellDefinition cellDef : cells) {
            Cell cell = cellDef.newCell();
            cellMap.put(cellDef, cell);
            Row targetRow = rowMap.get(cellDef.getRowNumber());
            cell.setRow(targetRow);
            targetRow.getCells().add(cell);
            Column targetColumn = columnMap.get(cellDef.getColumnNumber());
            cell.setColumn(targetColumn);
            targetColumn.getCells().add(cell);

            if (cellDef.getLeftParentCell() == null && cellDef.getTopParentCell() == null) {
                report.setRootCell(cell);
            }
            report.addCell(cell);
        }
        for (CellDefinition cellDef : cells) {
            Cell targetCell = cellMap.get(cellDef);
            CellDefinition leftParentCellDef = cellDef.getLeftParentCell();
            if (leftParentCellDef != null) {
                targetCell.setLeftParentCell(cellMap.get(leftParentCellDef));
            } else {
                targetCell.setLeftParentCell(null);
            }
            CellDefinition topParentCellDef = cellDef.getTopParentCell();
            if (topParentCellDef != null) {
                targetCell.setTopParentCell(cellMap.get(topParentCellDef));
            } else {
                targetCell.setTopParentCell(null);
            }
            DataUtils.cellList(targetCell, true, true);
        }
//        for (CellDefinition cellDef : cells) {
//            Cell targetCell = cellMap.get(cellDef);
//
//            List<CellDefinition> rowChildrenCellDefinitions = cellDef.getRowChildrenCells();
//            for (CellDefinition childCellDef : rowChildrenCellDefinitions) {
//                Cell childCell = cellMap.get(childCellDef);
//                targetCell.addRowChild(childCell);
//            }
//            List<CellDefinition> columnChildrenCellDefinitions = cellDef.getColumnChildrenCells();
//            for (CellDefinition childCellDef : columnChildrenCellDefinitions) {
//                Cell childCell = cellMap.get(childCellDef);
//                targetCell.addColumnChild(childCell);
//            }
//        }
        return report;
    }

}
