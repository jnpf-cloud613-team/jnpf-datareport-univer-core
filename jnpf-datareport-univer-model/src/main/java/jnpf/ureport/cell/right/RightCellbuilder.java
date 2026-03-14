package jnpf.ureport.cell.right;

import jnpf.ureport.Range;
import jnpf.ureport.definition.BlankCellInfo;
import jnpf.ureport.definition.CellDefinition;
import jnpf.ureport.utils.BuildUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @since 2月24日
 */
public class RightCellbuilder {

    public void buildParentCell(CellDefinition cell, List<CellDefinition> cells) {
        List<Range> rangeList = new ArrayList<>();
        Range range = buildChildrenCellRange(cell);
        List<CellDefinition> parentCells = new ArrayList<>();
        collectParentCells(cell, parentCells);
        buildParents(cell, parentCells, range, rangeList);

        Range childRange = buildChildrenCells(cell, rangeList);
        buildChildrenBlankCells(cell, cells, childRange);
        Range colRange = buildColumnRange(rangeList);
        buildColumnsBlankCells(cell, cells, colRange);
        int start = colRange.getStart();
        int end = colRange.getEnd();
        int colNumberStart = cell.getColumnNumber();
        int colNumberEnd = cell.getColumnNumber();
        int colSpan = cell.getColSpan();
        if (colSpan > 0) {
            colNumberEnd += colSpan - 1;
        }
        int rangeStart = 0;
        int rangeEnd = 0;
        if (start != -1) {
            rangeStart = start - colNumberStart;
        }
        if (end > colNumberStart && end > colNumberEnd) {
            rangeEnd = end - colNumberStart;
        } else {
            rangeEnd = colNumberEnd - colNumberStart;
        }
        Range duplicateRange = new Range(rangeStart, rangeEnd);
        cell.setDuplicateRange(duplicateRange);
    }


    private void buildColumnsBlankCells(CellDefinition cell, List<CellDefinition> cells, Range range) {
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
                int colNumber = cellDef.getColumnNumber();
                if (colNumber == i) {
                    int offset = colNumber - cell.getColumnNumber();
                    blankCellNamesMap.put(name, new BlankCellInfo(offset, cellDef.getColSpan(), false));
                } else if (colNumber < i) {
                    int endColNumber = BuildUtils.buildColNumberEnd(cellDef, colNumber);
                    if (endColNumber >= i) {
                        int offset = colNumber - cell.getColumnNumber();
                        blankCellNamesMap.put(name, new BlankCellInfo(offset, cellDef.getColSpan(), false));
                    }
                }
            }
        }
        if (nextEnd > end) {
            buildColumnsBlankCells(cell, cells, new Range(end, nextEnd));
        }
    }

    private Range buildColumnRange(List<Range> rangeList) {
        Range colRange = new Range();
        for (Range range : rangeList) {
            for (int i = range.getStart(); i <= range.getEnd(); i++) {
                if (colRange.getStart() == -1 || i < colRange.getStart()) {
                    colRange.setStart(i);
                }
                if (colRange.getEnd() < i) {
                    colRange.setEnd(i);
                }
            }
        }
        return colRange;
    }

    private Range buildChildrenCells(CellDefinition cell, List<Range> rangeList) {
        Range range = new Range();
        List<CellDefinition> childrenCells = cell.getColumnChildrenCells();
        for (CellDefinition childCell : childrenCells) {
            cell.getNewCellNames().add(childCell.getName());
            int colNumber = childCell.getColumnNumber();
            int endColNumber = BuildUtils.buildColNumberEnd(childCell, colNumber);
            rangeList.add(new Range(colNumber, endColNumber));
            if (endColNumber > range.getEnd()) {
                range.setEnd(endColNumber);
            }
            if (range.getStart() == -1 || colNumber < range.getStart()) {
                range.setStart(colNumber);
            }
        }
        return range;
    }


    private void buildChildrenBlankCells(CellDefinition cell, List<CellDefinition> cells, Range childRange) {
        int startColNumber = cell.getColumnNumber();
        int endColNumber = BuildUtils.buildColNumberEnd(cell, startColNumber);
        int start = childRange.getStart();
        int end = childRange.getEnd();
        if (start != -1 && start < startColNumber) {
            startColNumber = start;
        }
        if (end > endColNumber) {
            endColNumber = end;
        }
        Map<String, BlankCellInfo> blankCellNamesMap = cell.getNewBlankCellsMap();
        for (int i = startColNumber; i <= endColNumber; i++) {
            for (CellDefinition c : cells) {
                if (c.getColumnNumber() != i) {
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
                int offset = c.getColumnNumber() - cell.getColumnNumber();
                blankCellNamesMap.put(name, new BlankCellInfo(offset, c.getColSpan(), false));
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
        CellDefinition topParentCell = cell.getTopParentCell();
        if (topParentCell == null) {
            return;
        }
        parentCells.add(topParentCell);
        collectParentCells(topParentCell, parentCells);
    }

    private void buildParents(CellDefinition mainCell, List<CellDefinition> parentCells, Range childRange, List<Range> rangeList) {
        int colNumberStart = mainCell.getColumnNumber();
        int colNumberEnd = BuildUtils.buildColNumberEnd(mainCell, colNumberStart);
        rangeList.add(new Range(colNumberStart, colNumberEnd));

        int start = childRange.getStart();
        int end = childRange.getEnd();
        Map<String, BlankCellInfo> newBlankCellsMap = mainCell.getNewBlankCellsMap();
        boolean increase = true;
        for (CellDefinition parentCell : parentCells) {
            String parentCellName = parentCell.getName();
            int parentColNumberStart = parentCell.getColumnNumber();
            int parentColNumberEnd = BuildUtils.buildColNumberEnd(parentCell, parentColNumberStart);
            int offset = parentColNumberStart - colNumberStart;
            int parentColSpan = parentCell.getColSpan();
            boolean isOut = assertOut(parentCell, mainCell, childRange);
            if (isOut) {
                increase = false;
                boolean doBlank = assertDoBlank(parentCell.getTopParentCell(), parentCell, mainCell, childRange);
                if (doBlank) {
                    newBlankCellsMap.put(parentCellName, new BlankCellInfo(offset, parentColSpan, true));
                    rangeList.add(new Range(parentColNumberStart, parentColNumberEnd));
                }
                continue;
            }
            if ((start != -1 && start < parentColNumberStart) || end > parentColNumberEnd) {
                newBlankCellsMap.put(parentCellName, new BlankCellInfo(offset, parentColSpan, true));
                rangeList.add(new Range(parentColNumberStart, parentColNumberEnd));
                increase = false;
                continue;
            }
            if (increase) {
                mainCell.getIncreaseSpanCellNames().add(parentCellName);
            } else {
                newBlankCellsMap.put(parentCellName, new BlankCellInfo(offset, parentColSpan, true));
                rangeList.add(new Range(parentColNumberStart, parentColNumberEnd));
            }
        }
    }

    private boolean assertDoBlank(CellDefinition nextParentCell, CellDefinition parentCell, CellDefinition mainCell, Range childRange) {
        if (nextParentCell == null) {
            return false;
        }
        boolean isOut = assertOut(nextParentCell, mainCell, childRange);
        if (isOut) {
            return assertDoBlank(nextParentCell.getTopParentCell(), parentCell, mainCell, childRange);
        }
        int start = parentCell.getColumnNumber();
        int end = BuildUtils.buildColNumberEnd(parentCell, start);
        int nextStart = nextParentCell.getColumnNumber();
        if (nextStart <= end) {
            return true;
        }
        return assertDoBlank(nextParentCell.getTopParentCell(), parentCell, mainCell, childRange);
    }

    private boolean assertOut(CellDefinition parentCell, CellDefinition mainCell, Range childRange) {
        int start = parentCell.getColumnNumber();
        int end = BuildUtils.buildColNumberEnd(parentCell, start);
        int rangeStart = childRange.getStart();
        int rangeEnd = childRange.getEnd();
        if (rangeStart != -1) {
            if ((start >= rangeStart && start <= rangeEnd) || (end >= rangeStart && end <= rangeEnd)) {
                return false;
            }
        }
        int colStart = mainCell.getColumnNumber();
        int colEnd = BuildUtils.buildColNumberEnd(mainCell, colStart);
        if ((start >= colStart && start <= colEnd) || (end >= colStart && end <= colEnd) || (start <= colStart && end >= colEnd)) {
            return false;
        }
        return true;
    }

    private Range buildChildrenCellRange(CellDefinition mainCell) {
        Range range = new Range();
        List<CellDefinition> childrenCells = mainCell.getColumnChildrenCells();
        for (CellDefinition childCell : childrenCells) {
            int childColumnNumberStart = childCell.getColumnNumber();
            int childColSpan = childCell.getColSpan();
            childColSpan = childColSpan > 0 ? childColSpan - 1 : childColSpan;
            int childColumnNumberEnd = childColumnNumberStart + childColSpan;
            if (range.getStart() == -1 || childColumnNumberStart < range.getStart()) {
                range.setStart(childColumnNumberStart);
            }
            if (childColumnNumberEnd > range.getEnd()) {
                range.setEnd(childColumnNumberEnd);
            }
        }
        return range;
    }
}
