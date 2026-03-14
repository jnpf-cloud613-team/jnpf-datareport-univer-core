package jnpf.ureport.cell.down;

import jnpf.ureport.cell.DuplicateType;
import jnpf.ureport.model.Cell;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class DownDuplocatorWrapper {
    private String mainCellName;
    private List<CellDownDuplicator> mainCellChildren = new ArrayList<>();
    private List<CellDownDuplicator> cellDuplicators = new ArrayList<>();
    private Map<Cell, List<CellDownDuplicator>> createNewDuplicatorsMap = new HashMap<>();
    private List<Cell> duplicatorCells = new ArrayList<>();

    public DownDuplocatorWrapper(String mainCellName) {
        this.mainCellName = mainCellName;
    }

    public void addCellDownDuplicator(CellDownDuplicator duplicator) {
        if (duplicator.getDuplicateType().equals(DuplicateType.Duplicate)) {
            addCellDownDuplicatorToMap(duplicator);
        } else {
            cellDuplicators.add(duplicator);
            duplicatorCells.add(duplicator.getCell());
        }
    }

    private void addCellDownDuplicatorToMap(CellDownDuplicator duplicator) {
        Cell leftParentCell = duplicator.getCell().getLeftParentCell();
        if (leftParentCell.getName().equals(mainCellName)) {
            mainCellChildren.add(duplicator);
        }
        List<CellDownDuplicator> list = null;
        if (createNewDuplicatorsMap.containsKey(leftParentCell)) {
            list = createNewDuplicatorsMap.get(leftParentCell);
        } else {
            list = new ArrayList<>();
            createNewDuplicatorsMap.put(leftParentCell, list);
        }
        list.add(duplicator);
    }

    public boolean contains(Cell cell) {
        return duplicatorCells.contains(cell);
    }

    public List<CellDownDuplicator> getMainCellChildren() {
        return mainCellChildren;
    }

    public List<CellDownDuplicator> fetchChildrenDuplicator(Cell leftParentCell) {
        return createNewDuplicatorsMap.get(leftParentCell);
    }

}
