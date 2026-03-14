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
public class UniverSheetFreeze implements Serializable {
    private Integer xSplit = 0;
    private Integer ySplit = 0;
    private Integer startRow = -1;
    private Integer startColumn = -1;
}
