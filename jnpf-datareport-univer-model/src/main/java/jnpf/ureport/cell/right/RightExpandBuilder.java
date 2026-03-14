package jnpf.ureport.cell.right;

import jnpf.ureport.Range;
import jnpf.ureport.build.BindData;
import jnpf.ureport.build.Context;
import jnpf.ureport.cell.CellBuilder;
import jnpf.ureport.cell.DuplicateType;
import jnpf.ureport.definition.BlankCellInfo;
import jnpf.ureport.model.Cell;
import jnpf.ureport.model.Column;
import jnpf.ureport.utils.DataUtils;
import jnpf.util.RandomUtil;

import java.util.List;
import java.util.Map;

/**
 * @author
 * @since 2016年11月1日
 */

public class RightExpandBuilder implements CellBuilder {

    @Override
    public Cell buildCell(List<BindData> dataList, Cell cell, Context context) {
        Range duplicateRange = cell.getDuplicateRange();
        int mainCellColNumber = cell.getColumn().getColumnNumber();
        Range colRange = buildColRange(mainCellColNumber, duplicateRange);

        RightDuplocatorWrapper rightDuplocatorWrapper = buildCellRightDuplicator(cell, context, colRange);

        int colSize = colRange.getEnd() - colRange.getStart() + 1;
        RightBlankCellApply rightBlankCellApply = new RightBlankCellApply(colSize, cell, context, rightDuplocatorWrapper);
        CellRightDuplicateUnit unit = new CellRightDuplicateUnit(context, rightDuplocatorWrapper, cell, mainCellColNumber, colSize);
        Cell lastCell = cell;
        String groupId = RandomUtil.uuId();
        for (int i = 0; i < dataList.size(); i++) {
            BindData bindData = dataList.get(i);
            if (i == 0) {
                cell.setData(bindData.getValue());
                cell.setBindData(bindData.getDataList());
                cell.setDataList(dataList);
                cell.setTopGroupId(groupId);
                DataUtils.cellList(cell, true, true);
                continue;
            }
            boolean useBlank = rightBlankCellApply.useBlankCell(i, bindData);
            if (useBlank) {
                continue;
            }
            Cell newCell = cell.newCell();
            newCell.setColumnNumber(cell.getColumnNumber() + i);
            newCell.setData(bindData.getValue());
            newCell.setBindData(bindData.getDataList());
            newCell.setDataList(dataList);
            newCell.setTopGroupId(groupId);
            newCell.setProcessed(true);
            Cell topParentCell = cell.getTopParentCell();
            if (topParentCell != null) {
                topParentCell.addColumnChild(newCell);
            }
            Cell leftParentCell = cell.getLeftParentCell();
            if (leftParentCell != null) {
                leftParentCell.addRowChild(newCell);
            }
            DataUtils.cellList(newCell, true, true);
            unit.setGroupId(RandomUtil.uuId());
            unit.duplicate(newCell, i);
            lastCell = newCell;
        }
        unit.complete();
        return lastCell;
    }


    private Range buildColRange(int mainCellRowNumber, Range range) {
        int start = mainCellRowNumber + range.getStart();
        int end = mainCellRowNumber + range.getEnd();
        Range ranges = new Range();
        ranges.setStart(start);
        ranges.setEnd(end);
        return ranges;
    }

    private RightDuplocatorWrapper buildCellRightDuplicator(Cell cell, Context context, Range range) {
        RightDuplocatorWrapper duplicatorWrapper = new RightDuplocatorWrapper(cell.getName());
        buildParentCellDuplicators(cell, cell, duplicatorWrapper);
        for (int i = range.getStart(); i <= range.getEnd(); i++) {
            Column col = context.getColumn(i);
            List<Cell> colCells = col.getCells();
            for (Cell colCell : colCells) {
                colCell.setTopGroupId(RandomUtil.uuId());
                buildDuplicator(duplicatorWrapper, cell, colCell, i);
            }
        }
        return duplicatorWrapper;
    }

    private void buildParentCellDuplicators(Cell cell, Cell mainCell, RightDuplocatorWrapper duplicatorWrapper) {
        Cell topParentCell = cell.getTopParentCell();
        if (topParentCell == null) {
            return;
        }
        buildDuplicator(duplicatorWrapper, mainCell, topParentCell, 0);
        buildParentCellDuplicators(topParentCell, mainCell, duplicatorWrapper);
    }

    private void buildDuplicator(RightDuplocatorWrapper duplicatorWrapper, Cell mainCell, Cell currentCell, int currentCellColNumber) {
        if (mainCell.equals(currentCell)) {
            return;
        }
        String name = currentCell.getName();
        Map<String, BlankCellInfo> newBlankCellNamesMap = mainCell.getNewBlankCellsMap();
        List<String> increaseCellNames = mainCell.getIncreaseSpanCellNames();
        List<String> newCellNames = mainCell.getNewCellNames();
        if (newBlankCellNamesMap.containsKey(name)) {
            if (!duplicatorWrapper.contains(currentCell)) {
                CellRightDuplicator cellDuplicator = new CellRightDuplicator(currentCell, DuplicateType.Blank, newBlankCellNamesMap.get(name), currentCellColNumber);
                duplicatorWrapper.addCellRightDuplicator(cellDuplicator);
            }
        } else if (increaseCellNames.contains(name)) {
            if (!duplicatorWrapper.contains(currentCell)) {
                CellRightDuplicator cellDuplicator = new CellRightDuplicator(currentCell, DuplicateType.IncreaseSpan, currentCellColNumber);
                duplicatorWrapper.addCellRightDuplicator(cellDuplicator);
            }
        } else if (newCellNames.contains(name)) {
            CellRightDuplicator cellDuplicator = new CellRightDuplicator(currentCell, DuplicateType.Duplicate, currentCellColNumber);
            duplicatorWrapper.addCellRightDuplicator(cellDuplicator);
        } else if (mainCell.getName().equals(name)) {
            CellRightDuplicator cellDuplicator = new CellRightDuplicator(currentCell, DuplicateType.Self, currentCellColNumber);
            duplicatorWrapper.addCellRightDuplicator(cellDuplicator);
        }
    }
}
