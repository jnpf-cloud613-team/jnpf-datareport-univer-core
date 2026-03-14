package jnpf.ureport.cell.down;

import jnpf.ureport.Range;
import jnpf.ureport.build.BindData;
import jnpf.ureport.build.Context;
import jnpf.ureport.cell.CellBuilder;
import jnpf.ureport.cell.DuplicateType;
import jnpf.ureport.definition.BlankCellInfo;
import jnpf.ureport.model.Cell;
import jnpf.ureport.model.Row;
import jnpf.ureport.utils.DataUtils;
import jnpf.util.RandomUtil;

import java.util.List;
import java.util.Map;

/**
 * @author
 * @since 2016年11月1日
 */
public class DownExpandBuilder implements CellBuilder {
    @Override
    public Cell buildCell(List<BindData> dataList, Cell cell, Context context) {
        Range duplicateRange = cell.getDuplicateRange();
        int mainCellRowNumber = cell.getRow().getRowNumber();
        Range rowRange = buildRowRange(mainCellRowNumber, duplicateRange);
        DownDuplocatorWrapper downDuplocatorWrapper = buildCellDownDuplicator(cell, context, rowRange);
        int rowSize = rowRange.getEnd() - rowRange.getStart() + 1;
        DownBlankCellApply downBlankCellApply = new DownBlankCellApply(rowSize, cell, context, downDuplocatorWrapper);
        CellDownDuplicateUnit unit = new CellDownDuplicateUnit(context, downDuplocatorWrapper, cell, mainCellRowNumber, rowSize);
        Cell lastCell = cell;
        String groupId = RandomUtil.uuId();
        for (int i = 0; i < dataList.size(); i++) {
            BindData bindData = dataList.get(i);
            if (i == 0) {
                cell.setData(bindData.getValue());
                cell.setBindData(bindData.getDataList());
                cell.setDataList(dataList);
                cell.setLeftGroupId(groupId);
                DataUtils.cellList(cell, true, true);
                continue;
            }
            boolean useBlankCell = downBlankCellApply.useBlankCell(i, bindData);
            if (useBlankCell) {
                continue;
            }
            Cell newCell = cell.newCell();
            newCell.setRowNumber(newCell.getRowNumber() + i);
            newCell.setData(bindData.getValue());
            newCell.setBindData(bindData.getDataList());
            newCell.setDataList(dataList);
            newCell.setLeftGroupId(groupId);
            newCell.setProcessed(true);
            Cell leftParentCell = cell.getLeftParentCell();
            if (leftParentCell != null) {
                leftParentCell.addRowChild(newCell);
            }
            Cell topParentCell = cell.getTopParentCell();
            if (topParentCell != null) {
                topParentCell.addColumnChild(newCell);
            }
            DataUtils.cellList(newCell, true, true);
            unit.setGroupId(RandomUtil.uuId());
            unit.duplicate(newCell, i);
            lastCell = newCell;
        }
        unit.complete();
        return lastCell;
    }

    private Range buildRowRange(int mainCellRowNumber, Range range) {
        int start = mainCellRowNumber + range.getStart();
        int end = mainCellRowNumber + range.getEnd();
        Range ranges = new Range();
        ranges.setStart(start);
        ranges.setEnd(end);
        return ranges;
    }

    private DownDuplocatorWrapper buildCellDownDuplicator(Cell cell, Context context, Range range) {
        DownDuplocatorWrapper duplicatorWrapper = new DownDuplocatorWrapper(cell.getName());
        buildParentCellDuplicators(cell, cell, duplicatorWrapper);
        for (int i = range.getStart(); i <= range.getEnd(); i++) {
            Row row = context.getRow(i);
            List<Cell> rowCells = row.getCells();
            for (Cell rowCell : rowCells) {
                rowCell.setLeftGroupId(RandomUtil.uuId());
                buildDuplicator(duplicatorWrapper, cell, rowCell, i);
            }
        }
        return duplicatorWrapper;
    }

    private void buildParentCellDuplicators(Cell cell, Cell mainCell, DownDuplocatorWrapper duplicatorWrapper) {
        Cell leftParentCell = cell.getLeftParentCell();
        if (leftParentCell == null) {
            return;
        }
        buildDuplicator(duplicatorWrapper, mainCell, leftParentCell, 0);
        buildParentCellDuplicators(leftParentCell, mainCell, duplicatorWrapper);
    }

    private void buildDuplicator(DownDuplocatorWrapper duplicatorWrapper, Cell mainCell, Cell currentCell, int rowNumber) {
        if (currentCell == mainCell) {
            return;
        }
        String name = currentCell.getName();
        Map<String, BlankCellInfo> newBlankCellNamesMap = mainCell.getNewBlankCellsMap();
        List<String> increaseCellNames = mainCell.getIncreaseSpanCellNames();
        List<String> newCellNames = mainCell.getNewCellNames();
        if (newBlankCellNamesMap.containsKey(name)) {
            if (!duplicatorWrapper.contains(currentCell)) {
                CellDownDuplicator cellDuplicator = new CellDownDuplicator(currentCell, DuplicateType.Blank, newBlankCellNamesMap.get(name), rowNumber);
                duplicatorWrapper.addCellDownDuplicator(cellDuplicator);
            }
        } else if (increaseCellNames.contains(name)) {
            if (!duplicatorWrapper.contains(currentCell)) {
                CellDownDuplicator cellDuplicator = new CellDownDuplicator(currentCell, DuplicateType.IncreaseSpan, rowNumber);
                duplicatorWrapper.addCellDownDuplicator(cellDuplicator);
            }
        } else if (newCellNames.contains(name)) {
            CellDownDuplicator cellDuplicator = new CellDownDuplicator(currentCell, DuplicateType.Duplicate, rowNumber);
            duplicatorWrapper.addCellDownDuplicator(cellDuplicator);
        } else if (mainCell.getName().equals(name)) {
            CellDownDuplicator cellDuplicator = new CellDownDuplicator(currentCell, DuplicateType.Self, rowNumber);
            duplicatorWrapper.addCellDownDuplicator(cellDuplicator);
        }
    }
}

