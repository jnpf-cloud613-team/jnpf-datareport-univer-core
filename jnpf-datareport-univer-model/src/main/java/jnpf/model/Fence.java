package jnpf.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class Fence {

    @Schema(description = "sheet")
    private String sheet;

    @Schema(description = "sheet名称")
    private String sheetName;

    @Schema(description = "分栏设置")
    private ColumnList columnList;


}
