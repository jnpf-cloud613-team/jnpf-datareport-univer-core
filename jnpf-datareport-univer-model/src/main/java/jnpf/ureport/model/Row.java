package jnpf.ureport.model;

import jnpf.univer.sheet.UniverSheetRowData;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Setter
@Getter
public class Row {
    private String rowKey;
    /**
     * 一个用来临时存放当前行号的属性，只在构建报表时创建新行时使用
     */
    private int tempRowNumber;
    /**
     * 是否冻结
     */
    private Boolean freeze = false;

    private List<Row> rows;

    private List<Cell> cells = new ArrayList<>();


    private UniverSheetRowData sheetRowData;

    public Row(List<Row> rows) {
        this.rows = rows;
    }

    public Row newRow() {
        Row row = new Row(rows);
        row.setFreeze(freeze);
        row.setRowKey(rowKey);
        row.setSheetRowData(sheetRowData);
        return row;
    }

    public int getRowNumber() {
        return rows.indexOf(this) + 1;
    }

}
