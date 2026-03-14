package jnpf.ureport.cell.down;

import jnpf.ureport.Range;
import jnpf.ureport.definition.BlankCellInfo;
import jnpf.ureport.definition.CellDefinition;
import jnpf.ureport.utils.BuildUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class DownCellbuilder {

    public void buildParentCell(CellDefinition cell, List<CellDefinition> cells) {
        List<Range> rangeList = new ArrayList<>();
        Range range = buildChildrenCellRange(cell);
        List<CellDefinition> parentCells = new ArrayList<>();
        collectParentCells(cell, parentCells);
        buildParents(cell, parentCells, range, rangeList);

        Range childRange = buildChildrenCells(cell, rangeList);
        buildChildrenBlankCells(cell, cells, childRange);
        Range rowRange = buildRowRange(rangeList);
        buildRowsBlankCells(cell, cells, rowRange);
        int start = rowRange.getStart();
        int end = rowRange.getEnd();
        int rowNumberStart = cell.getRowNumber();
        int rowNumberEnd = cell.getRowNumber();
        int rowSpan = cell.getRowSpan();
        if (rowSpan > 0) {
            rowNumberEnd += rowSpan - 1;
        }
        int rangeStart = 0;
        int rangeEnd = 0;
        if (start != -1) {
            rangeStart = start - rowNumberStart;
        }
        if (end > rowNumberStart && end > rowNumberEnd) {
            rangeEnd = end - rowNumberStart;
        } else {
            rangeEnd = rowNumberEnd - rowNumberStart;
        }
        Range duplicateRange = new Range(rangeStart, rangeEnd);
        cell.setDuplicateRange(duplicateRange);
    }


    private void buildRowsBlankCells(CellDefinition cell, List<CellDefinition> cells, Range range) {
        Map<String, BlankCellInfo> blankCellNamesMap = cell.getNewBlankCellsMap();
        int start = range.getStart();
        int end = range.getEnd();
        int nextEnd = 0;
        for (int i = start; i <= end; i++) {
            for (CellDefinition cellDef : cells) {
                String name = cellDef.getName();
                if (cellPrcessed(cell, name)) {
                    continue;
                }
                int rowNumber = cellDef.getRowNumber();
                if (rowNumber == i) {
                    int offset = rowNumber - cell.getRowNumber();
                    blankCellNamesMap.put(name, new BlankCellInfo(offset, cellDef.getRowSpan(), false));
                } else if (rowNumber < i) {
                    int endRowNumber = BuildUtils.buildRowNumberEnd(cellDef, rowNumber);
                    if (endRowNumber >= i) {
                        int offset = rowNumber - cell.getRowNumber();
                        blankCellNamesMap.put(name, new BlankCellInfo(offset, cellDef.getRowSpan(), false));
                    }
                }
            }
        }
        if (nextEnd > end) {
            buildRowsBlankCells(cell, cells, new Range(end, nextEnd));
        }
    }

    private Range buildRowRange(List<Range> rangeList) {
        Range rowRange = new Range();
        for (Range range : rangeList) {
            for (int i = range.getStart(); i <= range.getEnd(); i++) {
                if (rowRange.getStart() == -1 || i < rowRange.getStart()) {
                    rowRange.setStart(i);
                }
                if (rowRange.getEnd() < i) {
                    rowRange.setEnd(i);
                }
            }
        }
        return rowRange;
    }

    private Range buildChildrenCells(CellDefinition cell, List<Range> rangeList) {
        Range range = new Range();
        List<CellDefinition> rowChildrenCells = cell.getRowChildrenCells();
        for (CellDefinition childCell : rowChildrenCells) {
            cell.getNewCellNames().add(childCell.getName());
            int rowNumber = childCell.getRowNumber();
            int endRowNumber = BuildUtils.buildRowNumberEnd(childCell, rowNumber);
            rangeList.add(new Range(rowNumber, endRowNumber));
            if (endRowNumber > range.getEnd()) {
                range.setEnd(endRowNumber);
            }
            if (range.getStart() == -1 || rowNumber < range.getStart()) {
                range.setStart(rowNumber);
            }
        }
        return range;
    }

    private void buildChildrenBlankCells(CellDefinition cell, List<CellDefinition> cells, Range childRange) {
        int startRowNumber = cell.getRowNumber();
        int endRowNumber = BuildUtils.buildRowNumberEnd(cell, startRowNumber);
        int start = childRange.getStart();
        int end = childRange.getEnd();
        if (start != -1 && start < startRowNumber) {
            startRowNumber = start;
        }
        if (end > endRowNumber) {
            endRowNumber = end;
        }
        Map<String, BlankCellInfo> blankCellNamesMap = cell.getNewBlankCellsMap();
        for (int i = startRowNumber; i <= endRowNumber; i++) {
            for (CellDefinition c : cells) {
                if (c.getRowNumber() != i) {
                    continue;
                }
                if (c.equals(cell)) {
                    continue;
                }
                String name = c.getName();
                boolean contain = cellPrcessed(cell, name);
                if (contain) {
                    continue;
                }
                int offset = c.getRowNumber() - cell.getRowNumber();
                blankCellNamesMap.put(name, new BlankCellInfo(offset, c.getRowSpan(), false));
            }
        }
    }

    private boolean cellPrcessed(CellDefinition cell, String name) {
        List<String> newCellNames = cell.getNewCellNames();
        List<String> increaseCellNames = cell.getIncreaseSpanCellNames();
        Map<String, BlankCellInfo> blankCellNamesMap = cell.getNewBlankCellsMap();
        boolean contain = cell.getName().equals(name);
        if (newCellNames.contains(name)) {
            contain = true;
        }
        if (increaseCellNames.contains(name)) {
            contain = true;
        }
        if (blankCellNamesMap.containsKey(name)) {
            contain = true;
        }
        return contain;
    }

    private void collectParentCells(CellDefinition cell, List<CellDefinition> parentCells) {
        CellDefinition leftParentCell = cell.getLeftParentCell();
        if (leftParentCell == null) {
            return;
        }
        parentCells.add(leftParentCell);
        collectParentCells(leftParentCell, parentCells);
    }

    private void buildParents(CellDefinition mainCell, List<CellDefinition> parentCells, Range childRange, List<Range> rangeList) {
        int rowNumberStart = mainCell.getRowNumber();
        int rowNumberEnd = BuildUtils.buildRowNumberEnd(mainCell, rowNumberStart);
        rangeList.add(new Range(rowNumberStart, rowNumberEnd));

        int start = childRange.getStart();
        int end = childRange.getEnd();
        Map<String, BlankCellInfo> newBlankCellsMap = mainCell.getNewBlankCellsMap();
        boolean increase = true;
        for (CellDefinition parentCell : parentCells) {
            String parentCellName = parentCell.getName();
            int parentRowNumberStart = parentCell.getRowNumber();
            int parentRowNumberEnd = BuildUtils.buildRowNumberEnd(parentCell, parentRowNumberStart);
            int offset = parentRowNumberStart - rowNumberStart;
            int parentRowSpan = parentCell.getRowSpan();
            boolean isOut = assertOut(parentCell, mainCell, childRange);
            if (isOut) {
                increase = false;
                boolean doBlank = assertDoBlank(parentCell.getLeftParentCell(), parentCell, mainCell, childRange);
                if (doBlank) {
                    newBlankCellsMap.put(parentCellName, new BlankCellInfo(offset, parentRowSpan, true));
                    rangeList.add(new Range(parentRowNumberStart, parentRowNumberEnd));
                }
                continue;
            }
            if ((start != -1 && start < parentRowNumberStart) || end > parentRowNumberEnd) {
                newBlankCellsMap.put(parentCellName, new BlankCellInfo(offset, parentRowSpan, true));
                rangeList.add(new Range(parentRowNumberStart, parentRowNumberEnd));
                increase = false;
                continue;
            }
            if (increase) {
                mainCell.getIncreaseSpanCellNames().add(parentCellName);
            } else {
                newBlankCellsMap.put(parentCellName, new BlankCellInfo(offset, parentRowSpan, true));
                rangeList.add(new Range(parentRowNumberStart, parentRowNumberEnd));
            }
        }
    }

    private boolean assertDoBlank(CellDefinition nextParentCell, CellDefinition parentCell, CellDefinition mainCell, Range childRange) {
        if (nextParentCell == null) {
            return false;
        }
        boolean isOut = assertOut(nextParentCell, mainCell, childRange);
        if (isOut) {
            return assertDoBlank(nextParentCell.getLeftParentCell(), parentCell, mainCell, childRange);
        }
        int start = parentCell.getRowNumber();
        int end = BuildUtils.buildRowNumberEnd(parentCell, start);
        int nextStart = nextParentCell.getRowNumber();
        if (nextStart <= end) {
            return true;
        }
        return assertDoBlank(nextParentCell.getLeftParentCell(), parentCell, mainCell, childRange);
    }

    private boolean assertOut(CellDefinition parentCell, CellDefinition mainCell, Range childRange) {
        int start = parentCell.getRowNumber();
        int end = BuildUtils.buildRowNumberEnd(parentCell, start);
        int rangeStart = childRange.getStart();
        int rangeEnd = childRange.getEnd();
        if (rangeStart != -1) {
            if ((start >= rangeStart && start <= rangeEnd) || (end >= rangeStart && end <= rangeEnd)) {
                return false;
            }
        }
        int rowStart = mainCell.getRowNumber();
        int rowEnd = BuildUtils.buildRowNumberEnd(mainCell, rowStart);
        if ((start >= rowStart && start <= rowEnd) || (end >= rowStart && end <= rowEnd) || (start <= rowStart && end >= rowEnd)) {
            return false;
        }
        return true;
    }

    private Range buildChildrenCellRange(CellDefinition mainCell) {
        Range range = new Range();
        List<CellDefinition> childrenCells = mainCell.getRowChildrenCells();
        for (CellDefinition childCell : childrenCells) {
            int childRowNumberStart = childCell.getRowNumber();
            int childRowSpan = childCell.getRowSpan();
            childRowSpan = childRowSpan > 0 ? childRowSpan - 1 : childRowSpan;
            int childRowNumberEnd = childRowNumberStart + childRowSpan;
            if (range.getStart() == -1 || childRowNumberStart < range.getStart()) {
                range.setStart(childRowNumberStart);
            }
            if (childRowNumberEnd > range.getEnd()) {
                range.setEnd(childRowNumberEnd);
            }
        }
        return range;
    }

}
