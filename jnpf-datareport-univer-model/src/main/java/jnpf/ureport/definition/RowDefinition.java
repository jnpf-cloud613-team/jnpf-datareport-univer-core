package jnpf.ureport.definition;

import jnpf.ureport.model.Row;
import jnpf.univer.sheet.UniverSheetRowData;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class RowDefinition {
    private int rowNumber;
    private boolean freeze;
    private UniverSheetRowData sheetRowData;

    protected Row newRow(List<Row> rows) {
        Row row = new Row(rows);
        row.setRowKey("r" + rowNumber);
        row.setFreeze(freeze);
        row.setSheetRowData(sheetRowData);
        return row;
    }


}
