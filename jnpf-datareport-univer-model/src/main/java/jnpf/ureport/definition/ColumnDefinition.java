package jnpf.ureport.definition;

import jnpf.ureport.model.Column;
import jnpf.univer.sheet.UniverSheetColumnData;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Setter
@Getter
public class ColumnDefinition {
    private int columnNumber;
    private boolean freeze;
    private UniverSheetColumnData sheetColumnData;

    protected Column newColumn(List<Column> columns) {
        Column col = new Column(columns);
        col.setFreeze(freeze);
        col.setSheetColumnData(sheetColumnData);
        return col;
    }

}
