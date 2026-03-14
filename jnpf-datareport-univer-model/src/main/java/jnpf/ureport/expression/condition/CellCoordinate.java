package jnpf.ureport.expression.condition;

import lombok.Data;

/**
 * @author
 * @since 4月2日
 */
@Data
public class CellCoordinate {
	private String cellName;
	private int position;
	private boolean reverse;
	private CoordinateType coordinateType;

	public CellCoordinate(String cellName, CoordinateType coordinateType) {
		this.cellName = cellName;
		this.coordinateType=coordinateType;
	}

}
