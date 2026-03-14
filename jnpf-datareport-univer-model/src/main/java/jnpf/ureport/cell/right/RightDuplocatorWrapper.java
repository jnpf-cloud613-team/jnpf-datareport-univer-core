package jnpf.ureport.cell.right;

import jnpf.ureport.cell.DuplicateType;
import jnpf.ureport.model.Cell;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @since 2月25日
 */
@Setter
@Getter
public class RightDuplocatorWrapper {
    private String mainCellName;
    private List<CellRightDuplicator> mainCellChildren = new ArrayList<>();
    private List<CellRightDuplicator> cellDuplicators = new ArrayList<>();
    private Map<Cell, List<CellRightDuplicator>> createNewDuplicatorsMap = new HashMap<>();
    private List<Cell> duplicatorCells = new ArrayList<>();

    public RightDuplocatorWrapper(String mainCellName) {
        this.mainCellName = mainCellName;
    }

    public void addCellRightDuplicator(CellRightDuplicator duplicator) {
        if (duplicator.getDuplicateType().equals(DuplicateType.Duplicate)) {
            addCellRightDuplicatorToMap(duplicator);
        } else {
            cellDuplicators.add(duplicator);
            duplicatorCells.add(duplicator.getCell());
        }
    }

    private void addCellRightDuplicatorToMap(CellRightDuplicator duplicator) {
        Cell topParentCell = duplicator.getCell().getTopParentCell();
        if (topParentCell.getName().equals(mainCellName)) {
            mainCellChildren.add(duplicator);
        }
        List<CellRightDuplicator> list = null;
        if (createNewDuplicatorsMap.containsKey(topParentCell)) {
            list = createNewDuplicatorsMap.get(topParentCell);
        } else {
            list = new ArrayList<>();
            createNewDuplicatorsMap.put(topParentCell, list);
        }
        list.add(duplicator);
    }

    public boolean contains(Cell cell) {
        return duplicatorCells.contains(cell);
    }

    public List<CellRightDuplicator> getMainCellChildren() {
        return mainCellChildren;
    }

    public List<CellRightDuplicator> fetchChildrenDuplicator(Cell topParentCell) {
        return createNewDuplicatorsMap.get(topParentCell);
    }

    public List<CellRightDuplicator> getCellDuplicators() {
        return cellDuplicators;
    }
}
