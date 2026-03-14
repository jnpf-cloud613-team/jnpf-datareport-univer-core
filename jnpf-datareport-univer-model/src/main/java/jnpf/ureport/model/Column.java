package jnpf.ureport.model;

import jnpf.univer.sheet.UniverSheetColumnData;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Setter
@Getter
public class Column {
    /**
     * 是否冻结
     */
    private Boolean freeze = false;
    /**
     * 一个用来临时存放当前列号的属性，只在构建报表时创建新列时使用
     */
    private int tempColumnNumber;

    /**
     * 原数据列数
     */
    private int columnKey;

    private List<Column> columns;

    private List<Cell> cells = new ArrayList<>();


    private UniverSheetColumnData sheetColumnData;

    public Column(List<Column> columns) {
        this.columns = columns;
    }

    public Column newColumn() {
        Column col = new Column(columns);
        col.setFreeze(freeze);
        col.setSheetColumnData(sheetColumnData);
        return col;
    }

    public int getColumnNumber() {
        return this.columns.indexOf(this) + 1;
    }

}
