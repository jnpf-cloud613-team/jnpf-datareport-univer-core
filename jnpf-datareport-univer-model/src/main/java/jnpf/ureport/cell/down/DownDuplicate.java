package jnpf.ureport.cell.down;

import jnpf.ureport.build.Context;
import jnpf.ureport.model.Cell;
import jnpf.ureport.model.Report;
import jnpf.ureport.model.Row;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Setter
@Getter
public class DownDuplicate {
    private int index;
    private Cell mainCell;
    private int rowSize;
    private Context context;
    private int minRowNumber = -1;
    private Map<Row, Row> rowMap = new HashMap<>();
    private List<Row> newRowList = new ArrayList<>();

    public DownDuplicate(Cell mainCell, int rowSize, Context context) {
        this.mainCell = mainCell;
        this.rowSize = rowSize;
        this.context = context;
    }

    public Row newRow(Row row, int currentRowNumber) {
        if (rowMap.containsKey(row)) {
            return rowMap.get(row);
        } else {
            int rowNumber = currentRowNumber;
            Row newRow = row.newRow();
            rowNumber = rowNumber + rowSize * (index - 1) + rowSize;
            if (minRowNumber == -1 || minRowNumber > rowNumber) {
                minRowNumber = rowNumber;
            }
            newRow.setTempRowNumber(rowNumber);
            newRowList.add(newRow);
            rowMap.put(row, newRow);
            return newRow;
        }
    }

    public void complete() {
        if (minRowNumber < 1) {
            return;
        }
        Report report = context.getReport();
        report.insertRows(minRowNumber, newRowList);
    }

    public void reset() {
        rowMap.clear();
    }
}
