package jnpf.univer.sheet;

import lombok.Data;

import java.io.Serializable;

/**
 * @author ：JNPF开发平台组
 * @version: V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date ：2024/5/11 下午4:35
 */
@Data
public class UniverSheetRange implements Serializable {
    /**
     * The start row (inclusive) of the range
     * startRow
     */
    private Integer startRow;

    /**
     * The end row (exclusive) of the range
     * endRow
     */
    private Integer endRow;
    /**
     * The start column (inclusive) of the range
     * startColumn
     */
    private Integer startColumn;

    /**
     * The end column (exclusive) of the range
     * endColumn
     */
    private Integer endColumn;

    private Integer rangeType;

    private Integer startAbsoluteRefType;

    private Integer endAbsoluteRefType;

    private String unitId;

    private String sheetId;
}
