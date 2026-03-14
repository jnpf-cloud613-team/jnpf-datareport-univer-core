package jnpf.ureport.utils;


import jnpf.ureport.definition.CellDefinition;

/**
 * @author
 * @since 2月27日
 */
public class BuildUtils {

    public static int buildRowNumberEnd(CellDefinition cell, int rowNumber) {
        int rowSpan = cell.getRowSpan();
        rowSpan = rowSpan > 0 ? rowSpan - 1 : rowSpan;
        return rowNumber + rowSpan;
    }

    public static int buildColNumberEnd(CellDefinition cell, int colNumber) {
        int colSpan = cell.getColSpan();
        colSpan = colSpan > 0 ? colSpan - 1 : colSpan;
        return colNumber + colSpan;
    }
}
