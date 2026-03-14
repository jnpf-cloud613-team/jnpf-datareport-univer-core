package jnpf.ureport.cell.right;

import jnpf.ureport.build.Context;
import jnpf.ureport.model.Cell;
import jnpf.ureport.model.Column;
import jnpf.ureport.model.Report;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @since 2016年11月10日
 */
@Setter
@Getter
public class RightDuplicate {
    private int index;
    private int columnSize;
    private Context context;
    private Cell mainCell;
    private int minColNumber = -1;
    private Map<Column, Column> colMap = new HashMap<>();
    private List<Column> newColList = new ArrayList<>();

    public RightDuplicate(Cell mainCell, int columnSize, Context context) {
        this.mainCell = mainCell;
        this.columnSize = columnSize;
        this.context = context;
    }

    public Column newColumn(Column col, int colNumber) {
        if (colMap.containsKey(col)) {
            return colMap.get(col);
        } else {
            Column newCol = col.newColumn();
            colNumber = colNumber + columnSize * (index - 1) + columnSize;
            if (minColNumber == -1 || minColNumber > colNumber) {
                minColNumber = colNumber;
            }
            newCol.setTempColumnNumber(colNumber);
            newCol.setColumnKey(col.getColumnKey());
            newColList.add(newCol);
            colMap.put(col, newCol);
            return newCol;
        }
    }


    public void complete() {
        if (minColNumber < 1) {
            return;
        }
        Report report = context.getReport();
        report.insertColumns(minColNumber, newColList);
    }

    public void reset() {
        colMap.clear();
    }
}
