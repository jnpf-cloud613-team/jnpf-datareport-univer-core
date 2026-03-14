package jnpf.ureport.expression.expr;


import jnpf.ureport.build.Context;
import jnpf.ureport.expression.condition.CellCoordinate;
import jnpf.ureport.expression.condition.CellCoordinateSet;
import jnpf.ureport.expression.condition.Condition;
import jnpf.ureport.expression.condition.CoordinateType;
import jnpf.ureport.expression.data.ExpressionData;
import jnpf.ureport.expression.data.ObjectExpressionData;
import jnpf.ureport.expression.data.ObjectListExpressionData;
import jnpf.ureport.model.Cell;
import jnpf.ureport.utils.DataUtils;
import jnpf.util.StringUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author
 * @since 1月1日
 */
@Setter
@Getter
public class CellCoordinateExpression extends CellExpression {
    private Condition condition;
    private CellCoordinateSet leftCoordinate;
    private CellCoordinateSet topCoordinate;

    public CellCoordinateExpression(String cellName) {
        super(cellName);
    }


    @Override
    public boolean supportPaging() {
        return false;
    }

    @Override
    protected ExpressionData compute(Cell cell, Cell currentCell, Context context) {
        //todo
        while (!context.isCellPocessed(cellName)) {
            context.getReportBuilder().buildCell(context, null);
        }
        List<Cell> leftCellList = buildLeftCells(cell, context);
        List<Cell> topCellList = buildTopCells(cell, context);
        List<Object> list = new ArrayList<>();
        if (leftCellList == null) {
            if (topCellList != null) {
                topCellList = filterCells(cell, context, condition, topCellList);
                for (Cell c : topCellList) {
                    list.add(c.getData());
                }
            } else {
                List<Cell> cells = context.getReport().getCellsMap().get(cellName);
                cells = filterCells(cell, context, condition, cells);
                for (Cell c : cells) {
                    list.add(c.getData());
                }
            }
        } else {
            if (topCellList != null) {
                leftCellList = filterCells(cell, context, condition, leftCellList);
                topCellList = filterCells(cell, context, condition, topCellList);
                for (Cell c : topCellList) {
                    if (leftCellList.contains(c)) {
                        list.add(c.getData());
                    }
                }
            } else {
                leftCellList = filterCells(cell, context, condition, leftCellList);
                for (Cell c : leftCellList) {
                    list.add(c.getData());
                }
            }
        }
        if (list.size() == 1) {
            return new ObjectExpressionData(list.get(0));
        } else {
            return new ObjectListExpressionData(list);
        }
    }

    private List<Cell> buildLeftCells(Cell cell, Context context) {
        if (leftCoordinate == null) {
            return null;
        }
        List<Cell> cellList = null;
        Cell targetLeftCell = null;
        int number = cell.getRow().getRowNumber();
        List<CellCoordinate> leftCoordinates = leftCoordinate.getCellCoordinates();
        Map<String, List<Cell>> cellsMap = context.getReport().getCellsMap();
        for (CellCoordinate coordinate : leftCoordinates) {
            String name = coordinate.getCellName();
            int position = coordinate.getPosition();
            while (!context.isCellPocessed(name)) {
                context.getReportBuilder().buildCell(context, null);
            }
            if (coordinate.getCoordinateType().equals(CoordinateType.relative)) {
                cellList = DataUtils.fetchTargetCells(cell, context, name);
            } else {
                Cell leftCell = left(cell, name);
                cellList = new ArrayList<>();
                if (leftCell != null) {
                    number = leftCell.getRow().getRowNumber() + position;
                    for (Cell left : cellsMap.get(name)) {
                        int rowNumber = left.getRow().getRowNumber();
                        if (Objects.equals(rowNumber, number)) {
                            cellList = leftCell(left, cellsMap, name);
                            break;
                        }
                        int rowSpan = leftCell.getRowSpan();
                        if (rowSpan > 0) {
                            int numberStart = rowNumber;
                            int numberEnd = rowNumber + rowSpan - 1;
                            if (numberStart <= number && numberEnd >= number) {
                                cellList = leftCell(left, cellsMap, name);
                                break;
                            }
                        }
                    }
                }
            }
            for (Cell leftCell : cellList) {
                int rowNumber = leftCell.getRow().getRowNumber();
                if (Objects.equals(rowNumber, number)) {
                    targetLeftCell = leftCell;
                    break;
                }
                int rowSpan = leftCell.getRowSpan();
                if (rowSpan > 0) {
                    int numberStart = rowNumber;
                    int numberEnd = numberStart + rowSpan - 1;
                    if (numberStart <= number && numberEnd >= number) {
                        targetLeftCell = leftCell;
                        break;
                    }
                }
            }
        }
        List<Cell> childCellList = cellsMap.get(cellName) != null ? cellsMap.get(cellName) : new ArrayList<>();
        Set<Cell> leftCellList = new HashSet<>();
        if (targetLeftCell != null) {
            for (Cell childCell : childCellList) {
                Cell left = left(childCell, targetLeftCell.getName());
                if (Objects.equals(left, targetLeftCell)) {
                    leftCellList.add(childCell);
                }
            }
        }
        return leftCellList.isEmpty() ? null : new ArrayList<>(leftCellList);
    }

    private List<Cell> buildTopCells(Cell cell, Context context) {
        if (topCoordinate == null) {
            return null;
        }
        List<Cell> cellList = null;
        Cell targetTopCell = null;
        int number = cell.getColumn().getColumnNumber();
        List<CellCoordinate> topCoordinates = topCoordinate.getCellCoordinates();
        Map<String, List<Cell>> cellsMap = context.getReport().getCellsMap();
        for (CellCoordinate coordinate : topCoordinates) {
            String name = coordinate.getCellName();
            int position = coordinate.getPosition();
            while (!context.isCellPocessed(name)) {
                context.getReportBuilder().buildCell(context, null);
            }
            if (coordinate.getCoordinateType().equals(CoordinateType.relative)) {
                cellList = DataUtils.fetchTargetCells(cell, context, name);
            } else {
                Cell topCell = top(cell, name);
                cellList = new ArrayList<>();
                if (topCell != null) {
                    number = topCell.getColumn().getColumnNumber() + position;
                    for (Cell left : cellsMap.get(name)) {
                        int columnNumber = left.getColumn().getColumnNumber();
                        if (Objects.equals(columnNumber, number)) {
                            cellList = topCell(left, cellsMap, name);
                            break;
                        }
                        int colSpan = topCell.getColSpan();
                        if (colSpan > 0) {
                            int numberStart = columnNumber;
                            int childRowNumberEnd = columnNumber + colSpan - 1;
                            if (numberStart <= number && childRowNumberEnd >= number) {
                                cellList = topCell(left, cellsMap, name);
                                break;
                            }
                        }
                    }
                }
            }
            for (Cell topCell : cellList) {
                int columnNumber = topCell.getColumn().getColumnNumber();
                if (Objects.equals(columnNumber, number)) {
                    targetTopCell = topCell;
                    break;
                }
                int rowSpan = topCell.getRowSpan();
                if (rowSpan > 0) {
                    int numberStart = columnNumber;
                    int numberEnd = numberStart + rowSpan - 1;
                    if (numberStart <= number && numberEnd >= number) {
                        targetTopCell = topCell;
                        break;
                    }
                }
            }
        }
        List<Cell> childCellList = cellsMap.get(cellName) != null ? cellsMap.get(cellName) : new ArrayList<>();
        Set<Cell> leftCellList = new HashSet<>();
        if (targetTopCell != null) {
            for (Cell childCell : childCellList) {
                Cell top = top(childCell, targetTopCell.getName());
                if (Objects.equals(top, targetTopCell)) {
                    leftCellList.add(childCell);
                }
            }
        }
        return leftCellList.isEmpty() ? null : new ArrayList<>(leftCellList);
    }

    private List<Cell> leftCell(Cell cell, Map<String, List<Cell>> cellMap, String cellName) {
        List<Cell> cellList = new ArrayList<>();
        if (cell == null) {
            return cellList;
        }
        if (Objects.equals(cell.getName(), cellName)) {
            List<Cell> cellNameList = cellMap.get(cellName) != null ? cellMap.get(cellName) : new ArrayList<>();
            Map<String, List<Cell>> map = cellNameList.stream().filter(t -> StringUtil.isNotEmpty(t.getLeftGroupId())).collect(Collectors.groupingBy(Cell::getLeftGroupId));
            if (StringUtil.isNotEmpty(cell.getLeftGroupId())) {
                if (map.get(cell.getLeftGroupId()) != null) {
                    cellList.addAll(map.get(cell.getLeftGroupId()));
                }
            }
            return cellList;
        }
        return leftCell(cell.getLeftParentCell(), cellMap, cellName);
    }

    private List<Cell> topCell(Cell cell, Map<String, List<Cell>> cellMap, String cellName) {
        List<Cell> cellList = new ArrayList<>();
        if (cell == null) {
            return cellList;
        }
        if (Objects.equals(cell.getName(), cellName)) {
            List<Cell> cellNameList = cellMap.get(cellName) != null ? cellMap.get(cellName) : new ArrayList<>();
            Map<String, List<Cell>> map = cellNameList.stream().filter(t -> StringUtil.isNotEmpty(t.getTopGroupId())).collect(Collectors.groupingBy(Cell::getTopGroupId));
            if (StringUtil.isNotEmpty(cell.getTopGroupId())) {
                if (map.get(cell.getTopGroupId()) != null) {
                    cellList.addAll(map.get(cell.getTopGroupId()));
                }
            }
            return cellList;
        }
        return topCell(cell.getTopParentCell(), cellMap, cellName);
    }

    private Cell top(Cell cell, String cellName) {
        if (cell == null) {
            return null;
        }
        if (Objects.equals(cell.getName(), cellName)) {
            return cell;
        }
        return top(cell.getTopParentCell(), cellName);
    }

    private Cell left(Cell cell, String cellName) {
        if (cell == null) {
            return null;
        }
        if (Objects.equals(cell.getName(), cellName)) {
            return cell;
        }
        return left(cell.getLeftParentCell(), cellName);
    }


}
