package jnpf.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ColumnList {

    @Schema(description = "是否启用分栏")
    private boolean columnState;

    @Schema(description = "分栏类型 row-列分栏，col-行分栏")
    private String columnStyle;

    @Schema(description = "分栏类型 2-分成多少列，超出多少数量分栏")
    private String columnType;

    @Schema(description = "")
    private Integer maxCol;

    @Schema(description = "")
    private Integer rowCount;

    @Schema(description = "超过列号数量")
    private Integer maxRow;

    @Schema(description = "超过行号数量")
    private Integer colCount;

    @Schema(description = "分栏数据")
    private String columnData;

    @Schema(description = "复制列号")
    private String copyCol;

    @Schema(description = "复制行号")
    private String copyRow;

    @Schema(description = "是否填充空白格")
    private boolean fillEmptyRows;


    @Schema(description = "分栏数据行范围")
    private Integer rowMaxNum;

    private Integer rowMinNum;
    @Schema(description = "分栏数据列范围")
    private Integer colMaxNum;

    private Integer colMinNum;
}
