package jnpf.ureport.expression.condition;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * @since 4月2日
 */
@Setter
@Getter
public class CellCoordinateSet {
	private List<CellCoordinate> cellCoordinates = new ArrayList<>();
}
